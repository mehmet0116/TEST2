package com.example.snakegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snakegame.ui.screens.GameOverScreen
import com.example.snakegame.ui.screens.GameScreen
import com.example.snakegame.ui.screens.MainMenuScreen
import com.example.snakegame.ui.screens.ScoreboardScreen
import com.example.snakegame.ui.screens.SettingsScreen
import com.example.snakegame.ui.theme.SnakeGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakeGameTheme {
                // Uygulamanın ana yüzeyi
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SnakeApp()
                }
            }
        }
    }
}

@Composable
fun SnakeApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "main_menu"
    ) {
        composable("main_menu") {
            MainMenuScreen(
                onPlayClicked = { navController.navigate("game") },
                onScoreboardClicked = { navController.navigate("scoreboard") },
                onSettingsClicked = { navController.navigate("settings") },
                onExitClicked = { /* Uygulamadan çıkış - gerçek uygulamada finish() kullanılabilir */ }
            )
        }
        composable("game") {
            GameScreen(
                onGameOver = { score ->
                    navController.navigate("game_over/$score") {
                        popUpTo("game") { inclusive = true }
                    }
                },
                onBackToMenu = { navController.navigate("main_menu") }
            )
        }
        composable("game_over/{score}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            GameOverScreen(
                score = score,
                onPlayAgain = {
                    navController.navigate("game") {
                        popUpTo("game_over") { inclusive = true }
                    }
                },
                onBackToMenu = { navController.navigate("main_menu") }
            )
        }
        composable("scoreboard") {
            ScoreboardScreen(
                onBackClicked = { navController.navigate("main_menu") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackClicked = { navController.navigate("main_menu") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SnakeAppPreview() {
    SnakeGameTheme {
        SnakeApp()
    }
}