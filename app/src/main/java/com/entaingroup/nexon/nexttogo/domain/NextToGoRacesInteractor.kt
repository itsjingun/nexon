package com.entaingroup.nexon.nexttogo.domain

import kotlinx.coroutines.flow.Flow

internal interface NextToGoRacesInteractor {
    /**
     * A [Flow] that emits any errors encountered during background tasks.
     */
    val backgroundErrors: Flow<Exception>

    /**
     * A [Flow] that emits a list of upcoming [Race]s in chronological order.
     *
     * @param categories a set of categories to filter for.
     * @param count maximum number of races to fetch.
     * @return the list of races.
     */
    fun getNextRaces(
        categories: Set<RacingCategory> = emptySet(),
        count: Int,
    ): Flow<List<Race>>

    suspend fun clearAllData()
}
