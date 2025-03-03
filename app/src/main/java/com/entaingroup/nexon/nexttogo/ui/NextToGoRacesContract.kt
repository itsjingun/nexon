package com.entaingroup.nexon.nexttogo.ui

import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory

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
                categories = DEFAULT_CATEGORIES,
                selectedCategories = DEFAULT_CATEGORIES,
                showError = false,
            )
        }
    }

    companion object {
        val DEFAULT_CATEGORIES = setOf(
            RacingCategory.GREYHOUND,
            RacingCategory.HARNESS,
            RacingCategory.HORSE,
        )
        const val MAX_NUMBER_OF_RACES = 5
    }
}
