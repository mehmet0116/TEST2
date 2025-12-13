package com.example.snakegame

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import timber.log.Timber

class SnakeGameApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Timber logging sadece debug build'de
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // App lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
        
        // Game pre-initialization (arka planda)
        initializeGameResources()
    }
    
    private fun initializeGameResources() {
        // Oyun kaynaklarını önceden yükle
        // Bu, oyun başlangıcında gecikmeyi azaltır
        Thread {
            // Gerekli kaynakları önceden yükle
            Timber.d("Game resources initialized in background")
        }.start()
    }
    
    inner class AppLifecycleObserver : LifecycleObserver {
        
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onAppBackgrounded() {
            Timber.d("App backgrounded - saving game state if needed")
        }
        
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onAppForegrounded() {
            Timber.d("App foregrounded - restoring game state if needed")
        }
    }
}