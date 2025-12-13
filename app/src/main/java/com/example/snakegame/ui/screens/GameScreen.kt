package com.example.snakegame.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.snakegame.game.Direction
import com.example.snakegame.viewmodel.GameViewModel
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, navController: NavController) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val gridSize = 20

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Skor: ${gameState.score}")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePause() }) {
                        Icon(
                            if (gameState.isPaused) Icons.Default.PlayArrow else Icons.Default.Build,
                            if (gameState.isPaused) "Devam" else "Duraklat"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Oyun Alanı
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellSize = size.width / gridSize

                    // Grid çizgileri
                    for (i in 0..gridSize) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(i.toFloat() * cellSize, 0f),
                            end = Offset(i.toFloat() * cellSize, size.height),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, i.toFloat() * cellSize),
                            end = Offset(size.width, i.toFloat() * cellSize),
                            strokeWidth = 1f
                        )
                    }

                    // Yılan çiz
                    gameState.snake.forEachIndexed { index, segment ->
                        val isHead = index == 0
                        drawRoundRect(
                            color = if (isHead)
                                Color(0xFF4CAF50)
                            else
                                Color(0xFF81C784),
                            topLeft = Offset(
                                segment.x.toFloat() * cellSize + 2,
                                segment.y.toFloat() * cellSize + 2
                            ),
                            size = Size(cellSize - 4, cellSize - 4),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                    }

                    // Yemek çiz
                    drawCircle(
                        color = Color(0xFFF44336),
                        center = Offset(
                            gameState.food.x.toFloat() * cellSize + cellSize / 2,
                            gameState.food.y.toFloat() * cellSize + cellSize / 2
                        ),
                        radius = cellSize / 2 - 3
                    )
                }

                // Game Over Ekranı
                if (gameState.isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "OYUN BİTTİ!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Skor: ${gameState.score}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        viewModel.startNewGame()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Tekrar Oyna")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ana Menü")
                                }
                            }
                        }
                    }
                }
            }

            // DAİRESEL JOYSTICK KONTROL
            CircularJoystick(
                onDirectionChange = { direction ->
                    direction?.let { viewModel.changeDirection(it) }
                },
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun CircularJoystick(
    onDirectionChange: (Direction?) -> Unit,
    modifier: Modifier = Modifier
) {
    var joystickCenter by remember { mutableStateOf(Offset.Zero) }
    var knobPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val joystickRadius = 100.dp.value
    val knobRadius = 40.dp.value

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        joystickCenter = Offset(size.width / 2f, size.height / 2f)
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        // Knob pozisyonunu güncelle
                        val newPosition = knobPosition + dragAmount
                        val centerOffset = newPosition - joystickCenter

                        // Mesafeyi hesapla
                        val distance = sqrt(
                            centerOffset.x.pow(2) + centerOffset.y.pow(2)
                        )

                        // Joystick sınırları içinde tut
                        knobPosition = if (distance <= joystickRadius - knobRadius) {
                            joystickCenter + centerOffset
                        } else {
                            val angle = atan2(centerOffset.y, centerOffset.x)
                            val maxDistance = joystickRadius - knobRadius
                            joystickCenter + Offset(
                                kotlin.math.cos(angle) * maxDistance,
                                kotlin.math.sin(angle) * maxDistance
                            )
                        }

                        // Yönü belirle
                        val direction = getDirectionFromOffset(knobPosition - joystickCenter)
                        onDirectionChange(direction)
                    },
                    onDragEnd = {
                        knobPosition = joystickCenter
                        isDragging = false
                        onDirectionChange(null)
                    },
                    onDragCancel = {
                        knobPosition = joystickCenter
                        isDragging = false
                        onDirectionChange(null)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)

            if (joystickCenter == Offset.Zero) {
                joystickCenter = center
                knobPosition = center
            }

            // Dış halka (joystick base)
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = joystickRadius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // İç halka (merkez göstergesi)
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = joystickRadius / 2,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Yön çizgileri
            val lineLength = joystickRadius * 0.7f
            // Yukarı
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = center - Offset(0f, lineLength),
                end = center - Offset(0f, joystickRadius * 0.3f),
                strokeWidth = 3.dp.toPx()
            )
            // Aşağı
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = center + Offset(0f, lineLength),
                end = center + Offset(0f, joystickRadius * 0.3f),
                strokeWidth = 3.dp.toPx()
            )
            // Sol
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = center - Offset(lineLength, 0f),
                end = center - Offset(joystickRadius * 0.3f, 0f),
                strokeWidth = 3.dp.toPx()
            )
            // Sağ
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = center + Offset(lineLength, 0f),
                end = center + Offset(joystickRadius * 0.3f, 0f),
                strokeWidth = 3.dp.toPx()
            )

            // Kontrol topu (knob)
            drawCircle(
                color = if (isDragging)
                    Color(0xFF4CAF50)
                else
                    Color.Gray.copy(alpha = 0.6f),
                radius = knobRadius,
                center = knobPosition
            )

            // Knob içi - daha belirgin olması için
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = knobRadius * 0.6f,
                center = knobPosition
            )
        }
    }
}

private fun getDirectionFromOffset(offset: Offset): Direction? {
    val threshold = 20f // Minimum hareket mesafesi

    if (sqrt(offset.x.pow(2) + offset.y.pow(2)) < threshold) {
        return null
    }

    val angle = atan2(offset.y, offset.x)
    val degrees = Math.toDegrees(angle.toDouble())

    return when {
        degrees > -45 && degrees <= 45 -> Direction.RIGHT
        degrees > 45 && degrees <= 135 -> Direction.DOWN
        degrees > -135 && degrees <= -45 -> Direction.UP
        else -> Direction.LEFT
    }
}

