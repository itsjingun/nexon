package com.entaingroup.nexon.nexttogo.data

import com.entaingroup.nexon.nexttogo.data.api.AdvertisedStart
import com.entaingroup.nexon.nexttogo.data.api.Data
import com.entaingroup.nexon.nexttogo.data.api.NextRacesApiResponse
import com.entaingroup.nexon.nexttogo.data.api.RaceSummary
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory

internal fun sufficientNumberOfRaces() = NextRacesApiResponse(
    data = Data(
        raceSummaries = mapOf(
            "race_4" to RaceSummary(
                raceId = "race_4",
                raceNumber = 3,
                meetingName = "Royal Ascot",
                categoryId = RacingCategory.HORSE.id,
                advertisedStart = AdvertisedStart(seconds = 5400),
            ),
            "race_1" to RaceSummary(
                raceId = "race_1",
                raceNumber = 2,
                meetingName = "Ellerslie",
                categoryId = RacingCategory.HORSE.id,
                advertisedStart = AdvertisedStart(seconds = 0),
            ),
            "race_2" to RaceSummary(
                raceId = "race_2",
                raceNumber = 5,
                meetingName = "Spring Carnival",
                categoryId = RacingCategory.HARNESS.id,
                advertisedStart = AdvertisedStart(seconds = 1200),
            ),
            "race_7" to RaceSummary(
                raceId = "race_7",
                raceNumber = 6,
                meetingName = "Belmont Stakes",
                categoryId = RacingCategory.GREYHOUND.id,
                advertisedStart = AdvertisedStart(seconds = 10800),
            ),
            "race_3" to RaceSummary(
                raceId = "race_3",
                raceNumber = 8,
                meetingName = "Kentucky Derby",
                categoryId = RacingCategory.GREYHOUND.id,
                advertisedStart = AdvertisedStart(seconds = 3600),
            ),
            "race_5" to RaceSummary(
                raceId = "race_5",
                raceNumber = 1,
                meetingName = "Epsom Derby",
                categoryId = RacingCategory.HORSE.id,
                advertisedStart = AdvertisedStart(seconds = 7200),
            ),
            "race_6" to RaceSummary(
                raceId = "race_6",
                raceNumber = 4,
                meetingName = "Preakness Stakes",
                categoryId = RacingCategory.HORSE.id,
                advertisedStart = AdvertisedStart(seconds = 9000),
            ),
        ),
    ),
)

internal fun notEnoughRaces() = NextRacesApiResponse(
    data = Data(
        raceSummaries = mapOf(
            "race_4" to RaceSummary(
                raceId = "race_4",
                raceNumber = 3,
                meetingName = "Royal Ascot",
                categoryId = RacingCategory.HORSE.id,
                advertisedStart = AdvertisedStart(seconds = 5400),
            ),
            "race_1" to RaceSummary(
                raceId = "race_1",
                raceNumber = 2,
                meetingName = "Ellerslie",
                categoryId = RacingCategory.HORSE.id,
                advertisedStart = AdvertisedStart(seconds = 0),
            ),
            "race_2" to RaceSummary(
                raceId = "race_2",
                raceNumber = 5,
                meetingName = "Spring Carnival",
                categoryId = RacingCategory.HARNESS.id,
                advertisedStart = AdvertisedStart(seconds = 1200),
            ),
        ),
    ),
)
