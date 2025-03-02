package com.entaingroup.nexon.nexttogo.data

import com.entaingroup.nexon.nexttogo.data.api.NextToGoRacesApi
import com.entaingroup.nexon.nexttogo.data.persisted.DbRace
import com.entaingroup.nexon.nexttogo.data.persisted.NextToGoDatabase
import com.entaingroup.nexon.nexttogo.data.persisted.toRace
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesInteractor
import com.entaingroup.nexon.nexttogo.domain.Race
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

internal class DefaultNextToGoRacesInteractor @Inject constructor(
    private val nextToGoRacesApi: NextToGoRacesApi,
    private val nextToGoDatabase: NextToGoDatabase,
    private val timeProvider: TimeProvider,
) : NextToGoRacesInteractor {
    private val mutableNextRaces = MutableSharedFlow<List<Race>>()
    override val nextRaces: Flow<List<Race>> = mutableNextRaces.asSharedFlow()

    private val mutableBackgroundErrors = MutableSharedFlow<Exception>()
    override val backgroundErrors = mutableBackgroundErrors.asSharedFlow()

    private val dbRaceDao = nextToGoDatabase.dbRaceDao()

    /**
     * This keeps track of the earliest race's expiry time (i.e. the time at which it should be
     * removed from the UI) so that data can be updated when this time is reached.
     */
    private var nextUpdateTime: Instant? = null

    /**
     * Tracks if the list of races are currently being fetched from the server.
     *
     * This is used to ensure only one fetch is being executed at a time.
     */
    private var isFetching = false

    /**
     * A multiplier for the count when fetching from the server.
     */
    private var fetchCountMultiplier = INITIAL_FETCH_MULTIPLIER

    /**
     * A [Flow] that emits the minimum start time, used in the Room database query.
     *
     * Note: Whenever this emits a value, it will also trigger an emission for the flow used
     * in [startRaceUpdates].
     */
    private val minStartTimeFlow = MutableStateFlow<Instant>(
        timeProvider.now().minusSeconds(EXPIRY_THRESHOLD)
    )

    /**
     * A ticker that runs every second to constantly check whether data needs to be updated.
     */
    private val mutableTicker = MutableSharedFlow<Unit>()

    /**
     * A [CoroutineScope] for running background operations.
     *
     * Note: [Dispatchers.Main] is set as the context to ensure that properties are only changed
     * on the main thread, just for thread safety.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * The [Job] used for emitting data into [nextRaces].
     */
    private var racesJob: Job? = null

    init {
        startTicker()
    }

    private fun startTicker() {
        scope.launch {
            while (true) {
                // Attempt to trigger a database emission (and possibly fetch more data)
                // if an update is needed.
                nextUpdateTime?.let { updateTime ->
                    if (!isFetching) {
                        if (timeProvider.now() >= updateTime) {
                            minStartTimeFlow.value =
                                timeProvider.now().minusSeconds(EXPIRY_THRESHOLD)
                        }
                    }
                }

                mutableTicker.emit(Unit)
                delay(1000) // Run every second
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRaceUpdates(count: Int, categories: Set<RacingCategory>) {
        nextUpdateTime = null
        fetchCountMultiplier = INITIAL_FETCH_MULTIPLIER

        // Add a buffer to the count so that data can be fetched earlier than strictly
        // necessary so that the UI can maintain the required number of items when
        // the list is being updated.
        val countWithBuffer = count + COUNT_BUFFER

        val flow = minStartTimeFlow
            .flatMapLatest { minStartTime ->
                if (categories.isEmpty()) {
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
            }
            .onEach { dbRaces ->
                Timber.d("Races emitted from database: $dbRaces")

                updateNextUpdateTime(races = dbRaces)
                fetchNextRacesIfNeeded(
                    races = dbRaces,
                    minimumSize = countWithBuffer,
                    countForUi = count,
                )
            }
            .map { dbRaces ->
                // Take only the required count.
                dbRaces.take(count).map { it.toRace() }
            }

        racesJob?.cancel()
        racesJob = scope.launch {
            flow.collect { races ->
                mutableNextRaces.emit(races)
            }
        }
    }

    private fun updateNextUpdateTime(races: List<DbRace>) {
        nextUpdateTime = races.firstOrNull()?.let {
            Instant.ofEpochSecond(it.startTime + EXPIRY_THRESHOLD)
        } ?: timeProvider.now()
        nextUpdateTime?.let {
            Timber.d("The next time to update is at: ${DateUtils.format(it)}")
        }
    }

    private fun fetchNextRacesIfNeeded(races: List<DbRace>, minimumSize: Int, countForUi: Int) {
        // Fetch more data if there are an insufficient number of items
        // in the local database.
        if (races.size < minimumSize) {
            nextUpdateTime = timeProvider.now()

            if (!isFetching) {
                // Multiply the fetch count, and coerce to a maximum value.
                val fetchCount = (countForUi * fetchCountMultiplier)
                    .coerceAtMost(MAX_FETCH_COUNT)

                // Delay subsequent fetches (i.e. only the initial fetch should
                // have no delay).
                val delay = if (fetchCountMultiplier > INITIAL_FETCH_MULTIPLIER)
                    1000L else 0

                fetchNextRacesInBackground(count = fetchCount, afterDelay = delay)

                // Increase the fetch multiplier so that the next call fetches more data.
                fetchCountMultiplier += INITIAL_FETCH_MULTIPLIER
            }
        } else {
            // Revert to the initial multiplier once there is sufficient data.
            fetchCountMultiplier = INITIAL_FETCH_MULTIPLIER
        }
    }

    private fun fetchNextRacesInBackground(count: Int, afterDelay: Long = 0) {
        if (isFetching) return

        isFetching = true

        scope.launch {
            // Add a delay to limit frequency of network requests.
            delay(afterDelay)

            Timber.d("$count races being fetched...")

            val apiResponse = try {
                nextToGoRacesApi.getNextRaces(method = "nextraces", count = count)
            } catch (e: Exception) {
                // Note: Naturally we should try to handle specific exceptions separately
                // in real world production code.

                isFetching = false
                mutableBackgroundErrors.emit(e)
                return@launch
            }

            val minStartTime = timeProvider.now().epochSecond - EXPIRY_THRESHOLD
            val racesToInsert = apiResponse.data.raceSummaries.map { entry ->
                val raceSummary = entry.value
                DbRace(
                    id = raceSummary.raceId,
                    meetingName = raceSummary.meetingName,
                    raceNumber = raceSummary.raceNumber,
                    categoryId = raceSummary.categoryId,
                    startTime = raceSummary.advertisedStart.seconds,
                )
            }.filter {
                // Only insert races that are not stale.
                it.startTime >= minStartTime
            }

            try {
                dbRaceDao.insertAll(racesToInsert)
                dbRaceDao.deleteRacesWithStartTimeLowerThan(minStartTime)
            } catch (e: Exception) {
                // Clear database just to be safe.
                withContext(Dispatchers.IO) {
                    nextToGoDatabase.clearAllTables()
                }

                isFetching = false
                mutableBackgroundErrors.emit(e)
                return@launch
            }

            isFetching = false
        }
    }

    override fun stopRaceUpdates() {
        racesJob?.cancel()
        nextUpdateTime = null
    }

    companion object {
        private const val EXPIRY_THRESHOLD = 59L // 59 seconds
        private const val COUNT_BUFFER = 2
        private const val INITIAL_FETCH_MULTIPLIER = 2
        private const val MAX_FETCH_COUNT = 100
    }
}
