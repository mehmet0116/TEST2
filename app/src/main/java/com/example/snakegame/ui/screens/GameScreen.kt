package com.example.snakegame.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snakegame.R
import com.example.snakegame.game.Direction
import com.example.snakegame.game.SnakeGame
import com.example.snakegame.ui.theme.BackgroundBeige
import com.example.snakegame.ui.theme.FoodRed
import com.example.snakegame.ui.theme.GameGridDark
import com.example.snakegame.ui.theme.GameGridLight
import com.example.snakegame.ui.theme.SnakeDarkGreen
import com.example.snakegame.ui.theme.SnakeGreen
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun GameScreen(
    onGameOver: (Int) -> Unit,
    onBackToMenu: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    
    // Oyun tahtası boyutunu ekrana uygun şekilde ayarla
    val gridSize = min(screenWidth, screenHeight * 0.7f).toInt()
    val cellSize = gridSize / 20 // 20x20 grid
    
    val game = remember { SnakeGame() }
    val gameState by game.gameState
    
    // Oyun döngüsü
    LaunchedEffect(Unit) {
        while (true) {
            if (!gameState.isPaused && !gameState.isGameOver) {
                game.update()
                delay(150) // Oyun hızı
            } else {
                delay(100)
            }
        }
    }
    
    // Oyun bittiğinde callback'i tetikle
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
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
                        
                        drawRect(
                            color = snakeColor,
                            topLeft = Offset(
                                position.x * cellWidth,
                                position.y * cellHeight
                            ),
                            size = Size(cellWidth, cellHeight)
                        )
                        
                        // Baş için gözler
                        if (isHead) {
                            val eyeSize = cellWidth * 0.2f
                            val eyeOffset = cellWidth * 0.3f
                            
                            drawCircle(
                                color = Color.White,
                                center = Offset(
                                    position.x * cellWidth + eyeOffset,
                                    position.y * cellHeight + eyeOffset
                                ),
                                radius = eyeSize
                            )
                            drawCircle(
                                color = Color.White,
                                center = Offset(
                                    (position.x + 1) * cellWidth - eyeOffset,
                                    position.y * cellHeight + eyeOffset
                                ),
                                radius = eyeSize
                            )
                        }
                    }
                    
                    // Yemeği çiz
                    drawCircle(
                        color = FoodRed,
                        center = Offset(
                            gameState.food.x * cellWidth + cellWidth / 2,
                            gameState.food.y * cellHeight + cellHeight / 2
                        ),
                        radius = cellWidth * 0.4f
                    )
                    
                    // Yılan yemek yediğinde efekti göster
                    if (gameState.foodEaten) {
                        drawCircle(
                            color = FoodRed.copy(alpha = 0.5f),
                            center = Offset(
                                gameState.food.x * cellWidth + cellWidth / 2,
                                gameState.food.y * cellHeight + cellHeight / 2
                            ),
                            radius = cellWidth * 0.6f,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Yön kontrol butonları
            DirectionControls(
                onDirectionChange = { direction ->
                    if (!gameState.isPaused && !gameState.isGameOver) {
                        game.setDirection(direction)
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
                Text(
                    text = "OYUN DURDURULDU",
                    color = Color.Gray,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(8.dp)
                )
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
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Geri",
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Skor gösterimi
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "SKOR", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = SnakeGreen
            )
        }
        
        // Duraklat/Devam et butonu
        IconButton(onClick = onPauseToggle) {
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
        IconButton(
            onClick = { onDirectionChange(Direction.UP) },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_up),
                contentDescription = "Yukarı",
                modifier = Modifier.size(48.dp)
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sol butonu
            IconButton(
                onClick = { onDirectionChange(Direction.LEFT) },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_left),
                    contentDescription = "Sol",
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.size(64.dp))
            
            // Sağ butonu
            IconButton(
                onClick = { onDirectionChange(Direction.RIGHT) },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_right),
                    contentDescription = "Sağ",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        // Aşağı butonu
        IconButton(
            onClick = { onDirectionChange(Direction.DOWN) },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_down),
                contentDescription = "Aşağı",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen(
        onGameOver = {},
        onBackToMenu = {}
    )
}