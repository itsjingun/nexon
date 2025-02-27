package com.entaingroup.nexon.nexttogo.data

import android.util.Log
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import javax.inject.Inject

internal class DefaultNextToGoRacesRepository @Inject constructor() : NextToGoRacesRepository {
    override fun doNothing() {
        Log.d("NextToGo", "Nothing has been done!")
    }
}
