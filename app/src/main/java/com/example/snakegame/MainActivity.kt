package com.example.snakegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snakegame.data.SettingsRepository
import com.example.snakegame.ui.navigation.SnakeGameNavigation
import com.example.snakegame.ui.theme.SnakeGameTheme
import com.example.snakegame.viewmodel.GameViewModel
import timber.log.Timber

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Timber'ı başlat
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setContent {
            // Tema ayarını al
            val isDarkMode by SettingsRepository.isDarkMode.collectAsState(initial = false)

            SnakeGameTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: GameViewModel = viewModel()
                    SnakeGameNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

