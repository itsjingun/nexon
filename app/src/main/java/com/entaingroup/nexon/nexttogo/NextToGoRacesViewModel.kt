package com.entaingroup.nexon.nexttogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NextToGoRacesViewModel @Inject constructor(
    private val nextToGoRacesRepository: NextToGoRacesRepository,
) : ViewModel() {

    private val mutableViewState = MutableStateFlow(NextToGoRacesContract.ViewState.INITIAL)
    val viewState: StateFlow<NextToGoRacesContract.ViewState> = mutableViewState.asStateFlow()

    /**
     * A global ticker that is used to keep the race timers in sync.
     */
    private val mutableTicker = MutableSharedFlow<Unit>()
    val ticker: Flow<Unit> = mutableTicker.asSharedFlow()

    init {
        val selectedCategories = mutableViewState.value.selectedCategories
        mutableViewState.update {
            it.copy(
                isLoading = false,
                racesFlow = nextToGoRacesRepository.getNextRaces(
                    categories = selectedCategories,
                    count = RACE_COUNT,
                ),
            )
        }

        viewModelScope.launch {
            while (true) {
                mutableTicker.emit(Unit)
                delay(1000) // Run every second
            }
        }
    }

    fun toggleRacingCategory(category: RacingCategory) {
        mutableViewState.update {
            val selectedCategories = mutableViewState.value.selectedCategories
            val updatedCategories = if (selectedCategories.contains(category)) {
                selectedCategories.minus(category)
            } else {
                selectedCategories.plus(category)
            }

            it.copy(
                selectedCategories = updatedCategories,
                racesFlow = nextToGoRacesRepository.getNextRaces(
                    categories = updatedCategories,
                    count = RACE_COUNT,
                ),
            )
        }
    }

    companion object {
        private const val RACE_COUNT = 5
    }
}
