package com.entaingroup.nexon.nexttogo.data

import com.entaingroup.nexon.nexttogo.data.api.NextToGoRacesApi
import com.entaingroup.nexon.nexttogo.data.persisted.DbRace
import com.entaingroup.nexon.nexttogo.data.persisted.NextToGoDatabase
import com.entaingroup.nexon.nexttogo.data.persisted.toRace
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import com.entaingroup.nexon.nexttogo.domain.Race
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import com.entaingroup.nexon.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

internal class DefaultNextToGoRacesRepository @Inject constructor(
    private val nextToGoRacesApi: NextToGoRacesApi,
    nextToGoDatabase: NextToGoDatabase,
) : NextToGoRacesRepository {

    private val dbRaceDao = nextToGoDatabase.dbRaceDao()

    private var nextUpdateTime: Instant? = null
    private var isFetching = false

    /**
     * A surplus added to the count when fetching from the server.
     */
    private var surplusCount = DEFAULT_FETCH_SURPLUS

    /**
     * A [Flow] that emits the minimum start time, used in the Room database query.
     *
     * Note: Whenever this emits a value, it will also trigger an emission for [getNextRaces].
     */
    private val minStartTimeFlow = MutableStateFlow<Instant>(
        Instant.now().minusSeconds(EXPIRY_THRESHOLD)
    )

    private val mutableTicker = MutableSharedFlow<Unit>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            while (true) {
                nextUpdateTime?.let { updateTime ->
                    if (!isFetching) {
                        // Trigger the [getNextRaces] flow in order to possibly fetch more data.
                        if (Instant.now() >= updateTime) {
                            minStartTimeFlow.value =
                                Instant.now().minusSeconds(EXPIRY_THRESHOLD) // 1 minute
                        }
                    }
                }

                mutableTicker.emit(Unit)
                delay(1000) // Run every second
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNextRaces(categories: Set<RacingCategory>, count: Int): Flow<List<Race>> {
        return minStartTimeFlow
            .flatMapLatest { minStartTime ->
                // Add a buffer to the count so that data can be fetched earlier than strictly
                // necessary so that the UI can maintain the required number of items when
                // the list is being updated.
                val countWithBuffer = count + COUNT_BUFFER

                val flow = if (categories.isEmpty()) {
                    dbRaceDao.getNextRaces(
                        count = countWithBuffer,
                        minStartTime = minStartTime.epochSecond,
                    )
                } else {
                    dbRaceDao.getNextRacesByCategoryIds(
                        categoryIds = categories.map { it.id }.toSet(),
                        count = countWithBuffer,
                        minStartTime = minStartTime.epochSecond,
                    )
                }

                flow.onEach { races ->
                    Timber.d("Next races emitted: $races")

                    nextUpdateTime = races.firstOrNull()?.let {
                        Instant.ofEpochSecond(it.startTime + EXPIRY_THRESHOLD)
                    }
                    nextUpdateTime?.let {
                        Timber.d(
                            "The next time to update is at: ${DateUtils.format(it)}"
                        )
                    }

                    // Fetch more data if there are an insufficient number an items
                    // in the local database.
                    if (races.size < countWithBuffer) {
                        if (!isFetching) {
                            fetchNextRaces(count + surplusCount)
                            surplusCount += FETCH_SURPLUS_INCREMENT
                        }
                    } else {
                        // Revert to the default surplus once there is sufficient data.
                        surplusCount = DEFAULT_FETCH_SURPLUS
                    }
                }
                    .map {
                        // Take only the required count.
                        it.take(count).map { dbRace -> dbRace.toRace() }
                    }
            }
    }

    private fun fetchNextRaces(count: Int) {
        if (isFetching) return

        isFetching = true
        nextUpdateTime = null

        Timber.d("$count races being fetched...")

        scope.launch {
            delay(1000)

            val apiResponse = nextToGoRacesApi.getNextRaces(
                method = "nextraces",
                count = count,
            )

            // TODO: Handle error (and possibly throw).

            isFetching = false

            val racesToInsert = apiResponse.data.raceSummaries.map { entry ->
                val raceSummary = entry.value
                DbRace(
                    id = raceSummary.raceId,
                    name = raceSummary.raceName,
                    number = raceSummary.raceNumber,
                    categoryId = raceSummary.categoryId,
                    startTime = raceSummary.advertisedStart.seconds,
                )
            }
            dbRaceDao.insertAll(racesToInsert)
        }
    }

    companion object {
        private const val EXPIRY_THRESHOLD = 59L // 59 seconds
        private const val COUNT_BUFFER = 2
        private const val DEFAULT_FETCH_SURPLUS = 5
        private const val FETCH_SURPLUS_INCREMENT = 5
    }
}
