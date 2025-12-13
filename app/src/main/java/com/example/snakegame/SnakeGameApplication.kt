package com.example.snakegame

import android.app.Application

class SnakeGameApplication : Application() {
    
    companion object {
        lateinit var instance: SnakeGameApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}