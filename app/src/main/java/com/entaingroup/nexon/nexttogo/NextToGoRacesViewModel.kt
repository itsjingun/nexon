package com.entaingroup.nexon.nexttogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entaingroup.nexon.nexttogo.NextToGoRacesContract.Companion.MAX_NUMBER_OF_RACES
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesInteractor
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NextToGoRacesViewModel @Inject constructor(
    private val nextToGoRacesInteractor: NextToGoRacesInteractor,
) : ViewModel() {
    private val mutableViewState = MutableStateFlow(NextToGoRacesContract.ViewState.INITIAL)
    val viewState: StateFlow<NextToGoRacesContract.ViewState> = mutableViewState.asStateFlow()

    /**
     * A global ticker that is used to keep the race timers in sync.
     */
    private val mutableTicker = MutableSharedFlow<Unit>()
    val ticker: Flow<Unit> = mutableTicker.asSharedFlow()

    private var racesJob: Job? = null

    init {
        nextToGoRacesInteractor.backgroundErrors
            .onEach { error -> handleError(error) }
            .launchIn(viewModelScope)

        startCollectingRaces()

        viewModelScope.launch {
            while (true) {
                mutableTicker.emit(Unit)
                delay(1000) // Run every second
            }
        }
    }

    private fun startCollectingRaces() {
        racesJob?.cancel()

        racesJob = viewModelScope.launch {
            nextToGoRacesInteractor.getNextRaces(
                categories = mutableViewState.value.selectedCategories,
                count = MAX_NUMBER_OF_RACES,
            )
                .distinctUntilChanged()
                .catch { e -> handleError(e) }
                .collect { races ->
                    mutableViewState.update { it.copy(races = races) }
                }
        }
    }

    private fun handleError(throwable: Throwable) {
        // TODO: Possibly respond differently depending on type of error.

        mutableViewState.update { it.copy(showError = true) }
    }

    fun onTryAgainButtonClick() {
        viewModelScope.launch {
            mutableViewState.update { it.copy(showError = false) }
            startCollectingRaces()
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
            it.copy(
                selectedCategories = updatedCategories,
            )
        }

        startCollectingRaces()
    }
}
