package com.entaingroup.nexon

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initializeLogging()
    }

    private fun initializeLogging() {
        Timber.plant(Timber.DebugTree())
    }
}
