package com.example.snakegame

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val INITIAL_SPEED = 150L // ms
        private const val GRID_SIZE = 20
        private const val MIN_SPEED = 50L
    }
    
    // Renkler
    private val snakeColor = Color.GREEN
    private val foodColor = Color.RED
    private val backgroundColor = Color.BLACK
    private val gridColor = Color.DKGRAY
    
    // Oyun değişkenleri
    private var snake = mutableListOf<Point>()
    private var food = Point()
    private var direction = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    private var score = 0
    private var highScore = 0
    private var gameSpeed = INITIAL_SPEED
    private var isGameRunning = false
    private var isGamePaused = false
    private var cellSize = 0
    private var gridWidth = 0
    private var gridHeight = 0
    
    // Çizim araçları
    private val paint = Paint()
    private val handler = Handler(Looper.getMainLooper())
    private val gameLoop = object : Runnable {
        override fun run() {
            if (isGameRunning && !isGamePaused) {
                updateGame()
                invalidate()
                handler.postDelayed(this, gameSpeed)
            }
        }
    }
    
    private var gameStateChangeListener: OnGameStateChangeListener? = null
    
    interface OnGameStateChangeListener {
        fun onScoreChanged(score: Int)
        fun onHighScoreChanged(highScore: Int)
        fun onGameOver()
        fun onGameStarted()
        fun onGamePaused()
    }
    
    fun setOnGameStateChangeListener(listener: OnGameStateChangeListener) {
        this.gameStateChangeListener = listener
    }
    
    init {
        // View hazır olduğunda boyutları al
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                initializeGame()
            }
        })
        
        // Yüksek skoru yükle
        val prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)
        gameStateChangeListener?.onHighScoreChanged(highScore)
    }
    
    private fun initializeGame() {
        gridWidth = width / GRID_SIZE
        gridHeight = height / GRID_SIZE
        cellSize = minOf(width / gridWidth, height / gridHeight)
        
        resetGame()
    }
    
    private fun resetGame() {
        snake.clear()
        // Yılanı ortada başlat
        val startX = gridWidth / 2
        val startY = gridHeight / 2
        snake.add(Point(startX, startY))
        snake.add(Point(startX - 1, startY))
        snake.add(Point(startX - 2, startY))
        
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        score = 0
        gameSpeed = INITIAL_SPEED
        
        generateFood()
        
        gameStateChangeListener?.onScoreChanged(score)
    }
    
    fun startGame() {
        if (!isGameRunning) {
            isGameRunning = true
            isGamePaused = false
            gameStateChangeListener?.onGameStarted()
            handler.post(gameLoop)
        } else if (isGamePaused) {
            isGamePaused = false
            handler.post(gameLoop)
        }
    }
    
    fun pauseGame() {
        if (isGameRunning && !isGamePaused) {
            isGamePaused = true
            gameStateChangeListener?.onGamePaused()
        }
    }
    
    fun restartGame() {
        handler.removeCallbacks(gameLoop)
        resetGame()
        startGame()
    }
    
    fun isGameRunning(): Boolean = isGameRunning && !isGamePaused
    
    private fun updateGame() {
        direction = nextDirection
        
        // Yeni kafa pozisyonu
        val head = snake.first()
        val newHead = Point(head.x, head.y)
        
        when (direction) {
            Direction.UP -> newHead.y--
            Direction.DOWN -> newHead.y++
            Direction.LEFT -> newHead.x--
            Direction.RIGHT -> newHead.x++
        }
        
        // Duvar çarpışması kontrolü
        if (newHead.x < 0 || newHead.x >= gridWidth || 
            newHead.y < 0 || newHead.y >= gridHeight) {
            gameOver()
            return
        }
        
        // Kendine çarpma kontrolü
        if (snake.any { it.x == newHead.x && it.y == newHead.y }) {
            gameOver()
            return
        }
        
        // Yeni kafayı ekle
        snake.add(0, newHead)
        
        // Yem yeme kontrolü
        if (newHead.x == food.x && newHead.y == food.y) {
            score += 10
            gameStateChangeListener?.onScoreChanged(score)
            
            // Hızı artır (minimum hız sınırıyla)
            if (gameSpeed > MIN_SPEED) {
                gameSpeed -= 5
            }
            
            generateFood()
        } else {
            // Yem yenmediyse kuyruğu kısalt
            snake.removeLast()
        }
    }
    
    private fun generateFood() {
        var newFood: Point
        do {
            newFood = Point(
                Random.nextInt(0, gridWidth),
                Random.nextInt(0, gridHeight)
            )
        } while (snake.any { it.x == newFood.x && it.y == newFood.y })
        
        food = newFood
    }
    
    private fun gameOver() {
        isGameRunning = false
        isGamePaused = false
        handler.removeCallbacks(gameLoop)
        
        // Yüksek skoru güncelle
        if (score > highScore) {
            highScore = score
            val prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE)
            prefs.edit().putInt("high_score", highScore).apply()
            gameStateChangeListener?.onHighScoreChanged(highScore)
        }
        
        gameStateChangeListener?.onGameOver()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Arka plan
        canvas.drawColor(backgroundColor)
        
        // Izgara çiz
        paint.color = gridColor
        paint.strokeWidth = 1f
        
        // Dikey çizgiler
        for (i in 0..gridWidth) {
            val x = i * cellSize.toFloat()
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }
        
        // Yatay çizgiler
        for (i in 0..gridHeight) {
            val y = i * cellSize.toFloat()
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
        }
        
        // Yılanı çiz
        paint.color = snakeColor
        for ((index, segment) in snake.withIndex()) {
            val left = segment.x * cellSize.toFloat()
            val top = segment.y * cellSize.toFloat()
            val right = left + cellSize
            val bottom = top + cellSize
            
            // Baş için farklı renk
            if (index == 0) {
                paint.color = Color.GREEN
            } else {
                paint.color = Color.rgb(0, 150, 0)
            }
            
            canvas.drawRect(left, top, right, bottom, paint)
            
            // Hücre kenarları
            paint.color = Color.DKGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(left, top, right, bottom, paint)
            paint.style = Paint.Style.FILL
        }
        
        // Yemi çiz
        paint.color = foodColor
        val foodLeft = food.x * cellSize.toFloat()
        val foodTop = food.y * cellSize.toFloat()
        val foodRight = foodLeft + cellSize
        val foodBottom = foodTop + cellSize
        
        canvas.drawRect(foodLeft, foodTop, foodRight, foodBottom, paint)
        
        // Yem kenarları
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(foodLeft, foodTop, foodRight, foodBottom, paint)
        paint.style = Paint.Style.FILL
        
        // Oyun durumu metni
        if (!isGameRunning && snake.isNotEmpty()) {
            paint.color = Color.WHITE
            paint.textSize = 60f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", width / 2f, height / 2f, paint)
            
            paint.textSize = 40f
            canvas.drawText("Tap to restart", width / 2f, height / 2f + 60, paint)
        } else if (isGamePaused) {
            paint.color = Color.YELLOW
            paint.textSize = 60f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("PAUSED", width / 2f, height / 2f, paint)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (!isGameRunning) {
                restartGame()
                return true
            }
            
            // Kaydırma kontrolleri
            val touchX = event.x
            val touchY = event.y
            
            val centerX = width / 2f
            val centerY = height / 2f
            
            val dx = touchX - centerX
            val dy = touchY - centerY
            
            if (abs(dx) > abs(dy)) {
                // Yatay hareket
                if (dx > 0 && direction != Direction.LEFT) {
                    nextDirection = Direction.RIGHT
                } else if (dx < 0 && direction != Direction.RIGHT) {
                    nextDirection = Direction.LEFT
                }
            } else {
                // Dikey hareket
                if (dy > 0 && direction != Direction.UP) {
                    nextDirection = Direction.DOWN
                } else if (dy < 0 && direction != Direction.DOWN) {
                    nextDirection = Direction.UP
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }
    
    private fun abs(value: Float): Float = if (value < 0) -value else value
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(gameLoop)
    }
    
    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
}