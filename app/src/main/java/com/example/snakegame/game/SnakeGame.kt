package com.example.snakegame.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Yılan oyunu için pozisyon veri sınıfı
 * @param x X koordinatı
 * @param y Y koordinatı
 */
data class Position(val x: Int, val y: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false
        return x == other.x && y == other.y
    }
    
    override fun hashCode(): Int = 31 * x + y
}

/**
 * Yılan hareket yönleri
 */
enum class Direction {
    UP, DOWN, LEFT, RIGHT;
    
    /**
     * Verilen yönün tersini döndürür
     */
    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
    
    /**
     * Yön değişikliği geçerli mi kontrol eder
     */
    fun isValidChange(newDirection: Direction): Boolean {
        return this.opposite() != newDirection
    }
}

/**
 * Yılan oyunu ana sınıfı
 * @param gridWidth Oyun alanı genişliği (varsayılan: 20)
 * @param gridHeight Oyun alanı yüksekliği (varsayılan: 20)
 */
class SnakeGame(
    private val gridWidth: Int = 20,
    private val gridHeight: Int = 20
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private var currentDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    private var isDirectionChangedThisFrame = false
    
    private val initialSnake = listOf(
        Position(gridWidth / 2, gridHeight / 2),
        Position(gridWidth / 2 - 1, gridHeight / 2),
        Position(gridWidth / 2 - 2, gridHeight / 2)
    )
    
    init {
        resetGame()
    }
    
    /**
     * Oyunu sıfırlar
     */
    fun resetGame() {
        val newFood = generateFood(initialSnake)
        _gameState.value = GameState(
            snake = initialSnake.toMutableList(),
            food = newFood,
            score = 0,
            isGameOver = false,
            isPaused = false,
            foodEaten = false
        )
        currentDirection = Direction.RIGHT
        nextDirection = Direction.RIGHT
        isDirectionChangedThisFrame = false
    }
    
    /**
     * Yılanın yönünü değiştirir
     * @param direction Yeni yön
     */
    fun setDirection(direction: Direction) {
        // Aynı frame içinde birden fazla yön değişikliğini engelle
        if (isDirectionChangedThisFrame) return
        
        // Geçerli yön değişikliği kontrolü
        if (currentDirection.isValidChange(direction)) {
            nextDirection = direction
            isDirectionChangedThisFrame = true
        }
    }
    
    /**
     * Oyun durumunu günceller
     */
    fun update() {
        val currentState = _gameState.value
        if (currentState.isGameOver || currentState.isPaused) return
        
        // Yönü güncelle ve flag'i sıfırla
        currentDirection = nextDirection
        isDirectionChangedThisFrame = false
        
        val head = currentState.snake.first()
        val newHead = calculateNewHead(head, currentDirection)
        
        // Çarpışma kontrolleri
        if (checkWallCollision(newHead) || checkSelfCollision(newHead, currentState.snake)) {
            _gameState.value = currentState.copy(isGameOver = true)
            return
        }
        
        // Yeni yılan pozisyonlarını oluştur
        val newSnake = mutableListOf<Position>()
        newSnake.add(newHead)
        newSnake.addAll(currentState.snake)
        
        // Yemek yeme kontrolü
        val isFoodEaten = newHead == currentState.food
        val newScore = if (isFoodEaten) currentState.score + 10 else currentState.score
        val newFood = if (isFoodEaten) generateFood(newSnake) else currentState.food
        
        // Yemek yenmediyse kuyruğu çıkar
        if (!isFoodEaten) {
            newSnake.removeLast()
        }
        
        _gameState.value = currentState.copy(
            snake = newSnake,
            food = newFood,
            score = newScore,
            foodEaten = isFoodEaten
        )
    }
    
    /**
     * Yeni kafa pozisyonunu hesaplar
     */
    private fun calculateNewHead(head: Position, direction: Direction): Position {
        return when (direction) {
            Direction.UP -> Position(head.x, head.y - 1)
            Direction.DOWN -> Position(head.x, head.y + 1)
            Direction.LEFT -> Position(head.x - 1, head.y)
            Direction.RIGHT -> Position(head.x + 1, head.y)
        }
    }
    
    /**
     * Duvar çarpışmasını kontrol eder
     */
    private fun checkWallCollision(position: Position): Boolean {
        return position.x < 0 || position.x >= gridWidth ||
               position.y < 0 || position.y >= gridHeight
    }
    
    /**
     * Kendine çarpma kontrolü (optimize edilmiş versiyon)
     */
    private fun checkSelfCollision(head: Position, snake: List<Position>): Boolean {
        // Baş hariç diğer segmentleri kontrol et
        for (i in 1 until snake.size) {
            if (snake[i] == head) return true
        }
        return false
    }
    
    /**
     * Oyunu duraklat/devam ettir
     */
    fun togglePause() {
        _gameState.value = _gameState.value.copy(isPaused = !_gameState.value.isPaused)
    }
    
    /**
     * Rastgele yemek pozisyonu oluşturur (optimize edilmiş)
     */
    private fun generateFood(snake: List<Position>): Position {
        // Boş hücreleri bul
        val emptyCells = mutableListOf<Position>()
        
        // Grid boyutuna göre optimize edilmiş boş hücre bulma
        val snakeSet = snake.toSet()
        
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val pos = Position(x, y)
                if (!snakeSet.contains(pos)) {
                    emptyCells.add(pos)
                }
            }
        }
        
        return if (emptyCells.isNotEmpty()) {
            emptyCells.random()
        } else {
            // Boş hücre yoksa (nadir durum) ilk pozisyonu döndür
            Position(0, 0)
        }
    }
    
    /**
     * Oyun hızını ayarlar
     */
    fun setGameSpeed(speed: Int) {
        // Hız ayarı için gelecekte kullanılabilir
    }
    
    fun getGridWidth(): Int = gridWidth
    fun getGridHeight(): Int = gridHeight
}

/**
 * Oyun durumu veri sınıfı
 */
data class GameState(
    val snake: List<Position> = emptyList(),
    val food: Position = Position(0, 0),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val foodEaten: Boolean = false
)