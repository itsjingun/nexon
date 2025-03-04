package com.entaingroup.nexon.nexttogo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesAutoUpdater
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import com.entaingroup.nexon.nexttogo.ui.NextToGoRacesContract.Companion.MAX_NUMBER_OF_RACES
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
    private val nextToGoRacesAutoUpdater: NextToGoRacesAutoUpdater,
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

    /**
     * Initializes this ViewModel to start listening to data updates.
     */
    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        nextToGoRacesAutoUpdater.backgroundErrors
            .onEach { error -> handleError(error) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            nextToGoRacesAutoUpdater.nextRaces.collect { races ->
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
        nextToGoRacesAutoUpdater.startRaceUpdates(
            count = MAX_NUMBER_OF_RACES,
            categories = mutableViewState.value.selectedCategories,
        )
    }

    private fun handleError(throwable: Throwable) {
        // TODO: Possibly respond differently depending on type of error.
        nextToGoRacesAutoUpdater.stopRaceUpdates()
        mutableViewState.update { it.copy(showError = true) }
    }

    fun onTryAgainButtonClick() {
        viewModelScope.launch {
            mutableViewState.update { it.copy(showError = false) }
            startRaceUpdates()
        }
    }

    /**
     * Toggle on or off a racing category depending on its current selected state.
     */
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

        nextToGoRacesAutoUpdater.startRaceUpdates(
            count = MAX_NUMBER_OF_RACES,
            categories = updatedCategories,
        )
    }
}
