package com.example.snakegame.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Uygulama ayarlarını yöneten repository
 */
object SettingsRepository {

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
    private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
    private val GAME_SPEED_KEY = floatPreferencesKey("game_speed")
    private val GRID_VISIBLE_KEY = booleanPreferencesKey("grid_visible")

    /**
     * Karanlık mod durumu
     */
    val isDarkMode: Flow<Boolean> = getContext().dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    /**
     * Ses durumu
     */
    val isSoundEnabled: Flow<Boolean> = getContext().dataStore.data
        .map { preferences ->
            preferences[SOUND_ENABLED_KEY] ?: true
        }

    /**
     * Titreşim durumu
     */
    val isVibrationEnabled: Flow<Boolean> = getContext().dataStore.data
        .map { preferences ->
            preferences[VIBRATION_ENABLED_KEY] ?: true
        }

    /**
     * Oyun hızı
     */
    val gameSpeed: Flow<Float> = getContext().dataStore.data
        .map { preferences ->
            preferences[GAME_SPEED_KEY] ?: 150f
        }

    /**
     * Grid görünürlüğü
     */
    val isGridVisible: Flow<Boolean> = getContext().dataStore.data
        .map { preferences ->
            preferences[GRID_VISIBLE_KEY] ?: true
        }

    /**
     * Karanlık modu ayarla
     */
    suspend fun setDarkMode(enabled: Boolean) {
        getContext().dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    /**
     * Sesi aç/kapat
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        getContext().dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }

    /**
     * Titreşimi aç/kapat
     */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        getContext().dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = enabled
        }
    }

    /**
     * Oyun hızını ayarla
     */
    suspend fun setGameSpeed(speed: Float) {
        getContext().dataStore.edit { preferences ->
            preferences[GAME_SPEED_KEY] = speed
        }
    }

    /**
     * Grid görünürlüğünü ayarla
     */
    suspend fun setGridVisible(visible: Boolean) {
        getContext().dataStore.edit { preferences ->
            preferences[GRID_VISIBLE_KEY] = visible
        }
    }

    /**
     * Tüm ayarları sıfırla
     */
    suspend fun resetSettings() {
        getContext().dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun getContext(): Context = AppContextHolder.appContext
}

