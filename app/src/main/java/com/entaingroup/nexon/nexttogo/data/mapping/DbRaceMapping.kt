package com.entaingroup.nexon.nexttogo.data.mapping

import com.entaingroup.nexon.nexttogo.data.api.NextRacesApiResponse
import com.entaingroup.nexon.nexttogo.data.persisted.DbRace

internal fun NextRacesApiResponse.toDbRaces(): List<DbRace> {
    return data.raceSummaries.map { entry ->
        val raceSummary = entry.value
        DbRace(
            id = raceSummary.raceId,
            meetingName = raceSummary.meetingName,
            raceNumber = raceSummary.raceNumber,
            categoryId = raceSummary.categoryId,
            startTime = raceSummary.advertisedStart.seconds,
        )
    }
}
