package com.example.snakegame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.snakegame.data.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val isDarkMode: Flow<Boolean> = SettingsRepository.isDarkMode
    val isSoundEnabled: Flow<Boolean> = SettingsRepository.isSoundEnabled
    val isVibrationEnabled: Flow<Boolean> = SettingsRepository.isVibrationEnabled
    val gameSpeed: Flow<Float> = SettingsRepository.gameSpeed
    val isGridVisible: Flow<Boolean> = SettingsRepository.isGridVisible

    suspend fun setDarkMode(enabled: Boolean) {
        SettingsRepository.setDarkMode(enabled)
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        SettingsRepository.setSoundEnabled(enabled)
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        SettingsRepository.setVibrationEnabled(enabled)
    }

    suspend fun setGameSpeed(speed: Float) {
        SettingsRepository.setGameSpeed(speed)
    }

    suspend fun setGridVisible(visible: Boolean) {
        SettingsRepository.setGridVisible(visible)
    }

    suspend fun resetSettings() {
        SettingsRepository.resetSettings()
    }
}

