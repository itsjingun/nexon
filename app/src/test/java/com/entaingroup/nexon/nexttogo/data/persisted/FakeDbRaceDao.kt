package com.entaingroup.nexon.nexttogo.data.persisted

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeDbRaceDao : DbRaceDao {
    private val raceDatabase = mutableSetOf<DbRace>()
    private val racesFlow = MutableStateFlow<List<DbRace>>(emptyList())

    override fun getNextRaces(count: Int, minStartTime: Long): Flow<List<DbRace>> {
        return racesFlow
            .map { races ->
                races
                    .filter { it.startTime >= minStartTime }
                    .sortedWith(compareBy<DbRace> { it.startTime }.thenBy { it.meetingName })
                    .take(count)
            }
    }

    override fun getNextRacesByCategoryIds(
        categoryIds: Set<String>,
        count: Int,
        minStartTime: Long,
    ): Flow<List<DbRace>> {
        return racesFlow
            .map { races ->
                races
                    .filter {
                        categoryIds.contains(it.categoryId) && it.startTime >= minStartTime
                    }
                    .sortedWith(compareBy<DbRace> { it.startTime }.thenBy { it.meetingName })
                    .take(count)
            }
    }

    override suspend fun insertAll(races: Collection<DbRace>) {
        raceDatabase.addAll(races)
        racesFlow.emit(raceDatabase.toList())
    }

    override suspend fun deleteRacesWithStartTimeLowerThan(time: Long) {
        // TODO: To be done if being tested in a unit test.
    }
}
