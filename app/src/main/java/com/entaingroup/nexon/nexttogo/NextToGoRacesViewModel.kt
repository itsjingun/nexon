package com.entaingroup.nexon.nexttogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entaingroup.nexon.nexttogo.NextToGoRacesContract.Companion.MAX_NUMBER_OF_RACES
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesInteractor
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NextToGoRacesViewModel @Inject constructor(
    private val nextToGoRacesInteractor: NextToGoRacesInteractor,
    val timeProvider: TimeProvider,
) : ViewModel() {
    private val mutableViewState = MutableStateFlow(NextToGoRacesContract.ViewState.INITIAL)
    val viewState: StateFlow<NextToGoRacesContract.ViewState> = mutableViewState.asStateFlow()

    /**
     * A global ticker that is used to keep the race timers in sync.
     */
    private val mutableTicker = MutableSharedFlow<Unit>()
    val ticker: Flow<Unit> = mutableTicker.asSharedFlow()

    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        nextToGoRacesInteractor.backgroundErrors
            .onEach { error -> handleError(error) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            nextToGoRacesInteractor.nextRaces.collect { races ->
                mutableViewState.update { it.copy(races = races) }
            }
        }

        viewModelScope.launch {
            while (true) {
                mutableTicker.emit(Unit)
                delay(1000) // Run every second
            }
        }

        startRaceUpdates()
    }

    private fun startRaceUpdates() {
        nextToGoRacesInteractor.startRaceUpdates(
            count = MAX_NUMBER_OF_RACES,
            categories = mutableViewState.value.selectedCategories,
        )
    }

    private fun handleError(throwable: Throwable) {
        // TODO: Possibly respond differently depending on type of error.
        nextToGoRacesInteractor.stopRaceUpdates()
        mutableViewState.update { it.copy(showError = true) }
    }

    fun onTryAgainButtonClick() {
        viewModelScope.launch {
            mutableViewState.update { it.copy(showError = false) }
            startRaceUpdates()
        }
    }

    fun toggleRacingCategory(category: RacingCategory) {
        val selectedCategories = mutableViewState.value.selectedCategories
        val updatedCategories = if (selectedCategories.contains(category)) {
            selectedCategories.minus(category)
        } else {
            selectedCategories.plus(category)
        }

        mutableViewState.update {
            it.copy(selectedCategories = updatedCategories)
        }

        nextToGoRacesInteractor.startRaceUpdates(
            count = MAX_NUMBER_OF_RACES,
            categories = updatedCategories,
        )
    }
}
