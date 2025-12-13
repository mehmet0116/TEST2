package com.example.snakegame

import android.app.Application
import com.example.snakegame.data.AppContextHolder
import timber.log.Timber

class SnakeGameApplication : Application() {
    
    companion object {
        lateinit var instance: SnakeGameApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this

        // AppContextHolder'ı initialize et
        AppContextHolder.appContext = this

        // Timber'ı başlat
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("SnakeGameApplication started")
    }
}