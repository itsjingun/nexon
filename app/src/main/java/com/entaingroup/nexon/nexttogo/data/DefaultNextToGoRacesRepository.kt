package com.entaingroup.nexon.nexttogo.data

import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import com.entaingroup.nexon.nexttogo.domain.Race
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultNextToGoRacesRepository @Inject constructor(
    private val nextToGoRacesService: NextToGoRacesService,
) : NextToGoRacesRepository {

    override suspend fun fetchNextRaces(count: Int): List<Race> {
        return withContext(Dispatchers.IO) {
            val apiResponse = nextToGoRacesService.getNextRaces(
                method = "nextraces",
                count = count,
            )

            // TODO: Handle error (and possibly throw).

            apiResponse.data.raceSummaries.map { entry ->
                Race(
                    id = entry.value.raceId,
                    name = entry.value.raceName,
                    number = entry.value.raceNumber,
                )
            }
        }
    }
}
