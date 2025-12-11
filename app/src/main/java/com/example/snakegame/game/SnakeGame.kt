package com.example.snakegame.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class Position(val x: Int, val y: Int)

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

class SnakeGame(
    private val gridWidth: Int = 20,
    private val gridHeight: Int = 20
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private var currentDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    
    private val initialSnake = listOf(
        Position(gridWidth / 2, gridHeight / 2),
        Position(gridWidth / 2 - 1, gridHeight / 2),
        Position(gridWidth / 2 - 2, gridHeight / 2)
    )
    
    init {
        resetGame()
    }
    
    fun resetGame() {
        val newFood = generateFood(initialSnake)
        _gameState.value = GameState(
            snake = initialSnake.toMutableList(),
            food = newFood,
            score = 0,
            isGameOver = false,
            isPaused = false
        )
        currentDirection = Direction.RIGHT
        nextDirection = Direction.RIGHT
    }
    
    fun setDirection(direction: Direction) {
        // Yön değişikliği için geçersiz hareketleri engelle (ters yöne gitme)
        when (direction) {
            Direction.UP -> if (currentDirection != Direction.DOWN) nextDirection = direction
            Direction.DOWN -> if (currentDirection != Direction.UP) nextDirection = direction
            Direction.LEFT -> if (currentDirection != Direction.RIGHT) nextDirection = direction
            Direction.RIGHT -> if (currentDirection != Direction.LEFT) nextDirection = direction
        }
    }
    
    fun update() {
        val currentState = _gameState.value
        if (currentState.isGameOver || currentState.isPaused) return
        
        // Yönü güncelle
        currentDirection = nextDirection
        
        val head = currentState.snake.first()
        val newHead = when (currentDirection) {
            Direction.UP -> Position(head.x, head.y - 1)
            Direction.DOWN -> Position(head.x, head.y + 1)
            Direction.LEFT -> Position(head.x - 1, head.y)
            Direction.RIGHT -> Position(head.x + 1, head.y)
        }
        
        // Oyun alanı sınırlarını kontrol et (duvarlardan geçme)
        if (newHead.x < 0 || newHead.x >= gridWidth || 
            newHead.y < 0 || newHead.y >= gridHeight) {
            _gameState.value = currentState.copy(isGameOver = true)
            return
        }
        
        // Kendine çarpma kontrolü
        if (currentState.snake.any { it.x == newHead.x && it.y == newHead.y }) {
            _gameState.value = currentState.copy(isGameOver = true)
            return
        }
        
        val newSnake = mutableListOf<Position>()
        newSnake.add(newHead)
        newSnake.addAll(currentState.snake)
        
        // Yemek yeme kontrolü
        var newFood = currentState.food
        var newScore = currentState.score
        var foodEaten = false
        
        if (newHead.x == currentState.food.x && newHead.y == currentState.food.y) {
            // Yemek yendi, yılan büyümesin (baş ekledik, kuyruğu çıkarmayacağız)
            newScore += 10
            newFood = generateFood(newSnake)
            foodEaten = true
        } else {
            // Yemek yenmedi, yılanın kuyruğunu çıkar
            newSnake.removeLast()
        }
        
        _gameState.value = currentState.copy(
            snake = newSnake,
            food = newFood,
            score = newScore,
            foodEaten = foodEaten
        )
    }
    
    fun togglePause() {
        _gameState.value = _gameState.value.copy(isPaused = !_gameState.value.isPaused)
    }
    
    private fun generateFood(snake: List<Position>): Position {
        val availablePositions = mutableListOf<Position>()
        
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val pos = Position(x, y)
                if (snake.none { it.x == pos.x && it.y == pos.y }) {
                    availablePositions.add(pos)
                }
            }
        }
        
        return if (availablePositions.isNotEmpty()) {
            availablePositions.random()
        } else {
            Position(0, 0) // Fallback, boş pozisyon yoksa
        }
    }
    
    fun getGridWidth(): Int = gridWidth
    fun getGridHeight(): Int = gridHeight
}

data class GameState(
    val snake: List<Position> = emptyList(),
    val food: Position = Position(0, 0),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val foodEaten: Boolean = false
)