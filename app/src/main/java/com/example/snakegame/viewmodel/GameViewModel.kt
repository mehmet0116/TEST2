package com.example.snakegame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snakegame.data.ScoreRepository
import com.example.snakegame.game.Direction
import com.example.snakegame.game.GameState
import com.example.snakegame.game.SnakeGame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val game = SnakeGame()

    // Directly expose the StateFlow from the SnakeGame instance
    val gameState: StateFlow<GameState> = game.gameState

    val highScore: Flow<Int> = ScoreRepository.highScore
    val recentScores = ScoreRepository.recentScores
    val totalGames: Flow<Int> = ScoreRepository.totalGames
    val averageScore = ScoreRepository.averageScore

    init {
        // Game loop
        viewModelScope.launch {
            while (true) {
                val currentGameState = gameState.value
                if (!currentGameState.isPaused && !currentGameState.isGameOver) {
                    game.update()
                    delay(currentGameState.gameSpeed.toLong())
                } else {
                    delay(100) // Prevent busy-waiting when paused or game over
                }
            }
        }
    }

    fun startNewGame() {
        game.resetGame()
    }

    fun changeDirection(direction: Direction) {
        game.setDirection(direction)
    }

    fun togglePause() {
        game.togglePause()
    }

    fun saveScore() {
        viewModelScope.launch {
            ScoreRepository.saveScore(gameState.value.score)
        }
    }

    fun resetAllScores() {
        viewModelScope.launch {
            ScoreRepository.clearAllScores()
        }
    }
}
