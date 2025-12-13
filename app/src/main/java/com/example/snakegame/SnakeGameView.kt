package com.example.snakegame

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class SnakeGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val GRID_SIZE = 20
        private const val INITIAL_SPEED = 150L // ms
        private const val MIN_SPEED = 50L
        private const val SPEED_INCREMENT = 5L
        private const val HIGH_SCORE_KEY = "high_score"
    }
    
    // Game state
    private var isGameRunning = false
    var isGamePaused = false
        private set
    private var score = 0
    private var highScore = 0
    
    // Snake properties
    private val snake = mutableListOf<Point>()
    private var snakeDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    
    // Food properties
    private var food = Point(0, 0)
    
    // Game grid
    private var gridWidth = 0
    private var gridHeight = 0
    private var cellSize = 0
    
    // Paints
    private val snakePaint = Paint().apply {
        color = context.getColor(R.color.snake_color)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val foodPaint = Paint().apply {
        color = context.getColor(R.color.food_color)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val gridPaint = Paint().apply {
        color = context.getColor(R.color.grid_color)
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }
    
    // Game loop
    private val handler = Handler(Looper.getMainLooper())
    private val gameLoop = object : Runnable {
        override fun run() {
            if (isGameRunning && !isGamePaused) {
                updateGame()
                invalidate()
            }
            handler.postDelayed(this, getCurrentSpeed())
        }
    }
    
    // Callbacks
    var onScoreChanged: ((Int) -> Unit)? = null
    var onGameOver: ((Int) -> Unit)? = null
    
    // Shared preferences for high score
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("snake_game_prefs", Context.MODE_PRIVATE)
    }
    
    init {
        loadHighScore()
        initializeGame()
        startGame()
    }
    
    private fun initializeGame() {
        // Calculate grid dimensions
        gridWidth = GRID_SIZE
        gridHeight = GRID_SIZE
        
        // Initialize snake in the middle
        val startX = gridWidth / 2
        val startY = gridHeight / 2
        snake.clear()
        snake.add(Point(startX, startY))
        snake.add(Point(startX - 1, startY))
        snake.add(Point(startX - 2, startY))
        
        // Generate first food
        generateFood()
        
        // Reset game state
        score = 0
        snakeDirection = Direction.RIGHT
        nextDirection = Direction.RIGHT
        isGameRunning = true
        isGamePaused = false
    }
    
    private fun generateFood() {
        val availablePoints = mutableListOf<Point>()
        
        // Generate all possible points
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val point = Point(x, y)
                if (!snake.contains(point)) {
                    availablePoints.add(point)
                }
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            food = availablePoints.random()
        }
    }
    
    private fun updateGame() {
        // Update direction
        snakeDirection = nextDirection
        
        // Calculate new head position
        val head = snake.first()
        val newHead = when (snakeDirection) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }
        
        // Check collision with walls
        if (newHead.x < 0 || newHead.x >= gridWidth || 
            newHead.y < 0 || newHead.y >= gridHeight) {
            gameOver()
            return
        }
        
        // Check collision with self
        if (snake.contains(newHead)) {
            gameOver()
            return
        }
        
        // Add new head
        snake.add(0, newHead)
        
        // Check if food is eaten
        if (newHead == food) {
            score++
            onScoreChanged?.invoke(score)
            generateFood()
            
            // Increase speed every 5 points
            if (score % 5 == 0) {
                // Speed already handled by getCurrentSpeed()
            }
        } else {
            // Remove tail if no food eaten
            snake.removeLast()
        }
    }
    
    private fun gameOver() {
        isGameRunning = false
        handler.removeCallbacks(gameLoop)
        
        if (score > highScore) {
            highScore = score
            saveHighScore()
        }
        
        onGameOver?.invoke(score)
    }
    
    fun restartGame() {
        handler.removeCallbacks(gameLoop)
        initializeGame()
        startGame()
        invalidate()
    }
    
    fun pauseGame() {
        isGamePaused = true
    }
    
    fun resumeGame() {
        isGamePaused = false
        if (!isGameRunning) {
            isGameRunning = true
            startGame()
        }
    }
    
    private fun startGame() {
        handler.post(gameLoop)
    }
    
    private fun getCurrentSpeed(): Long {
        return maxOf(MIN_SPEED, INITIAL_SPEED - (score / 5) * SPEED_INCREMENT)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Calculate cell size based on view dimensions
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        
        cellSize = minOf(viewWidth / gridWidth, viewHeight / gridHeight).toInt()
        
        // Draw grid
        for (x in 0..gridWidth) {
            val lineX = x * cellSize.toFloat()
            canvas.drawLine(lineX, 0f, lineX, gridHeight * cellSize.toFloat(), gridPaint)
        }
        
        for (y in 0..gridHeight) {
            val lineY = y * cellSize.toFloat()
            canvas.drawLine(0f, lineY, gridWidth * cellSize.toFloat(), lineY, gridPaint)
        }
        
        // Draw snake
        for ((index, point) in snake.withIndex()) {
            val left = point.x * cellSize.toFloat()
            val top = point.y * cellSize.toFloat()
            val right = left + cellSize
            val bottom = top + cellSize
            
            // Make head slightly different
            if (index == 0) {
                snakePaint.color = Color.GREEN
            } else {
                snakePaint.color = context.getColor(R.color.snake_color)
            }
            
            canvas.drawRoundRect(
                RectF(left + 2, top + 2, right - 2, bottom - 2),
                8f, 8f, snakePaint
            )
        }
        
        // Draw food
        val foodLeft = food.x * cellSize.toFloat()
        val foodTop = food.y * cellSize.toFloat()
        val foodRight = foodLeft + cellSize
        val foodBottom = foodTop + cellSize
        
        canvas.drawRoundRect(
            RectF(foodLeft + 4, foodTop + 4, foodRight - 4, foodBottom - 4),
            12f, 12f, foodPaint
        )
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !isGamePaused) {
            val x = event.x
            val y = event.y
            
            // Calculate swipe direction
            val centerX = width / 2f
            val centerY = height / 2f
            
            val dx = x - centerX
            val dy = y - centerY
            
            // Determine swipe direction based on larger movement
            if (abs(dx) > abs(dy)) {
                // Horizontal swipe
                if (dx > 0 && snakeDirection != Direction.LEFT) {
                    nextDirection = Direction.RIGHT
                } else if (dx < 0 && snakeDirection != Direction.RIGHT) {
                    nextDirection = Direction.LEFT
                }
            } else {
                // Vertical swipe
                if (dy > 0 && snakeDirection != Direction.UP) {
                    nextDirection = Direction.DOWN
                } else if (dy < 0 && snakeDirection != Direction.DOWN) {
                    nextDirection = Direction.UP
                }
            }
            
            return true
        }
        
        return super.onTouchEvent(event)
    }
    
    private fun loadHighScore() {
        highScore = prefs.getInt(HIGH_SCORE_KEY, 0)
    }
    
    private fun saveHighScore() {
        prefs.edit().putInt(HIGH_SCORE_KEY, highScore).apply()
    }
    
    fun getHighScore(): Int = highScore
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(gameLoop)
    }
    
    data class Point(val x: Int, val y: Int)
    
    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
}