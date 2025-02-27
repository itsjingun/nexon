package com.entaingroup.nexon.nexttogo.data

import retrofit2.http.GET
import retrofit2.http.Query

internal interface NextToGoRacesService {

    @GET("rest/v1/racing/")
    suspend fun getNextRaces(
        @Query("method") method: String,
        @Query("count") count: Int,
    ): NextRacesApiResponse
}
