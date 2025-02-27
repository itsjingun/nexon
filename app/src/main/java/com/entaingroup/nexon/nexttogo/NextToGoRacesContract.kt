package com.entaingroup.nexon.nexttogo

import com.entaingroup.nexon.nexttogo.domain.Race

internal interface NextToGoRacesContract {

    data class ViewState(
        val isLoading: Boolean,
        val races: List<Race>?,
    ) {
        companion object {
            val Initial = ViewState(
                isLoading = true,
                races = null,
            )
        }
    }
}
