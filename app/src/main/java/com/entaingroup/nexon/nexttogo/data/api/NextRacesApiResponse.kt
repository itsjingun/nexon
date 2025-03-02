package com.entaingroup.nexon.nexttogo.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NextRacesApiResponse(val data: Data)

@Serializable
internal data class Data(
    @SerialName("race_summaries") val raceSummaries: Map<String, RaceSummary>,
)

@Serializable
internal data class RaceSummary(
    @SerialName("race_id") val raceId: String,
    @SerialName("race_number") val raceNumber: Int,
    @SerialName("meeting_name") val meetingName: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("advertised_start") val advertisedStart: AdvertisedStart,
)

@Serializable
internal data class AdvertisedStart(
    @SerialName("seconds") val seconds: Long,
)
