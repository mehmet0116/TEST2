package com.example.snakegame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.snakegame.model.Direction
import com.example.snakegame.model.Game
import com.example.snakegame.model.GameState
import kotlin.math.abs

class SnakeGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private lateinit var game: Game
    private val paint = Paint()
    private var cellSize = 0f
    private var gridOffsetX = 0f
    private var gridOffsetY = 0f
    
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    
    init {
        setupPaint()
    }
    
    private fun setupPaint() {
        paint.isAntiAlias = true
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initializeGame(w, h)
    }
    
    private fun initializeGame(width: Int, height: Int) {
        val gridSize = 20
        val minDimension = minOf(width, height) - 32 // Account for margins
        cellSize = minDimension / gridSize.toFloat()
        
        // Center the grid
        gridOffsetX = (width - gridSize * cellSize) / 2
        gridOffsetY = (height - gridSize * cellSize) / 2
        
        game = Game(gridSize, width, height)
    }
    
    fun getGame(): Game = game
    
    fun update() {
        if (game.gameState == GameState.RUNNING) {
            game.update()
            invalidate()
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        drawGrid(canvas)
        drawSnake(canvas)
        drawFood(canvas)
        drawGameState(canvas)
    }
    
    private fun drawGrid(canvas: Canvas) {
        val gridSize = game.getGridSize()
        
        // Draw background
        paint.color = Color.parseColor("#2E7D32")
        canvas.drawRect(
            gridOffsetX,
            gridOffsetY,
            gridOffsetX + gridSize * cellSize,
            gridOffsetY + gridSize * cellSize,
            paint
        )
        
        // Draw grid lines
        paint.color = Color.parseColor("#81C784")
        paint.strokeWidth = 1f
        
        // Vertical lines
        for (i in 0..gridSize) {
            val x = gridOffsetX + i * cellSize
            canvas.drawLine(
                x, gridOffsetY,
                x, gridOffsetY + gridSize * cellSize,
                paint
            )
        }
        
        // Horizontal lines
        for (i in 0..gridSize) {
            val y = gridOffsetY + i * cellSize
            canvas.drawLine(
                gridOffsetX, y,
                gridOffsetX + gridSize * cellSize, y,
                paint
            )
        }
    }
    
    private fun drawSnake(canvas: Canvas) {
        val snake = game.getSnake()
        val body = snake.getBody()
        
        // Draw snake body
        paint.color = Color.parseColor("#FFEB3B")
        for (point in body) {
            val left = gridOffsetX + point.x * cellSize + 2
            val top = gridOffsetY + point.y * cellSize + 2
            val right = left + cellSize - 4
            val bottom = top + cellSize - 4
            
            canvas.drawRoundRect(
                left, top, right, bottom,
                cellSize / 4, cellSize / 4,
                paint
            )
        }
        
        // Draw snake eyes on head
        val head = snake.getHead()
        paint.color = Color.BLACK
        val headCenterX = gridOffsetX + head.x * cellSize + cellSize / 2
        val headCenterY = gridOffsetY + head.y * cellSize + cellSize / 2
        
        val eyeRadius = cellSize / 8
        val eyeOffset = cellSize / 4
        
        when (snake.direction) {
            Direction.UP -> {
                canvas.drawCircle(headCenterX - eyeOffset, headCenterY - eyeOffset, eyeRadius, paint)
                canvas.drawCircle(headCenterX + eyeOffset, headCenterY - eyeOffset, eyeRadius, paint)
            }
            Direction.DOWN -> {
                canvas.drawCircle(headCenterX - eyeOffset, headCenterY + eyeOffset, eyeRadius, paint)
                canvas.drawCircle(headCenterX + eyeOffset, headCenterY + eyeOffset, eyeRadius, paint)
            }
            Direction.LEFT -> {
                canvas.drawCircle(headCenterX - eyeOffset, headCenterY - eyeOffset, eyeRadius, paint)
                canvas.drawCircle(headCenterX - eyeOffset, headCenterY + eyeOffset, eyeRadius, paint)
            }
            Direction.RIGHT -> {
                canvas.drawCircle(headCenterX + eyeOffset, headCenterY - eyeOffset, eyeRadius, paint)
                canvas.drawCircle(headCenterX + eyeOffset, headCenterY + eyeOffset, eyeRadius, paint)
            }
        }
    }
    
    private fun drawFood(canvas: Canvas) {
        val food = game.getFood() ?: return
        
        paint.color = Color.parseColor("#F44336")
        val centerX = gridOffsetX + food.x * cellSize + cellSize / 2
        val centerY = gridOffsetY + food.y * cellSize + cellSize / 2
        val radius = cellSize / 2 - 4
        
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Draw shine effect
        paint.color = Color.parseColor("#FFCDD2")
        canvas.drawCircle(centerX - radius/3, centerY - radius/3, radius/4, paint)
    }
    
    private fun drawGameState(canvas: Canvas) {
        when (game.gameState) {
            GameState.GAME_OVER -> drawTextCentered(canvas, "Game Over!", Color.RED, 48f)
            GameState.PAUSED -> drawTextCentered(canvas, "Paused", Color.YELLOW, 36f)
            else -> {}
        }
    }
    
    private fun drawTextCentered(canvas: Canvas, text: String, color: Int, textSize: Float) {
        paint.color = color
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        paint.style = Paint.Style.FILL
        
        val x = width / 2f
        val y = height / 2f
        
        // Draw text background
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val padding = 20f
        
        paint.color = Color.parseColor("#80000000")
        canvas.drawRoundRect(
            x - textBounds.width() / 2f - padding,
            y - textBounds.height() / 2f - padding,
            x + textBounds.width() / 2f + padding,
            y + textBounds.height() / 2f + padding,
            16f, 16f, paint
        )
        
        // Draw text
        paint.color = color
        canvas.drawText(text, x, y + textBounds.height() / 2f, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - lastTouchX
                val deltaY = event.y - lastTouchY
                
                // Determine swipe direction based on larger movement
                if (abs(deltaX) > abs(deltaY)) {
                    // Horizontal swipe
                    if (deltaX > 0) {
                        game.setDirection(Direction.RIGHT)
                    } else {
                        game.setDirection(Direction.LEFT)
                    }
                } else {
                    // Vertical swipe
                    if (deltaY > 0) {
                        game.setDirection(Direction.DOWN)
                    } else {
                        game.setDirection(Direction.UP)
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}