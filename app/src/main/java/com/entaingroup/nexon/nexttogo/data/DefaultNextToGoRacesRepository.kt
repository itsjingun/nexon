package com.entaingroup.nexon.nexttogo.data

import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import com.entaingroup.nexon.nexttogo.domain.Race
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

internal class DefaultNextToGoRacesRepository @Inject constructor(
    private val nextToGoRacesService: NextToGoRacesService,
) : NextToGoRacesRepository {

    override suspend fun fetchNextRaces(count: Int): List<Race> {
        return withContext(Dispatchers.IO) {
            delay(1000)

            val apiResponse = nextToGoRacesService.getNextRaces(
                method = "nextraces",
                count = count,
            )

            // TODO: Handle error (and possibly throw).

            apiResponse.data.raceSummaries.map { entry ->
                val raceSummary = entry.value
                Race(
                    id = raceSummary.raceId,
                    name = raceSummary.raceName,
                    number = raceSummary.raceNumber,
                    startTime = Instant.ofEpochSecond(raceSummary.advertisedStart.seconds),
                )
            }
        }
    }
}
