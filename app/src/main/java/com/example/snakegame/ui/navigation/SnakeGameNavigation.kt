package com.example.snakegame.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snakegame.ui.screens.GameOverScreen
import com.example.snakegame.ui.screens.GameScreen
import com.example.snakegame.ui.screens.MainMenuScreen
import com.example.snakegame.ui.screens.ScoreboardScreen
import com.example.snakegame.ui.screens.SettingsScreen
import com.example.snakegame.viewmodel.GameViewModel
import kotlin.system.exitProcess

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Game : Screen("game")
    object GameOver : Screen("game_over")
    object Scoreboard : Screen("scoreboard")
    object Settings : Screen("settings")
}

@Composable
fun SnakeGameNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    var showExitDialog by remember { mutableStateOf(false) }

    // Ana menüde geri tuşuna basıldığında çıkış dialogunu göster
    BackHandler(enabled = navController.currentBackStackEntry?.destination?.route == Screen.MainMenu.route) {
        showExitDialog = true
    }

    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route
    ) {
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onPlayClicked = {
                    viewModel.startNewGame()
                    navController.navigate(Screen.Game.route)
                },
                onScoreboardClicked = {
                    navController.navigate(Screen.Scoreboard.route)
                },
                onSettingsClicked = {
                    navController.navigate(Screen.Settings.route)
                },
                onExitClicked = {
                    showExitDialog = true
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(Screen.GameOver.route) {
            GameOverScreen(
                score = 0,
                onPlayAgain = {
                    navController.popBackStack()
                    viewModel.startNewGame()
                    navController.navigate(Screen.Game.route)
                },
                onBackToMenu = {
                    navController.popBackStack(Screen.MainMenu.route, inclusive = false)
                }
            )
        }

        composable(Screen.Scoreboard.route) {
            ScoreboardScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
    }

    // Çıkış Dialogu
    if (showExitDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { androidx.compose.material3.Text("Uygulamadan Çık") },
            text = { androidx.compose.material3.Text("Uygulamayı kapatmak istediğinizden emin misiniz?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        exitProcess(0)
                    }
                ) {
                    androidx.compose.material3.Text(
                        "Çık",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    androidx.compose.material3.Text("İptal")
                }
            }
        )
    }
}
