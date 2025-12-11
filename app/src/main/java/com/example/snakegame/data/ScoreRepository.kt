package com.example.snakegame.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// DataStore için Context extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "snake_game_preferences")

/**
 * Skor veri sınıfı
 */
data class ScoreEntry(
    val score: Int,
    val date: Long = System.currentTimeMillis(),
    val level: Int = 1
) : Comparable<ScoreEntry> {
    override fun compareTo(other: ScoreEntry): Int {
        return other.score.compareTo(score) // Yüksek skor önce gelir
    }
}

/**
 * Skor yönetimi için repository sınıfı
 */
object ScoreRepository {
    private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
    private val RECENT_SCORES_KEY = stringPreferencesKey("recent_scores")
    private val TOTAL_GAMES_KEY = intPreferencesKey("total_games")
    private val TOTAL_SCORE_KEY = intPreferencesKey("total_score")
    
    private val gson = Gson()
    private val scoreListType = object : TypeToken<List<ScoreEntry>>() {}.type
    
    /**
     * Skoru kaydet
     */
    suspend fun saveScore(score: Int) {
        val context = AppContextHolder.appContext
        context.dataStore.edit { preferences ->
            // En yüksek skoru güncelle
            val currentHighScore = preferences[HIGH_SCORE_KEY] ?: 0
            if (score > currentHighScore) {
                preferences[HIGH_SCORE_KEY] = score
            }
            
            // Son skorları güncelle
            val recentScores = getRecentScoresFromPreferences(preferences)
            val newScoreEntry = ScoreEntry(score)
            val updatedScores = (listOf(newScoreEntry) + recentScores)
                .sortedDescending()
                .take(10) // Son 10 skoru sakla
            
            preferences[RECENT_SCORES_KEY] = gson.toJson(updatedScores)
            
            // İstatistikleri güncelle
            val totalGames = (preferences[TOTAL_GAMES_KEY] ?: 0) + 1
            val totalScore = (preferences[TOTAL_SCORE_KEY] ?: 0) + score
            
            preferences[TOTAL_GAMES_KEY] = totalGames
            preferences[TOTAL_SCORE_KEY] = totalScore
        }
    }
    
    /**
     * En yüksek skoru getir
     */
    val highScore: Flow<Int> = AppContextHolder.appContext.dataStore.data
        .map { preferences ->
            preferences[HIGH_SCORE_KEY] ?: 0
        }
    
    /**
     * Son skorları getir
     */
    val recentScores: Flow<List<ScoreEntry>> = AppContextHolder.appContext.dataStore.data
        .map { preferences ->
            getRecentScoresFromPreferences(preferences)
        }
    
    /**
     * Toplam oyun sayısını getir
     */
    val totalGames: Flow<Int> = AppContextHolder.appContext.dataStore.data
        .map { preferences ->
            preferences[TOTAL_GAMES_KEY] ?: 0
        }
    
    /**
     * Ortalama skoru getir
     */
    val averageScore: Flow<Float> = AppContextHolder.appContext.dataStore.data
        .map { preferences ->
            val totalGames = preferences[TOTAL_GAMES_KEY] ?: 0
            val totalScore = preferences[TOTAL_SCORE_KEY] ?: 0
            
            if (totalGames > 0) {
                totalScore.toFloat() / totalGames
            } else {
                0f
            }
        }
    
    /**
     * Tüm skorları temizle
     */
    suspend fun clearAllScores() {
        val context = AppContextHolder.appContext
        context.dataStore.edit { preferences ->
            preferences.remove(HIGH_SCORE_KEY)
            preferences.remove(RECENT_SCORES_KEY)
            preferences.remove(TOTAL_GAMES_KEY)
            preferences.remove(TOTAL_SCORE_KEY)
        }
    }
    
    /**
     * Preferences'tan son skorları al
     */
    private fun getRecentScoresFromPreferences(preferences: Preferences): List<ScoreEntry> {
        val scoresJson = preferences[RECENT_SCORES_KEY]
        return if (scoresJson != null) {
            try {
                gson.fromJson(scoresJson, scoreListType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Skor seviyesini belirle
     */
    fun getScoreLevel(score: Int): String {
        return when {
            score >= 500 -> "🏆 Efsanevi"
            score >= 300 -> "⭐ Profesyonel"
            score >= 200 -> "🎯 Usta"
            score >= 100 -> "👍 İyi"
            score >= 50 -> "😊 Orta"
            else -> "🐍 Başlangıç"
        }
    }
    
    /**
     * Tarihi formatla
     */
    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        return android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", date).toString()
    }
}

/**
 * Application context'i tutmak için helper object
 */
object AppContextHolder {
    lateinit var appContext: android.content.Context
}