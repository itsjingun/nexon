package com.entaingroup.nexon.nexttogo

import com.entaingroup.nexon.nexttogo.domain.Race
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import kotlinx.coroutines.flow.Flow

internal interface NextToGoRacesContract {

    data class ViewState(
        val isLoading: Boolean,
        val racesFlow: Flow<List<Race>>?,
        val selectedCategories: Set<RacingCategory>,
    ) {
        companion object {
            val INITIAL = ViewState(
                isLoading = true,
                racesFlow = null,
                selectedCategories = emptySet(),
            )
        }
    }
}
