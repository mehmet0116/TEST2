package com.example.snakegame.model

import kotlin.random.Random

class Game(
    private val gridSize: Int = 20,
    private val gameWidth: Int,
    private val gameHeight: Int
) {
    private val snake: Snake
    private var food: Point? = null
    private var score = 0
    private var highScore = 0
    var gameState = GameState.NOT_STARTED
        private set
    
    init {
        snake = Snake(gridSize, gameWidth, gameHeight)
        generateFood()
    }
    
    fun start() {
        if (gameState == GameState.NOT_STARTED || gameState == GameState.GAME_OVER) {
            reset()
        }
        gameState = GameState.RUNNING
    }
    
    fun pause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED
        }
    }
    
    fun resume() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING
        }
    }
    
    fun restart() {
        reset()
        gameState = GameState.RUNNING
    }
    
    private fun reset() {
        snake.reset()
        score = 0
        generateFood()
        gameState = GameState.RUNNING
    }
    
    fun update(): Boolean {
        if (gameState != GameState.RUNNING) return true
        
        // Move snake
        val moved = snake.move()
        if (!moved) {
            gameState = GameState.GAME_OVER
            updateHighScore()
            return false
        }
        
        // Check food collision
        val head = snake.getHead()
        food?.let { foodPoint ->
            if (head == foodPoint) {
                snake.grow()
                score += 10
                generateFood()
            } else {
                snake.shrink()
            }
        }
        
        return true
    }
    
    private fun generateFood() {
        val emptyCells = mutableListOf<Point>()
        val snakeBody = snake.getBody().toSet()
        
        // Find all empty cells
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val point = Point(x, y)
                if (!snakeBody.contains(point)) {
                    emptyCells.add(point)
                }
            }
        }
        
        if (emptyCells.isNotEmpty()) {
            food = emptyCells[Random.nextInt(emptyCells.size)]
        } else {
            food = null
        }
    }
    
    fun getSnake(): Snake = snake
    
    fun getFood(): Point? = food
    
    fun getScore(): Int = score
    
    fun getHighScore(): Int = highScore
    
    private fun updateHighScore() {
        if (score > highScore) {
            highScore = score
        }
    }
    
    fun setDirection(direction: Direction) {
        snake.setDirection(direction)
    }
    
    fun getGridSize(): Int = gridSize
}