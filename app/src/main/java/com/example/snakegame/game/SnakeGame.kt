package com.example.snakegame.game

import android.os.Parcelable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import kotlin.math.abs

/**
 * Yılan oyunu için pozisyon veri sınıfı
 * @param x X koordinatı
 * @param y Y koordinatı
 */
@Parcelize
data class Position(val x: Int, val y: Int) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false
        return x == other.x && y == other.y
    }
    
    override fun hashCode(): Int = 31 * x + y
    
    /**
     * İki pozisyon arasındaki mesafeyi hesaplar
     */
    fun distanceTo(other: Position): Double {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
    }
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
    
    /**
     * Yönü vektöre çevirir
     */
    fun toVector(): Pair<Int, Int> = when (this) {
        UP -> Pair(0, -1)
        DOWN -> Pair(0, 1)
        LEFT -> Pair(-1, 0)
        RIGHT -> Pair(1, 0)
    }
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
    val foodEaten: Boolean = false,
    val gameSpeed: Int = 150 // ms cinsinden
)

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
    
    // Yılan pozisyonlarını hızlı erişim için Set olarak tut
    private val snakePositions = mutableSetOf<Position>()
    
    // Boş hücreleri cache'le
    private val emptyCellsCache = mutableSetOf<Position>()
    private var isCacheDirty = true
    
    private val initialSnake = listOf(
        Position(gridWidth / 2, gridHeight / 2),
        Position(gridWidth / 2 - 1, gridHeight / 2),
        Position(gridWidth / 2 - 2, gridHeight / 2)
    )
    
    init {
        resetGame()
        Timber.d("SnakeGame initialized with grid: ${gridWidth}x$gridHeight")
    }
    
    /**
     * Oyunu sıfırlar
     */
    fun resetGame() {
        try {
            val newFood = generateFood(initialSnake)
            snakePositions.clear()
            snakePositions.addAll(initialSnake)
            
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
            isCacheDirty = true
            
            Timber.i("Game reset. Initial snake: $initialSnake, Food: $newFood")
        } catch (e: Exception) {
            Timber.e(e, "Error resetting game")
            throw IllegalStateException("Failed to reset game", e)
        }
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
            Timber.d("Direction changed to: $direction")
        }
    }
    
    /**
     * Oyun durumunu günceller
     */
    fun update() {
        val currentState = _gameState.value
        if (currentState.isGameOver || currentState.isPaused) return
        
        try {
            // Yönü güncelle ve flag'i sıfırla
            currentDirection = nextDirection
            isDirectionChangedThisFrame = false
            
            val head = currentState.snake.first()
            val newHead = calculateNewHead(head, currentDirection)
            
            // Çarpışma kontrolleri
            if (checkWallCollision(newHead) || checkSelfCollision(newHead)) {
                _gameState.value = currentState.copy(isGameOver = true)
                Timber.i("Game over! Final score: ${currentState.score}")
                return
            }
            
            // Yeni yılan pozisyonlarını oluştur
            val newSnake = mutableListOf<Position>()
            newSnake.add(newHead)
            newSnake.addAll(currentState.snake)
            
            // Yemek yeme kontrolü
            val isFoodEaten = newHead == currentState.food
            val newScore = if (isFoodEaten) currentState.score + 10 else currentState.score
            
            // Yemek yenmediyse kuyruğu çıkar
            if (!isFoodEaten) {
                val removedTail = newSnake.removeLast()
                snakePositions.remove(removedTail)
            } else {
                Timber.d("Food eaten at: ${currentState.food}")
            }
            
            // Snake positions set'ini güncelle
            snakePositions.clear()
            snakePositions.addAll(newSnake)
            
            val newFood = if (isFoodEaten) {
                isCacheDirty = true
                generateFood(newSnake)
            } else {
                currentState.food
            }
            
            _gameState.value = currentState.copy(
                snake = newSnake,
                food = newFood,
                score = newScore,
                foodEaten = isFoodEaten
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error updating game state")
            _gameState.value = currentState.copy(isGameOver = true)
        }
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
    private fun checkSelfCollision(head: Position): Boolean {
        return snakePositions.contains(head)
    }
    
    /**
     * Oyunu duraklat/devam ettir
     */
    fun togglePause() {
        val newPausedState = !_gameState.value.isPaused
        _gameState.value = _gameState.value.copy(isPaused = newPausedState)
        Timber.d("Game ${if (newPausedState) "paused" else "resumed"}")
    }
    
    /**
     * Rastgele yemek pozisyonu oluşturur (optimize edilmiş)
     */
    private fun generateFood(snake: List<Position>): Position {
        try {
            // Cache temizse boş hücreleri yeniden hesapla
            if (isCacheDirty) {
                emptyCellsCache.clear()
                
                // Tüm hücreleri oluştur
                for (x in 0 until gridWidth) {
                    for (y in 0 until gridHeight) {
                        emptyCellsCache.add(Position(x, y))
                    }
                }
                
                // Yılanın kapladığı hücreleri çıkar
                emptyCellsCache.removeAll(snakePositions)
                isCacheDirty = false
            }
            
            return if (emptyCellsCache.isNotEmpty()) {
                emptyCellsCache.random().also { position ->
                    Timber.d("Generated food at: $position")
                }
            } else {
                // Boş hücre yoksa (nadir durum) ilk pozisyonu döndür
                Timber.w("No empty cells available for food generation")
                Position(0, 0)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error generating food")
            return Position(0, 0)
        }
    }
    
    /**
     * Oyun hızını ayarlar
     */
    fun setGameSpeed(speed: Int) {
        require(speed in 50..300) { "Speed must be between 50 and 300 ms" }
        _gameState.value = _gameState.value.copy(gameSpeed = speed)
        Timber.d("Game speed set to: ${speed}ms")
    }
    
    fun getGridWidth(): Int = gridWidth
    fun getGridHeight(): Int = gridHeight
    
    /**
     * Oyun durumunu kontrol et
     */
    fun validateGameState(): Boolean {
        val state = _gameState.value
        return state.snake.isNotEmpty() &&
               state.food.x in 0 until gridWidth &&
               state.food.y in 0 until gridHeight &&
               !checkWallCollision(state.snake.first())
    }
}