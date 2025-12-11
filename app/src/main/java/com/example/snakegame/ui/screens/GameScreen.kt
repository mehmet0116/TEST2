package com.example.snakegame.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snakegame.R
import com.example.snakegame.game.Direction
import com.example.snakegame.game.SnakeGame
import com.example.snakegame.ui.theme.BackgroundBeige
import com.example.snakegame.ui.theme.FoodRed
import com.example.snakegame.ui.theme.GameGridLight
import com.example.snakegame.ui.theme.SnakeDarkGreen
import com.example.snakegame.ui.theme.SnakeGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import timber.log.Timber

@Composable
fun GameScreen(
    onGameOver: (Int) -> Unit,
    onBackToMenu: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val coroutineScope = rememberCoroutineScope()
    
    // Oyun tahtası boyutunu ekrana uygun şekilde ayarla
    val gridSize = remember(screenWidth, screenHeight) {
        min(screenWidth, screenHeight * 0.7f).toInt()
    }
    val cellSize = remember(gridSize) { gridSize / 20 }
    
    val game = remember { SnakeGame() }
    val gameState by game.gameState
    
    // Animasyon değerleri
    val foodAnimation = remember { Animatable(0.4f) }
    val snakeAnimation = remember { Animatable(1f) }
    
    // Oyun hızını derived state olarak takip et
    val gameSpeed by remember(gameState.gameSpeed) {
        derivedStateOf { gameState.gameSpeed }
    }
    
    // Oyun döngüsü
    LaunchedEffect(gameSpeed) {
        while (true) {
            if (!gameState.isPaused && !gameState.isGameOver) {
                game.update()
                delay(gameSpeed.toLong())
            } else {
                delay(100)
            }
        }
    }
    
    // Yemek yendiğinde animasyon
    LaunchedEffect(gameState.foodEaten) {
        if (gameState.foodEaten) {
            foodAnimation.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(durationMillis = 200)
            )
            foodAnimation.animateTo(
                targetValue = 0.4f,
                animationSpec = tween(durationMillis = 200)
            )
        }
    }
    
    // Oyun bittiğinde callback'i tetikle
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            Timber.i("Game over triggered with score: ${gameState.score}")
            onGameOver(gameState.score)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBeige)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst kontrol paneli
            GameHeader(
                score = gameState.score,
                isPaused = gameState.isPaused,
                onPauseToggle = { game.togglePause() },
                onBack = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Oyun tahtası
            Box(
                modifier = Modifier
                    .size(gridSize.dp)
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                try {
                                    // Ekrana dokunma ile yön kontrolü
                                    val cellWidth = size.width / 20
                                    val cellHeight = size.height / 20
                                    val tapX = offset.x / cellWidth
                                    val tapY = offset.y / cellHeight
                                    
                                    // Dokunulan pozisyona göre yön belirle
                                    val head = gameState.snake.firstOrNull() 
                                        ?: return@detectTapGestures
                                    val deltaX = tapX - head.x
                                    val deltaY = tapY - head.y
                                    
                                    val direction = when {
                                        kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY) -> {
                                            if (deltaX > 0) Direction.RIGHT else Direction.LEFT
                                        }
                                        else -> {
                                            if (deltaY > 0) Direction.DOWN else Direction.UP
                                        }
                                    }
                                    
                                    game.setDirection(direction)
                                    Timber.d("Tap direction: $direction at ($tapX, $tapY)")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error handling tap gesture")
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellWidth = size.width / 20
                    val cellHeight = size.height / 20
                    
                    // Izgara çizgileri
                    for (i in 0..20) {
                        // Dikey çizgiler
                        drawLine(
                            color = GameGridLight,
                            start = Offset(i * cellWidth, 0f),
                            end = Offset(i * cellWidth, size.height),
                            strokeWidth = 1f
                        )
                        // Yatay çizgiler
                        drawLine(
                            color = GameGridLight,
                            start = Offset(0f, i * cellHeight),
                            end = Offset(size.width, i * cellHeight),
                            strokeWidth = 1f
                        )
                    }
                    
                    // Yılanı çiz
                    gameState.snake.forEachIndexed { index, position ->
                        val isHead = index == 0
                        val snakeColor = if (isHead) SnakeDarkGreen else SnakeGreen
                        val scale = if (isHead) snakeAnimation.value else 1f
                        
                        val scaledWidth = cellWidth * scale
                        val scaledHeight = cellHeight * scale
                        val offsetX = (cellWidth - scaledWidth) / 2
                        val offsetY = (cellHeight - scaledHeight) / 2
                        
                        drawRect(
                            color = snakeColor,
                            topLeft = Offset(
                                position.x * cellWidth + offsetX,
                                position.y * cellHeight + offsetY
                            ),
                            size = Size(scaledWidth, scaledHeight)
                        )
                        
                        // Baş için gözler
                        if (isHead) {
                            val eyeSize = cellWidth * 0.15f
                            val eyeOffsetX = cellWidth * 0.25f
                            val eyeOffsetY = cellHeight * 0.25f
                            
                            drawCircle(
                                color = Color.White,
                                center = Offset(
                                    position.x * cellWidth + eyeOffsetX,
                                    position.y * cellHeight + eyeOffsetY
                                ),
                                radius = eyeSize
                            )
                            drawCircle(
                                color = Color.White,
                                center = Offset(
                                    (position.x + 1) * cellWidth - eyeOffsetX,
                                    position.y * cellHeight + eyeOffsetY
                                ),
                                radius = eyeSize
                            )
                            
                            // Göz bebekleri
                            val pupilSize = eyeSize * 0.5f
                            drawCircle(
                                color = Color.Black,
                                center = Offset(
                                    position.x * cellWidth + eyeOffsetX,
                                    position.y * cellHeight + eyeOffsetY
                                ),
                                radius = pupilSize
                            )
                            drawCircle(
                                color = Color.Black,
                                center = Offset(
                                    (position.x + 1) * cellWidth - eyeOffsetX,
                                    position.y * cellHeight + eyeOffsetY
                                ),
                                radius = pupilSize
                            )
                        }
                    }
                    
                    // Yemeği çiz (animasyonlu)
                    val foodRadius = cellWidth * 0.4f * foodAnimation.value
                    drawCircle(
                        color = FoodRed,
                        center = Offset(
                            gameState.food.x * cellWidth + cellWidth / 2,
                            gameState.food.y * cellHeight + cellHeight / 2
                        ),
                        radius = foodRadius
                    )
                    
                    // Yemek için detaylar
                    drawCircle(
                        color = FoodRed.copy(alpha = 0.7f),
                        center = Offset(
                            gameState.food.x * cellWidth + cellWidth / 2 - foodRadius * 0.3f,
                            gameState.food.y * cellHeight + cellHeight / 2 - foodRadius * 0.3f
                        ),
                        radius = foodRadius * 0.3f
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Yön kontrol butonları
            DirectionControls(
                onDirectionChange = { direction ->
                    if (!gameState.isPaused && !gameState.isGameOver) {
                        game.setDirection(direction)
                        // Yılan animasyonu
                        coroutineScope.launch {
                            snakeAnimation.animateTo(
                                targetValue = 1.1f,
                                animationSpec = tween(durationMillis = 100)
                            )
                            snakeAnimation.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 100)
                            )
                        }
                    }
                },
                modifier = Modifier.padding(16.dp)
            )
            
            // Oyun durumu mesajları
            if (gameState.isGameOver) {
                Text(
                    text = "OYUN BİTTİ!",
                    color = FoodRed,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(8.dp)
                )
            } else if (gameState.isPaused) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "OYUN DURDURULDU",
                        color = Color.Gray,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Devam etmek için duraklatma butonuna basın",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GameHeader(
    score: Int,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Geri butonu
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Geri",
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Skor gösterimi
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "SKOR",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = SnakeGreen
            )
        }
        
        // Duraklat/Devam et butonu
        IconButton(
            onClick = onPauseToggle,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause),
                contentDescription = if (isPaused) "Devam Et" else "Duraklat",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun DirectionControls(
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Yukarı butonu
        DirectionButton(
            direction = Direction.UP,
            iconId = R.drawable.ic_up,
            onClick = { onDirectionChange(Direction.UP) },
            modifier = Modifier.size(64.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sol butonu
            DirectionButton(
                direction = Direction.LEFT,
                iconId = R.drawable.ic_left,
                onClick = { onDirectionChange(Direction.LEFT) },
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.size(64.dp))
            
            // Sağ butonu
            DirectionButton(
                direction = Direction.RIGHT,
                iconId = R.drawable.ic_right,
                onClick = { onDirectionChange(Direction.RIGHT) },
                modifier = Modifier.size(64.dp)
            )
        }
        
        // Aşağı butonu
        DirectionButton(
            direction = Direction.DOWN,
            iconId = R.drawable.ic_down,
            onClick = { onDirectionChange(Direction.DOWN) },
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun DirectionButton(
    direction: Direction,
    iconId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = when (direction) {
                Direction.UP -> "Yukarı"
                Direction.DOWN -> "Aşağı"
                Direction.LEFT -> "Sol"
                Direction.RIGHT -> "Sağ"
            },
            modifier = Modifier.size(48.dp),
            tint = SnakeGreen
        )
    }
}