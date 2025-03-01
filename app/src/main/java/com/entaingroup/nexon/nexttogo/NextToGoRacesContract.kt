package com.entaingroup.nexon.nexttogo

import com.entaingroup.nexon.nexttogo.domain.Race
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal interface NextToGoRacesContract {

    data class ViewState(
        val racesFlow: Flow<List<Race>>,
        val categories: Set<RacingCategory>,
        val selectedCategories: Set<RacingCategory>,
    ) {
        companion object {
            val INITIAL = ViewState(
                racesFlow = emptyFlow(),
                categories = setOf(
                    RacingCategory.GREYHOUND,
                    RacingCategory.HARNESS,
                    RacingCategory.HORSE,
                ),
                selectedCategories = setOf(
                    RacingCategory.GREYHOUND,
                    RacingCategory.HARNESS,
                    RacingCategory.HORSE,
                ),
            )
        }
    }

    companion object {
        const val MAX_NUMBER_OF_RACES = 5
    }
}
