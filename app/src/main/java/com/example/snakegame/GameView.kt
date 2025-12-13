package com.example.snakegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import timber.log.Timber
import kotlin.math.abs

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {
    
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var gameThread: GameThread
    private val handler = Handler(Looper.getMainLooper())
    
    // Oyun state'i
    private var gameState = GameState.READY
    private var score = 0
    private var highScore = 0
    private var gameSpeed = INITIAL_SPEED
    
    // Yılan ve yem
    private val snake = mutableListOf<Point>()
    private var food: Point? = null
    private var currentDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    
    // Boyutlar
    private var gridSize = 20
    private var gridWidth = 0
    private var gridHeight = 0
    
    // Paint objeleri (reuse için)
    private val snakePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val foodPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    
    init {
        holder.addCallback(this)
        isFocusable = true
        
        // SharedPreferences'ten high score'u yükle
        loadHighScore()
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder = holder
        gridWidth = width / gridSize
        gridHeight = height / gridSize
        
        initializeGame()
        Timber.d("Surface created")
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Yeniden boyutlandırma durumunda
        this.gridWidth = width / gridSize
        this.gridHeight = height / gridSize
        Timber.d("Surface changed: $width x $height")
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
        Timber.d("Surface destroyed")
    }
    
    private fun initializeGame() {
        // Yılanı başlat
        snake.clear()
        val startX = gridWidth / 2
        val startY = gridHeight / 2
        
        for (i in 0..2) {
            snake.add(Point(startX - i, startY))
        }
        
        // Yem oluştur
        generateFood()
        
        // Skor ve hızı sıfırla
        score = 0
        gameSpeed = INITIAL_SPEED
        currentDirection = Direction.RIGHT
        nextDirection = Direction.RIGHT
        
        gameState = GameState.READY
    }
    
    fun startGame() {
        if (gameState == GameState.READY || gameState == GameState.GAME_OVER) {
            initializeGame()
            gameState = GameState.RUNNING
            gameThread = GameThread()
            gameThread.start()
            Timber.d("Game thread started")
        }
    }
    
    fun pauseGame() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED
            Timber.d("Game paused")
        }
    }
    
    fun resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING
            Timber.d("Game resumed")
        }
    }
    
    fun restartGame() {
        stopGame()
        initializeGame()
        updateUI()
        Timber.d("Game restarted")
    }
    
    fun stopGame() {
        gameState = GameState.GAME_OVER
        gameThread?.let {
            it.interrupt()
        }
        Timber.d("Game stopped")
    }
    
    fun isGameRunning(): Boolean {
        return gameState == GameState.RUNNING
    }
    
    fun changeDirection(direction: Direction) {
        // Zıt yöne gitmeyi engelle
        if (!isOppositeDirection(direction)) {
            nextDirection = direction
        }
    }
    
    private fun isOppositeDirection(newDirection: Direction): Boolean {
        return (currentDirection == Direction.UP && newDirection == Direction.DOWN) ||
               (currentDirection == Direction.DOWN && newDirection == Direction.UP) ||
               (currentDirection == Direction.LEFT && newDirection == Direction.RIGHT) ||
               (currentDirection == Direction.RIGHT && newDirection == Direction.LEFT)
    }
    
    private fun update() {
        if (gameState != GameState.RUNNING) return
        
        // Yönü güncelle
        currentDirection = nextDirection
        
        // Yılanın başını hareket ettir
        val head = snake.first()
        val newHead = Point(head)
        
        when (currentDirection) {
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
        
        // Yeni başı ekle
        snake.add(0, newHead)
        
        // Yem yeme kontrolü
        food?.let { foodPoint ->
            if (newHead.x == foodPoint.x && newHead.y == foodPoint.y) {
                // Yem yendi
                score += 10
                generateFood()
                
                // Her 50 puanda bir hızlan
                if (score % 50 == 0 && gameSpeed > MIN_SPEED) {
                    gameSpeed -= 10
                }
                
                // High score güncelle
                if (score > highScore) {
                    highScore = score
                    saveHighScore()
                }
                
                updateUI()
            } else {
                // Yem yenmediyse kuyruğu kısalt
                snake.removeLast()
            }
        }
    }
    
    private fun draw(canvas: Canvas) {
        // Canvas'ı temizle
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Izgara çiz (debug için)
        if (BuildConfig.DEBUG) {
            drawGrid(canvas)
        }
        
        // Yılanı çiz
        for ((index, point) in snake.withIndex()) {
            val isHead = index == 0
            val paint = if (isHead) {
                snakePaint.apply { color = Color.GREEN }
            } else {
                snakePaint.apply { color = Color.rgb(0, 150, 0) }
            }
            
            val left = point.x * gridSize.toFloat()
            val top = point.y * gridSize.toFloat()
            val right = left + gridSize
            val bottom = top + gridSize
            
            canvas.drawRect(left, top, right, bottom, paint)
            
            // Baş için gözler
            if (isHead) {
                val eyePaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                
                val eyeSize = gridSize / 5f
                val eyeOffset = gridSize / 3f
                
                // Sol göz
                canvas.drawCircle(
                    left + eyeOffset,
                    top + eyeOffset,
                    eyeSize,
                    eyePaint
                )
                
                // Sağ göz
                canvas.drawCircle(
                    right - eyeOffset,
                    top + eyeOffset,
                    eyeSize,
                    eyePaint
                )
            }
        }
        
        // Yemi çiz
        food?.let { point ->
            val left = point.x * gridSize.toFloat()
            val top = point.y * gridSize.toFloat()
            val right = left + gridSize
            val bottom = top + gridSize
            
            canvas.drawRect(left, top, right, bottom, foodPaint)
            
            // Yem için iç detay
            val innerPaint = Paint().apply {
                color = Color.YELLOW
                style = Paint.Style.FILL
            }
            
            val innerSize = gridSize / 3f
            val centerX = left + gridSize / 2f
            val centerY = top + gridSize / 2f
            
            canvas.drawCircle(centerX, centerY, innerSize, innerPaint)
        }
        
        // Oyun durumu metni
        if (gameState != GameState.RUNNING) {
            val statusText = when (gameState) {
                GameState.PAUSED -> "PAUSED"
                GameState.GAME_OVER -> "GAME OVER"
                else -> "READY"
            }
            
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 48f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            
            canvas.drawText(
                statusText,
                width / 2f,
                height / 2f,
                textPaint
            )
        }
    }
    
    private fun drawGrid(canvas: Canvas) {
        for (x in 0..gridWidth) {
            val lineX = x * gridSize.toFloat()
            canvas.drawLine(lineX, 0f, lineX, height.toFloat(), gridPaint)
        }
        
        for (y in 0..gridHeight) {
            val lineY = y * gridSize.toFloat()
            canvas.drawLine(0f, lineY, width.toFloat(), lineY, gridPaint)
        }
    }
    
    private fun generateFood() {
        val availablePoints = mutableListOf<Point>()
        
        // Tüm olası noktaları oluştur
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val point = Point(x, y)
                if (!snake.any { it.x == point.x && it.y == point.y }) {
                    availablePoints.add(point)
                }
            }
        }
        
        // Rastgele yem seç
        if (availablePoints.isNotEmpty()) {
            food = availablePoints.random()
        }
    }
    
    private fun gameOver() {
        gameState = GameState.GAME_OVER
        handler.post {
            (context as? MainActivity)?.gameOver()
        }
        Timber.d("Game over - Score: $score")
    }
    
    private fun updateUI() {
        handler.post {
            (context as? MainActivity)?.apply {
                updateScore(score)
                updateHighScore(highScore)
            }
        }
    }
    
    private fun loadHighScore() {
        val prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)
    }
    
    private fun saveHighScore() {
        val prefs = context.getSharedPreferences("snake_game", Context.MODE_PRIVATE)
        prefs.edit().putInt("high_score", highScore).apply()
    }
    
    fun releaseResources() {
        // Kaynakları serbest bırak
        stopGame()
        Timber.d("Resources released")
    }
    
    inner class GameThread : Thread() {
        private var running = true
        
        override fun run() {
            while (running && !isInterrupted) {
                val startTime = System.currentTimeMillis()
                
                if (gameState == GameState.RUNNING) {
                    update()
                }
                
                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    canvas?.let {
                        synchronized(surfaceHolder) {
                            draw(it)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in game thread")
                } finally {
                    canvas?.let {
                        surfaceHolder.unlockCanvasAndPost(it)
                    }
                }
                
                // FPS kontrolü
                val sleepTime = gameSpeed - (System.currentTimeMillis() - startTime)
                if (sleepTime > 0) {
                    try {
                        sleep(sleepTime)
                    } catch (e: InterruptedException) {
                        running = false
                        Timber.d("Game thread interrupted")
                    }
                }
            }
        }
        
        fun stopThread() {
            running = false
            interrupt()
        }
    }
    
    companion object {
        private const val INITIAL_SPEED = 150L // ms
        private const val MIN_SPEED = 50L // ms
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

enum class GameState {
    READY, RUNNING, PAUSED, GAME_OVER
}