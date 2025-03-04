package com.entaingroup.nexon.nexttogo.domain

import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import kotlinx.coroutines.flow.Flow

internal interface NextToGoRacesAutoUpdater {
    /**
     * A [Flow] that emits a list of upcoming [Race]s in chronological order.
     */
    val nextRaces: Flow<List<Race>>

    /**
     * A [Flow] that emits any errors encountered during background tasks.
     */
    val backgroundErrors: Flow<Exception>

    /**
     * Starts the automatic updates (for retrieving data for [nextRaces]).
     */
    fun startRaceUpdates(count: Int, categories: Set<RacingCategory>)

    /**
     * Stops automatic updates.
     */
    fun stopRaceUpdates()
}
