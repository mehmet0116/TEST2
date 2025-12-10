package com.snakegame.pro

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.snakegame.pro.ui.theme.SnakeGameProTheme

class GameActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Tam ekran ve ekranın sürekli açık kalması
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            SnakeGameProTheme {
                GameScreen()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Oyun duraklatıldığında ekranın açık kalmasını durdur
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun GameScreen() {
    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gameState
    val snake by viewModel.snake
    val food by viewModel.food
    val obstacles by viewModel.obstacles
    val score by viewModel.score
    val gameSpeed by viewModel.gameSpeed
    val isGameOver by viewModel.isGameOver
    val isPaused by viewModel.isPaused
    val context = LocalContext.current
    
    // Oyun döngüsü
    LaunchedEffect(gameSpeed, isPaused, isGameOver) {
        while (true) {
            if (!isPaused && !isGameOver) {
                viewModel.moveSnake()
                delay(gameSpeed)
            } else {
                delay(100)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Oyun alanı
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        viewModel.handleTap(offset, size)
                    }
                }
        ) {
            // Arka plan grid'i çiz
            drawGrid()
            
            // Engelleri çiz
            obstacles.forEach { obstacle ->
                drawObstacle(obstacle)
            }
            
            // Yemi çiz
            drawFood(food)
            
            // Yılanı çiz
            drawSnake(snake)
        }
        
        // UI Kontrolleri
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skor paneli
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SKOR: $score",
                    color = Color.Green,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "HIZ: ${viewModel.getSpeedLevel()}",
                    color = Color.Yellow,
                    fontSize = 20.sp
                )
                
                Text(
                    text = "UZUNLUK: ${snake.size}",
                    color = Color.Cyan,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Kontrol butonları
            if (!isGameOver) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Yukarı
                    Button(
                        onClick = { viewModel.changeDirection(GameViewModel.Direction.UP) },
                        modifier = Modifier.width(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("↑", fontSize = 24.sp)
                    }
                    
                    Row {
                        // Sol
                        Button(
                            onClick = { viewModel.changeDirection(GameViewModel.Direction.LEFT) },
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("←", fontSize = 24.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        // Sağ
                        Button(
                            onClick = { viewModel.changeDirection(GameViewModel.Direction.RIGHT) },
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("→", fontSize = 24.sp)
                        }
                    }
                    
                    // Aşağı
                    Button(
                        onClick = { viewModel.changeDirection(GameViewModel.Direction.DOWN) },
                        modifier = Modifier.width(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("↓", fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Kontrol butonları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.togglePause() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPaused) Color.Green else Color.Yellow
                            )
                        ) {
                            Text(if (isPaused) "DEVAM" else "DURAKLAT")
                        }
                        
                        Button(
                            onClick = { viewModel.resetGame() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text("YENİDEN")
                        }
                    }
                }
            } else {
                // Game Over ekranı
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OYUN BİTTİ!",
                        color = Color.Red,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Skorunuz: $score",
                        color = Color.Yellow,
                        fontSize = 28.sp
                    )
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Button(
                        onClick = { viewModel.resetGame() },
                        modifier = Modifier.width(200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green
                        )
                    ) {
                        Text("TEKRAR DENE", fontSize = 20.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Hız kontrolü
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.decreaseSpeed() },
                    modifier = Modifier.width(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue
                    )
                ) {
                    Text("YAVAŞ")
                }
                
                Button(
                    onClick = { viewModel.increaseSpeed() },
                    modifier = Modifier.width(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Magenta
                    )
                ) {
                    Text("HIZLI")
                }
            }
        }
    }
}

// Grid çizimi
private fun DrawScope.drawGrid() {
    val gridSize = 40f
    val gridColor = Color(0x22FFFFFF)
    
    // Dikey çizgiler
    for (x in 0..size.width.toInt() step gridSize.toInt()) {
        drawLine(
            color = gridColor,
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), size.height),
            strokeWidth = 1f
        )
    }
    
    // Yatay çizgiler
    for (y in 0..size.height.toInt() step gridSize.toInt()) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y.toFloat()),
            end = Offset(size.width, y.toFloat()),
            strokeWidth = 1f
        )
    }
}

// Yılan çizimi
private fun DrawScope.drawSnake(snake: List<Pair<Int, Int>>) {
    val cellSize = 40f
    
    snake.forEachIndexed { index, (x, y) ->
        val color = if (index == 0) {
            // Baş için farklı renk
            Color.Green.copy(alpha = 0.9f)
        } else {
            // Vücut için gradient renk
            val progress = index.toFloat() / snake.size
            Color(
                red = 0.2f + progress * 0.8f,
                green = 0.8f - progress * 0.3f,
                blue = 0.2f
            )
        }
        
        drawRoundRect(
            color = color,
            topLeft = Offset(x * cellSize, y * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = if (index == 0) 8.dp.toPx() else 4.dp.toPx()
        )
        
        // Gözler (baş için)
        if (index == 0) {
            val eyeSize = cellSize / 5
            // Sol göz
            drawCircle(
                color = Color.Black,
                center = Offset(x * cellSize + cellSize * 0.3f, y * cellSize + cellSize * 0.3f),
                radius = eyeSize
            )
            // Sağ göz
            drawCircle(
                color = Color.Black,
                center = Offset(x * cellSize + cellSize * 0.7f, y * cellSize + cellSize * 0.3f),
                radius = eyeSize
            )
        }
    }
}

// Yem çizimi
private fun DrawScope.drawFood(food: Pair<Int, Int>) {
    val cellSize = 40f
    val (x, y) = food
    
    // Yem için gradient etkisi
    val colors = listOf(Color.Red, Color.Yellow)
    for (i in 0 until 5) {
        val radius = cellSize / 2 * (1 - i * 0.1f)
        val color = colors[i % colors.size].copy(alpha = 1f - i * 0.15f)
        
        drawCircle(
            color = color,
            center = Offset(x * cellSize + cellSize / 2, y * cellSize + cellSize / 2),
            radius = radius
        )
    }
    
    // Yem için parlaklık efekti
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        center = Offset(x * cellSize + cellSize * 0.3f, y * cellSize + cellSize * 0.3f),
        radius = cellSize / 8
    )
}

// Engel çizimi
private fun DrawScope.drawObstacle(obstacle: Pair<Int, Int>) {
    val cellSize = 40f
    val (x, y) = obstacle
    
    drawRect(
        color = Color.Gray.copy(alpha = 0.7f),
        topLeft = Offset(x * cellSize, y * cellSize),
        size = Size(cellSize, cellSize)
    )
    
    // Engel deseni
    drawLine(
        color = Color.DarkGray,
        start = Offset(x * cellSize, y * cellSize),
        end = Offset(x * cellSize + cellSize, y * cellSize + cellSize),
        strokeWidth = 3f
    )
    
    drawLine(
        color = Color.DarkGray,
        start = Offset(x * cellSize + cellSize, y * cellSize),
        end = Offset(x * cellSize, y * cellSize + cellSize),
        strokeWidth = 3f
    )
}