package com.example.snakegame.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snakegame.ui.theme.SnakeGameTheme

@Composable
fun MainMenuScreen(
    onPlayClicked: () -> Unit,
    onScoreboardClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onExitClicked: () -> Unit
) {
    // Animasyon için scale
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo/Başlık
            Text(
                text = "🐍 YILAN OYUNU",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(scale)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Klasik Yılan Oyunu",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Oyun Başlat Butonu (Vurgulu)
            MenuButton(
                text = "OYUNA BAŞLA",
                icon = Icons.Default.PlayArrow,
                onClick = onPlayClicked,
                isPrimary = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Skor Tablosu
            MenuButton(
                text = "SKOR TABLOSU",
                icon = Icons.Default.Star,
                onClick = onScoreboardClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Ayarlar
            MenuButton(
                text = "AYARLAR",
                icon = Icons.Default.Settings,
                onClick = onSettingsClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Çıkış
            TextButton(
                onClick = onExitClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Çıkış",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ÇIKIŞ",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Versiyon bilgisi
            Text(
                text = "v1.0.0 • 2025",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isPrimary)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 8.dp else 4.dp,
            pressedElevation = 12.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (isPrimary) 18.sp else 16.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    SnakeGameTheme {
        MainMenuScreen(
            onPlayClicked = {},
            onScoreboardClicked = {},
            onSettingsClicked = {},
            onExitClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenDarkPreview() {
    SnakeGameTheme(darkTheme = true) {
        MainMenuScreen(
            onPlayClicked = {},
            onScoreboardClicked = {},
            onSettingsClicked = {},
            onExitClicked = {}
        )
    }
}

