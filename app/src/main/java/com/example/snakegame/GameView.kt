package com.example.snakegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val GRID_SIZE = 20
        private const val INITIAL_SNAKE_LENGTH = 3
        private const val INITIAL_SPEED = 150L
    }
    
    private var gridWidth = 0
    private var gridHeight = 0
    private var cellSize = 0
    
    private val snake = mutableListOf<Pair<Int, Int>>()
    private var food = Pair(0, 0)
    private var direction = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    
    private var score = 0
    private var highScore = 0
    
    private val snakePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.snake_color)
        style = Paint.Style.FILL
    }
    
    private val foodPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.food_color)
        style = Paint.Style.FILL
    }
    
    private val gridPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.grid_color)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.background_color)
        style = Paint.Style.FILL
    }
    
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    
    init {
        resetGame()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gridWidth = GRID_SIZE
        gridHeight = GRID_SIZE
        cellSize = minOf(w / gridWidth, h / gridHeight)
        resetGame()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Draw grid
        for (i in 0..gridWidth) {
            val x = i * cellSize
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
        }
        
        for (i in 0..gridHeight) {
            val y = i * cellSize
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
        }
        
        // Draw snake
        for ((x, y) in snake) {
            val left = x * cellSize.toFloat()
            val top = y * cellSize.toFloat()
            val right = left + cellSize
            val bottom = top + cellSize
            
            canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, snakePaint)
        }
        
        // Draw food
        val foodLeft = food.first * cellSize.toFloat()
        val foodTop = food.second * cellSize.toFloat()
        val foodRight = foodLeft + cellSize
        val foodBottom = foodTop + cellSize
        
        canvas.drawCircle(
            foodLeft + cellSize / 2,
            foodTop + cellSize / 2,
            cellSize / 2f - 4f,
            foodPaint
        )
    }
    
    fun update() {
        direction = nextDirection
        
        // Move snake
        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> Pair(head.first, head.second - 1)
            Direction.DOWN -> Pair(head.first, head.second + 1)
            Direction.LEFT -> Pair(head.first - 1, head.second)
            Direction.RIGHT -> Pair(head.first + 1, head.second)
        }
        
        // Check collision with walls
        if (newHead.first < 0 || newHead.first >= gridWidth ||
            newHead.second < 0 || newHead.second >= gridHeight) {
            return
        }
        
        // Check collision with self
        if (snake.contains(newHead)) {
            return
        }
        
        snake.add(0, newHead)
        
        // Check if food is eaten
        if (newHead == food) {
            score += 10
            if (score > highScore) {
                highScore = score
            }
            generateFood()
        } else {
            snake.removeLast()
        }
        
        invalidate()
    }
    
    private fun generateFood() {
        val availableCells = mutableListOf<Pair<Int, Int>>()
        
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val cell = Pair(x, y)
                if (!snake.contains(cell)) {
                    availableCells.add(cell)
                }
            }
        }
        
        if (availableCells.isNotEmpty()) {
            food = availableCells.random()
        }
    }
    
    fun handleSwipe(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - lastTouchX
                val deltaY = event.y - lastTouchY
                
                // Determine swipe direction
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // Horizontal swipe
                    if (deltaX > 0 && direction != Direction.LEFT) {
                        nextDirection = Direction.RIGHT
                    } else if (deltaX < 0 && direction != Direction.RIGHT) {
                        nextDirection = Direction.LEFT
                    }
                } else {
                    // Vertical swipe
                    if (deltaY > 0 && direction != Direction.UP) {
                        nextDirection = Direction.DOWN
                    } else if (deltaY < 0 && direction != Direction.DOWN) {
                        nextDirection = Direction.UP
                    }
                }
                return true
            }
        }
        return false
    }
    
    fun resetGame() {
        snake.clear()
        score = 0
        
        // Initialize snake in the middle
        val startX = gridWidth / 2
        val startY = gridHeight / 2
        
        for (i in 0 until INITIAL_SNAKE_LENGTH) {
            snake.add(Pair(startX - i, startY))
        }
        
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        
        generateFood()
        invalidate()
    }
    
    fun getScore(): Int = score
    fun getHighScore(): Int = highScore
    fun isGameOver(): Boolean {
        val head = snake.first()
        
        // Check wall collision
        if (head.first < 0 || head.first >= gridWidth ||
            head.second < 0 || head.second >= gridHeight) {
            return true
        }
        
        // Check self collision (skip head)
        return snake.subList(1, snake.size).contains(head)
    }
    
    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
}