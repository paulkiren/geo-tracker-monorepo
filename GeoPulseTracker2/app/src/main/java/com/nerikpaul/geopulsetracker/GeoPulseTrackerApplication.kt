package com.nerikpaul.geopulsetracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Hilt dependency injection
 * This triggers Hilt's code generation and sets up the dependency graph
 */
@HiltAndroidApp
class GeoPulseTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Additional initialization can be added here
    }
}
