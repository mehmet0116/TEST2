package com.example.snakegame.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
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
        min(screenWidth, (screenHeight * 0.7f).toInt())
    }
    val cellSize = remember(gridSize) { gridSize / 20 }
    
    val game = remember { SnakeGame() }
    val gameState by game.gameState.collectAsState()
    
    // Animasyon değerleri
    val foodAnimation = remember { Animatable(0.4f) }
    val snakeAnimation = remember { Animatable(1f) }
    val scoreAnimation = remember { Animatable(1f) }
    
    // Joystick pozisyonu
    var joystickCenter by remember { mutableStateOf(Offset.Zero) }
    var joystickKnobPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Oyun döngüsü
    LaunchedEffect(gameState.gameSpeed, gameState.isPaused, gameState.isGameOver) {
        while (true) {
            if (!gameState.isPaused && !gameState.isGameOver) {
                game.update()
                delay(gameState.gameSpeed.toLong())
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
            
            // Skor animasyonu
            scoreAnimation.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 150)
            )
            scoreAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150)
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
                level = gameState.level,
                isPaused = gameState.isPaused,
                onPauseToggle = { game.togglePause() },
                onBack = onBackToMenu,
                scoreAnimation = scoreAnimation.value,
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
                                    val cellWidth = size.width / 20f
                                    val cellHeight = size.height / 20f
                                    val tapX = offset.x / cellWidth
                                    val tapY = offset.y / cellHeight
                                    
                                    // Dokunulan pozisyona göre yön belirle
                                    val head = gameState.snake.firstOrNull() 
                                        ?: return@detectTapGestures
                                    val deltaX = tapX - head.x
                                    val deltaY = tapY - head.y
                                    
                                    val direction = when {
                                        abs(deltaX) > abs(deltaY) -> {
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
                    val cellWidth = size.width / 20f
                    val cellHeight = size.height / 20f
                    
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
                            size = Size(scaledWidth, scaledHeight),
                            cornerRadius = androidx.compose.ui.graphics.CornerRadius(4f, 4f)
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
            
            // Daire şeklinde joystick kontrolü
            JoystickControl(
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
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
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
            
            // Performans istatistikleri (debug için)
            if (BuildConfig.DEBUG) {
                val stats = game.getPerformanceStats()
                Text(
                    text = "Debug: Snake Length: ${stats["snakeLength"]}, Level: ${stats["level"]}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GameHeader(
    score: Int,
    level: Int,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onBack: () -> Unit,
    scoreAnimation: Float = 1f,
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
        
        // Skor ve seviye gösterimi
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SKOR",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = (18 * scoreAnimation).sp),
                        color = SnakeGreen
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SEVİYE",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = level.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = SnakeDarkGreen
                    )
                }
            }
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
fun JoystickControl(
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    var joystickCenter by remember { mutableStateOf(Offset.Zero) }
    var joystickKnobPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var currentDirection by remember { mutableStateOf<Direction?>(null) }
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        joystickCenter = Offset(size.width / 2, size.height / 2)
                        joystickKnobPosition = joystickCenter
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        
                        val newPosition = joystickKnobPosition + Offset(dragAmount.x, dragAmount.y)
                        val maxRadius = size.width / 2 - 30f // 30f knob yarıçapı için boşluk
                        
                        // Joystick sınırlarını hesapla
                        val vector = newPosition - joystickCenter
                        val distance = sqrt(vector.x * vector.x + vector.y * vector.y)
                        
                        val limitedPosition = if (distance > maxRadius) {
                            val normalized = vector / distance
                            joystickCenter + normalized * maxRadius
                        } else {
                            newPosition
                        }
                        
                        joystickKnobPosition = limitedPosition
                        
                        // Yönü hesapla
                        val angle = atan2(
                            limitedPosition.y - joystickCenter.y,
                            limitedPosition.x - joystickCenter.x
                        )
                        
                        val direction = when {
                            angle >= -Math.PI / 4 && angle < Math.PI / 4 -> Direction.RIGHT
                            angle >= Math.PI / 4 && angle < 3 * Math.PI / 4 -> Direction.DOWN
                            angle >= -3 * Math.PI / 4 && angle < -Math.PI / 4 -> Direction.UP
                            else -> Direction.LEFT
                        }
                        
                        if (currentDirection != direction) {
                            currentDirection = direction
                            onDirectionChange(direction)
                        }
                    },
                    onDragEnd = {
                        // Joystick'i merkeze geri getir
                        joystickKnobPosition = joystickCenter
                        isDragging = false
                        currentDirection = null
                    },
                    onDragCancel = {
                        joystickKnobPosition = joystickCenter
                        isDragging = false
                        currentDirection = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            joystickCenter = Offset(size.width / 2, size.height / 2)
            
            if (joystickKnobPosition == Offset.Zero) {
                joystickKnobPosition = joystickCenter
            }
            
            // Dış daire (joystick alanı)
            drawCircle(
                color = SnakeGreen.copy(alpha = 0.2f),
                center = joystickCenter,
                radius = size.width / 2 - 10f,
                style = Stroke(width = 4f)
            )
            
            // İç daire (joystick knob'ı)
            drawCircle(
                color = if (isDragging) SnakeDarkGreen else SnakeGreen,
                center = joystickKnobPosition,
                radius = 30f
            )
            
            // Yön göstergeleri
            val indicatorRadius = size.width / 2 - 40f
            
            // Yukarı ok
            drawCircle(
                color = if (currentDirection == Direction.UP) SnakeDarkGreen else SnakeGreen.copy(alpha = 0.3f),
                center = Offset(joystickCenter.x, joystickCenter.y - indicatorRadius),
                radius = 15f
            )
            
            // Aşağı ok
            drawCircle(
                color = if (currentDirection == Direction.DOWN) SnakeDarkGreen else SnakeGreen.copy(alpha = 0.3f),
                center = Offset(joystickCenter.x, joystickCenter.y + indicatorRadius),
                radius = 15f
            )
            
            // Sol ok
            drawCircle(
                color = if (currentDirection == Direction.LEFT) SnakeDarkGreen else SnakeGreen.copy(alpha = 0.3f),
                center = Offset(joystickCenter.x - indicatorRadius, joystickCenter.y),
                radius = 15f
            )
            
            // Sağ ok
            drawCircle(
                color = if (currentDirection == Direction.RIGHT) SnakeDarkGreen else SnakeGreen.copy(alpha = 0.3f),
                center = Offset(joystickCenter.x + indicatorRadius, joystickCenter.y),
                radius = 15f
            )
            
            // Yön okları için çizgiler
            drawLine(
                color = SnakeGreen.copy(alpha = 0.5f),
                start = Offset(joystickCenter.x, joystickCenter.y - 20f),
                end = Offset(joystickCenter.x, joystickCenter.y - indicatorRadius + 15f),
                strokeWidth = 2f
            )
            
            drawLine(
                color = SnakeGreen.copy(alpha = 0.5f),
                start = Offset(joystickCenter.x, joystickCenter.y + 20f),
                end = Offset(joystickCenter.x, joystickCenter.y + indicatorRadius - 15f),
                strokeWidth = 2f
            )
            
            drawLine(
                color = SnakeGreen.copy(alpha = 0.5f),
                start = Offset(joystickCenter.x - 20f, joystickCenter.y),
                end = Offset(joystickCenter.x - indicatorRadius + 15f, joystickCenter.y),
                strokeWidth = 2f
            )
            
            drawLine(
                color = SnakeGreen.copy(alpha = 0.5f),
                start = Offset(joystickCenter.x + 20f, joystickCenter.y),
                end = Offset(joystickCenter.x + indicatorRadius - 15f, joystickCenter.y),
                strokeWidth = 2f
            )
            
            // Merkez noktası
            drawCircle(
                color = SnakeGreen.copy(alpha = 0.5f),
                center = joystickCenter,
                radius = 5f
            )
        }
        
        // Yön etiketleri
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Yukarı
            Text(
                text = "YUKARI",
                color = if (currentDirection == Direction.UP) SnakeDarkGreen else Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
            
            // Aşağı
            Text(
                text = "AŞAĞI",
                color = if (currentDirection == Direction.DOWN) SnakeDarkGreen else Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
            
            // Sol
            Text(
                text = "SOL",
                color = if (currentDirection == Direction.LEFT) SnakeDarkGreen else Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
            )
            
            // Sağ
            Text(
                text = "SAĞ",
                color = if (currentDirection == Direction.RIGHT) SnakeDarkGreen else Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            )
        }
    }
}

// Eski DirectionControls composable'ını kaldırıyoruz
// @Composable
// fun DirectionControls(
//     onDirectionChange: (Direction) -> Unit,
//     modifier: Modifier = Modifier
// ) {
//     // Bu fonksiyon artık kullanılmıyor
// }

// Eski DirectionButton composable'ını da kaldırıyoruz
// @Composable
// fun DirectionButton(
//     direction: Direction,
//     iconId: Int,
//     onClick: () -> Unit,
//     modifier: Modifier = Modifier
// ) {
//     // Bu fonksiyon artık kullanılmıyor
// }