package com.entaingroup.nexon.nexttogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class NextToGoRacesViewModel @Inject constructor(
    private val nextToGoRacesRepository: NextToGoRacesRepository,
) : ViewModel() {

    fun fetchNextRaces() {
        viewModelScope.launch {
            // TODO: Handle error with try/catch block.
            val races = nextToGoRacesRepository.fetchNextRaces(count = 5)

            Timber.d(races.toString())

            // TODO: Update view state.
        }
    }
}
