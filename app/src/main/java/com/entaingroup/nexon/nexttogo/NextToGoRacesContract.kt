package com.entaingroup.nexon.nexttogo

import com.entaingroup.nexon.nexttogo.domain.Race
import com.entaingroup.nexon.nexttogo.domain.RacingCategory

internal interface NextToGoRacesContract {
    data class ViewState(
        val races: List<Race>,
        val categories: Set<RacingCategory>,
        val selectedCategories: Set<RacingCategory>,
        val showError: Boolean,
    ) {
        companion object {
            val INITIAL = ViewState(
                races = emptyList(),
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
                showError = false,
            )
        }
    }

    companion object {
        const val MAX_NUMBER_OF_RACES = 5
    }
}
