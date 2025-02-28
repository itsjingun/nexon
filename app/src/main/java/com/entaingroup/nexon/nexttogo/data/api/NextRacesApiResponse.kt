package com.entaingroup.nexon.nexttogo.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NextRacesApiResponse(
    val status: Int,
    val data: Data,
    val message: String,
)

@Serializable
internal data class Data(
    @SerialName("next_to_go_ids") val nextToGoIds: List<String>,
    @SerialName("race_summaries") val raceSummaries: Map<String, RaceSummary>,
)

@Serializable
internal data class RaceSummary(
    @SerialName("race_id") val raceId: String,
    @SerialName("race_name") val raceName: String,
    @SerialName("race_number") val raceNumber: Int,
    @SerialName("meeting_id") val meetingId: String,
    @SerialName("meeting_name") val meetingName: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("advertised_start") val advertisedStart: AdvertisedStart,
    @SerialName("race_form") val raceForm: RaceForm,
    @SerialName("venue_id") val venueId: String,
    @SerialName("venue_name") val venueName: String,
    @SerialName("venue_state") val venueState: String,
    @SerialName("venue_country") val venueCountry: String,
)

@Serializable
internal data class AdvertisedStart(
    @SerialName("seconds") val seconds: Long,
)

@Serializable
internal data class RaceForm(
    @SerialName("distance") val distance: Int,
    @SerialName("distance_type") val distanceType: DistanceType,
    @SerialName("distance_type_id") val distanceTypeId: String,
    @SerialName("track_condition") val trackCondition: TrackCondition? = null,
    @SerialName("track_condition_id") val trackConditionId: String? = null,
    @SerialName("weather") val weather: Weather? = null,
    @SerialName("weather_id") val weatherId: String? = null,
    @SerialName("race_comment") val raceComment: String? = null,
    @SerialName("additional_data") val additionalData: String,
    @SerialName("generated") val generated: Int,
    @SerialName("silk_base_url") val silkBaseUrl: String,
    @SerialName("race_comment_alternative") val raceCommentAlternative: String? = null,
)

@Serializable
internal data class DistanceType(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("short_name") val shortName: String,
)

@Serializable
internal data class TrackCondition(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("short_name") val shortName: String,
)

@Serializable
internal data class Weather(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("short_name") val shortName: String,
    @SerialName("icon_uri") val iconUri: String? = null,
)
