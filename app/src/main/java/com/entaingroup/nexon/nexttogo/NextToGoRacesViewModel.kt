package com.entaingroup.nexon.nexttogo

import androidx.lifecycle.ViewModel
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class NextToGoRacesViewModel @Inject constructor(
    private val nextToGoRacesRepository: NextToGoRacesRepository,
) : ViewModel() {

    fun doNothing() {
        nextToGoRacesRepository.doNothing()
    }
}
