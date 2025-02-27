package com.entaingroup.nexon.nexttogo.domain

internal interface NextToGoRacesRepository {

    /**
     * Fetches a list of the next [Race]s.
     *
     * @param count maximum number of races to fetch.
     * @return the list of races.
     */
    suspend fun fetchNextRaces(count: Int): List<Race>
}
