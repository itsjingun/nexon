package com.entaingroup.nexon.nexttogo.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * An API endpoint for fetching races.
 */
internal interface NextToGoRacesApi {
    @GET("rest/v1/racing/")
    suspend fun getNextRaces(
        @Query("method") method: String,
        @Query("count") count: Int,
    ): NextRacesApiResponse
}
