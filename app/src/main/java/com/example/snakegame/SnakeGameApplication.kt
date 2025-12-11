package com.example.snakegame

import android.app.Application
import com.example.snakegame.data.AppContextHolder

class SnakeGameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Context'i DataStore için ayarla
        AppContextHolder.appContext = applicationContext
    }
}