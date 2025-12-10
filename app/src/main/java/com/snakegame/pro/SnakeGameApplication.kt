package com.snakegame.pro

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

class SnakeGameApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Firebase'i başlat
        FirebaseApp.initializeApp(this)
        
        // Crashlytics'i etkinleştir (release build'de)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        
        // AdMob'u başlat
        MobileAds.initialize(this) { }
        
        // Uygulama başlangıç log'u
        FirebaseCrashlytics.getInstance().log("Snake Game Pro uygulaması başlatıldı")
    }
}