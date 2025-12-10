package com.snakegame.pro

import android.content.SharedPreferences
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toComposeRect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import kotlin.random.Random

class GameViewModel : ViewModel() {
    
    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    // Oyun durumu
    private val _gameState = MutableStateFlow(true) // true = çalışıyor
    val gameState: StateFlow<Boolean> = _gameState
    
    // Yılan pozisyonları (x, y) grid koordinatları
    private val _snake = MutableStateFlow(listOf(Pair(5, 5), Pair(4, 5), Pair(3, 5)))
    val snake: StateFlow<List<Pair<Int, Int>>> = _snake
    
    // Yem pozisyonu
    private val _food = MutableStateFlow(Pair(10, 10))
    val food: StateFlow<Pair<Int, Int>> = _food
    
    // Engeller
    private val _obstacles = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val obstacles: StateFlow<List<Pair<Int, Int>>> = _obstacles
    
    // Skor
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score
    
    // Yüksek skor
    private val _highScore = MutableStateFlow(0)
    
    // Oyun hızı (ms)
    private val _gameSpeed = MutableStateFlow(200L)
    val gameSpeed: StateFlow<Long> = _gameSpeed
    
    // Oyun bitti mi?
    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver
    
    // Oyun duraklatıldı mı?
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused
    
    // Mevcut yön
    private var currentDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    
    // Grid boyutları
    private var gridWidth = 20
    private var gridHeight = 35
    
    init {
        viewModelScope.launch {
            resetGame()
        }
    }
    
    fun changeDirection(newDirection: Direction) {
        // Ters yöne gitmeyi engelle
        when (newDirection) {
            Direction.UP -> if (currentDirection != Direction.DOWN) nextDirection = newDirection
            Direction.DOWN -> if (currentDirection != Direction.UP) nextDirection = newDirection
            Direction.LEFT -> if (currentDirection != Direction.RIGHT) nextDirection = newDirection
            Direction.RIGHT -> if (currentDirection != Direction.LEFT) nextDirection = newDirection
        }
    }
    
    fun moveSnake() {
        if (_isGameOver.value || _isPaused.value) return
        
        currentDirection = nextDirection
        
        val head = _snake.value.first()
        val newHead = when (currentDirection) {
            Direction.UP -> Pair(head.first, head.second - 1)
            Direction.DOWN -> Pair(head.first, head.second + 1)
            Direction.LEFT -> Pair(head.first - 1, head.second)
            Direction.RIGHT -> Pair(head.first + 1, head.second)
        }
        
        // Duvar çarpışması kontrolü
        if (newHead.first < 0 || newHead.first >= gridWidth || 
            newHead.second < 0 || newHead.second >= gridHeight) {
            gameOver()
            return
        }
        
        // Kendine çarpma kontrolü
        if (_snake.value.contains(newHead)) {
            gameOver()
            return
        }
        
        // Engele çarpma kontrolü
        if (_obstacles.value.contains(newHead)) {
            gameOver()
            return
        }
        
        val newSnake = mutableListOf(newHead)
        newSnake.addAll(_snake.value)
        
        // Yem yendi mi?
        if (newHead == _food.value) {
            // Yem yendi, yılan büyümeyecek (zaten head ekledik)
            generateFood()
            generateObstacle()
            
            // Skor güncelle
            val newScore = _score.value + 10
            _score.value = newScore
            
            // Her 50 puanda bir hız artır
            if (newScore % 50 == 0) {
                increaseSpeed()
            }
            
            // Her 100 puanda bir yeni engel
            if (newScore % 100 == 0) {
                generateObstacle()
            }
        } else {
            // Yem yenmedi, kuyruğu kısalt
            if (newSnake.size > 1) {
                newSnake.removeAt(newSnake.size - 1)
            }
        }
        
        _snake.value = newSnake
    }
    
    private fun generateFood() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        
        // Tüm boş hücreleri bul
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val pos = Pair(x, y)
                if (!_snake.value.contains(pos) && !_obstacles.value.contains(pos)) {
                    emptyCells.add(pos)
                }
            }
        }
        
        if (emptyCells.isNotEmpty()) {
            _food.value = emptyCells.random()
        }
    }
    
    private fun generateObstacle() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        
        // Yılan ve yemden uzak boş hücreleri bul
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val pos = Pair(x, y)
                val head = _snake.value.first()
                val distanceToHead = Math.abs(x - head.first) + Math.abs(y - head.second)
                
                if (!_snake.value.contains(pos) && 
                    pos != _food.value && 
                    distanceToHead > 5) {
                    emptyCells.add(pos)
                }
            }
        }
        
        if (emptyCells.isNotEmpty() && _obstacles.value.size < 10) {
            val newObstacles = _obstacles.value.toMutableList()
            repeat(2) {
                if (emptyCells.isNotEmpty()) {
                    newObstacles.add(emptyCells.random())
                    emptyCells.removeAll { it == newObstacles.last() }
                }
            }
            _obstacles.value = newObstacles
        }
    }
    
    private fun gameOver() {
        _isGameOver.value = true
        // Yüksek skoru güncelle
        if (_score.value > _highScore.value) {
            _highScore.value = _score.value
        }
    }
    
    fun resetGame() {
        _snake.value = listOf(Pair(5, 5), Pair(4, 5), Pair(3, 5))
        _score.value = 0
        _obstacles.value = emptyList()
        currentDirection = Direction.RIGHT
        nextDirection = Direction.RIGHT
        _isGameOver.value = false
        _isPaused.value = false
        _gameSpeed.value = 200L
        
        generateFood()
        // İlk engelleri oluştur
        repeat(3) {
            generateObstacle()
        }
    }
    
    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }
    
    fun increaseSpeed() {
        if (_gameSpeed.value > 50L) {
            _gameSpeed.value = _gameSpeed.value - 20L
        }
    }
    
    fun decreaseSpeed() {
        if (_gameSpeed.value < 500L) {
            _gameSpeed.value = _gameSpeed.value + 20L
        }
    }
    
    fun getSpeedLevel(): Int {
        return ((500L - _gameSpeed.value) / 20L).toInt()
    }
    
    fun handleTap(offset: Offset, canvasSize: Size) {
        // Ekrana dokunma ile yön değiştirme (isteğe bağlı)
        val centerX = canvasSize.width / 2
        val centerY = canvasSize.height / 2
        
        val deltaX = offset.x - centerX
        val deltaY = offset.y - centerY
        
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            // Yatay hareket
            if (deltaX > 0) {
                changeDirection(Direction.RIGHT)
            } else {
                changeDirection(Direction.LEFT)
            }
        } else {
            // Dikey hareket
            if (deltaY > 0) {
                changeDirection(Direction.DOWN)
            } else {
                changeDirection(Direction.UP)
            }
        }
    }
}