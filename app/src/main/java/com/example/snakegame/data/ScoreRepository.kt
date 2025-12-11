package com.example.snakegame.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore için Context extension
val Context.dataStore by preferencesDataStore(name = "snake_game_preferences")

object ScoreRepository {
    private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
    
    // En yüksek skoru kaydet
    suspend fun saveScore(score: Int) {
        val context = AppContextHolder.appContext
        context.dataStore.edit { preferences ->
            val currentHighScore = preferences[HIGH_SCORE_KEY] ?: 0
            if (score > currentHighScore) {
                preferences[HIGH_SCORE_KEY] = score
            }
        }
    }
    
    // En yüksek skoru getir
    val highScore: Flow<Int> = AppContextHolder.appContext.dataStore.data
        .map { preferences ->
            preferences[HIGH_SCORE_KEY] ?: 0
        }
    
    // Son skorları getir (basit bir implementasyon - gerçek uygulamada Room kullanılabilir)
    suspend fun getRecentScores(limit: Int = 10): List<Int> {
        // Bu basit versiyonda sadece en yüksek skoru döndürüyoruz
        // Gerçek uygulamada tüm skorları kaydedip sıralayabilirsiniz
        return listOf(getCurrentHighScore())
    }
    
    private suspend fun getCurrentHighScore(): Int {
        return AppContextHolder.appContext.dataStore.data
            .map { preferences -> preferences[HIGH_SCORE_KEY] ?: 0 }
            .firstOrNull() ?: 0
    }
}

// Application context'i tutmak için helper object
object AppContextHolder {
    lateinit var appContext: android.content.Context
}