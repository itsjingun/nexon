package com.entaingroup.nexon.nexttogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
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

    private val mutableViewState = MutableStateFlow(NextToGoRacesContract.ViewState.Initial)
    val viewState: StateFlow<NextToGoRacesContract.ViewState> = mutableViewState.asStateFlow()

    /**
     * A global ticker that is used to keep the race countdowns in sync.
     */
    private val mutableTicker = MutableSharedFlow<Unit>()
    val ticker: Flow<Unit> = mutableTicker.asSharedFlow()

    init {
        viewModelScope.launch {
            while (true) {
                mutableTicker.emit(Unit)
                delay(1000) // Update every second
            }
        }
    }

    fun fetchNextRaces() {
        mutableViewState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // TODO: Handle error with try/catch block.
            val races = nextToGoRacesRepository.fetchNextRaces(count = 10)

            mutableViewState.update {
                it.copy(
                    isLoading = false,
                    races = races,
                )
            }
        }
    }
}
