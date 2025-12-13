package com.example.snakegame

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import java.util.*
import kotlin.math.abs

class SnakeGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), OnTouchListener {
    
    companion object {
        private const val GRID_SIZE = 20
        private const val INITIAL_SNAKE_LENGTH = 3
        private const val INITIAL_SPEED = 150L // ms
        private const val MIN_SPEED = 50L
        private const val SPEED_DECREMENT = 5L
    }
    
    // Oyun durumu
    private var isRunning = false
    private var isPaused = false
    private var isGameOver = false
    
    // Oyun alanı
    private var gridWidth = 0
    private var gridHeight = 0
    private var cellSize = 0
    
    // Yılan
    private val snake = LinkedList<Point>()
    private var snakeDirection = Direction.RIGHT
    
    // Yem
    private var food = Point()
    
    // Skor
    private var score = 0
    private var highScore = 0
    
    // Renkler
    private val snakePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    
    private val foodPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    
    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    
    // Oyun döngüsü
    private val handler = Handler(Looper.getMainLooper())
    private var gameSpeed = INITIAL_SPEED
    private val gameRunnable = object : Runnable {
        override fun run() {
            if (isRunning && !isPaused && !isGameOver) {
                moveSnake()
                checkCollisions()
                invalidate()
                handler.postDelayed(this, gameSpeed)
            }
        }
    }
    
    // Listener
    private var gameStateChangeListener: OnGameStateChangeListener? = null
    
    interface OnGameStateChangeListener {
        fun onScoreChanged(score: Int)
        fun onGameOver()
        fun onGameStarted()
    }
    
    fun setOnGameStateChangeListener(listener: OnGameStateChangeListener) {
        this.gameStateChangeListener = listener
    }
    
    init {
        setOnTouchListener(this)
        loadHighScore()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        gridWidth = GRID_SIZE
        gridHeight = GRID_SIZE
        cellSize = minOf(w, h) / GRID_SIZE
        
        initializeGame()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Arkaplan
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Izgara çiz
        for (i in 0..gridWidth) {
            val x = i * cellSize.toFloat()
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        }
        for (i in 0..gridHeight) {
            val y = i * cellSize.toFloat()
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }
        
        // Yılanı çiz
        for (segment in snake) {
            val left = segment.x * cellSize.toFloat()
            val top = segment.y * cellSize.toFloat()
            val right = left + cellSize
            val bottom = top + cellSize
            
            canvas.drawRect(left, top, right, bottom, snakePaint)
            
            // Yılanın başını farklı renkte yap
            if (segment == snake.first) {
                val headPaint = Paint().apply {
                    color = Color.YELLOW
                    style = Paint.Style.FILL
                }
                canvas.drawRect(left + 2, top + 2, right - 2, bottom - 2, headPaint)
            }
        }
        
        // Yemi çiz
        val foodLeft = food.x * cellSize.toFloat()
        val foodTop = food.y * cellSize.toFloat()
        val foodRight = foodLeft + cellSize
        val foodBottom = foodTop + cellSize
        
        canvas.drawRect(foodLeft, foodTop, foodRight, foodBottom, foodPaint)
        
        // Yem içine daire çiz
        val foodCenterX = foodLeft + cellSize / 2
        val foodCenterY = foodTop + cellSize / 2
        val foodRadius = cellSize / 3f
        
        val innerFoodPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(foodCenterX, foodCenterY, foodRadius, innerFoodPaint)
    }
    
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !isGameOver) {
            val touchX = event.x
            val touchY = event.y
            
            val centerX = width / 2
            val centerY = height / 2
            
            val dx = touchX - centerX
            val dy = touchY - centerY
            
            // Yatay hareket daha büyükse yatay yönde değiş
            if (abs(dx) > abs(dy)) {
                if (dx > 0 && snakeDirection != Direction.LEFT) {
                    snakeDirection = Direction.RIGHT
                } else if (dx < 0 && snakeDirection != Direction.RIGHT) {
                    snakeDirection = Direction.LEFT
                }
            } else {
                if (dy > 0 && snakeDirection != Direction.UP) {
                    snakeDirection = Direction.DOWN
                } else if (dy < 0 && snakeDirection != Direction.DOWN) {
                    snakeDirection = Direction.UP
                }
            }
            
            return true
        }
        return false
    }
    
    private fun initializeGame() {
        snake.clear()
        
        // Yılanı ortada başlat
        val startX = gridWidth / 2
        val startY = gridHeight / 2
        
        for (i in 0 until INITIAL_SNAKE_LENGTH) {
            snake.add(Point(startX - i, startY))
        }
        
        generateFood()
        score = 0
        gameSpeed = INITIAL_SPEED
        isGameOver = false
    }
    
    private fun generateFood() {
        val random = Random()
        
        do {
            food.x = random.nextInt(gridWidth)
            food.y = random.nextInt(gridHeight)
        } while (snake.contains(food))
    }
    
    private fun moveSnake() {
        val head = snake.first
        
        val newHead = when (snakeDirection) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }
        
        // Duvardan geçiş (teleport)
        if (newHead.x < 0) newHead.x = gridWidth - 1
        if (newHead.x >= gridWidth) newHead.x = 0
        if (newHead.y < 0) newHead.y = gridHeight - 1
        if (newHead.y >= gridHeight) newHead.y = 0
        
        snake.addFirst(newHead)
        
        // Yem yenmediyse kuyruğu kes
        if (newHead != food) {
            snake.removeLast()
        } else {
            // Yem yendi
            score += 10
            gameStateChangeListener?.onScoreChanged(score)
            
            // Hızı artır
            if (gameSpeed > MIN_SPEED) {
                gameSpeed -= SPEED_DECREMENT
            }
            
            generateFood()
        }
    }
    
    private fun checkCollisions() {
        val head = snake.first
        
        // Kendine çarpma kontrolü (baş hariç)
        for (i in 1 until snake.size) {
            if (head == snake[i]) {
                gameOver()
                return
            }
        }
    }
    
    private fun gameOver() {
        isGameOver = true
        isRunning = false
        
        if (score > highScore) {
            highScore = score
            saveHighScore()
        }
        
        gameStateChangeListener?.onGameOver()
    }
    
    // Oyun kontrol fonksiyonları
    fun startGame() {
        if (!isRunning) {
            isRunning = true
            isPaused = false
            isGameOver = false
            initializeGame()
            gameStateChangeListener?.onGameStarted()
            handler.post(gameRunnable)
        }
    }
    
    fun pauseGame() {
        isPaused = true
    }
    
    fun resumeGame() {
        if (isRunning && isPaused) {
            isPaused = false
            handler.post(gameRunnable)
        }
    }
    
    fun restartGame() {
        stopGame()
        startGame()
    }
    
    fun stopGame() {
        isRunning = false
        isPaused = false
        handler.removeCallbacks(gameRunnable)
    }
    
    fun isGameRunning(): Boolean = isRunning
    
    fun isGamePaused(): Boolean = isPaused
    
    fun getScore(): Int = score
    
    fun getHighScore(): Int = highScore
    
    // Yüksek skoru kaydet/yükle
    private fun saveHighScore() {
        val prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE)
        prefs.edit().putInt("high_score", highScore).apply()
    }
    
    private fun loadHighScore() {
        val prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)
    }
    
    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
}