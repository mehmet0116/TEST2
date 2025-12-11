package com.example.snakegame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.snakegame.R
import com.example.snakegame.data.ScoreRepository
import com.example.snakegame.ui.theme.FoodRed
import com.example.snakegame.ui.theme.SnakeGameTheme
import com.example.snakegame.ui.theme.SnakeGreen
import com.example.snakegame.ui.theme.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GameOverScreen(
    score: Int,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    // Skoru kaydet
    LaunchedEffect(score) {
        withContext(Dispatchers.IO) {
            ScoreRepository.saveScore(score)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_game_over),
            contentDescription = "Game Over Background",
            modifier = Modifier.fillMaxSize(),
            alpha = 0.1f
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Oyun Bitti başlığı
            Text(
                text = "OYUN BİTTİ",
                style = Typography.displayLarge,
                color = FoodRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Skor gösterimi
            Text(
                text = "SKORUNUZ",
                style = Typography.titleLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = score.toString(),
                style = Typography.displayLarge.copy(fontSize = 48.sp),
                color = SnakeGreen,
                modifier = Modifier.padding(bottom = 40.dp)
            )
            
            // Tebrik mesajı
            Text(
                text = getCongratulationsMessage(score),
                style = Typography.bodyLarge,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 40.dp)
            )
            
            // Tekrar Oyna butonu
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnakeGreen,
                    contentColor = Color.White
                )
            ) {
                Text(text = "TEKRAR OYNA", style = Typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ana Menü butonu
            Button(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.DarkGray
                )
            ) {
                Text(text = "ANA MENÜ", style = Typography.titleLarge)
            }
        }
    }
}

private fun getCongratulationsMessage(score: Int): String {
    return when {
        score >= 500 -> "MÜTHİŞ! Profesyonel bir yılan oyuncususunuz! 🐍"
        score >= 300 -> "Harika! Çok iyi bir skor elde ettiniz! 🎯"
        score >= 200 -> "Tebrikler! İyi bir oyun çıkardınız! ⭐"
        score >= 100 -> "Güzel oynadınız! Daha iyisini yapabilirsiniz! 👍"
        else -> "İlk deneme için fena değil! Tekrar deneyin! 😊"
    }
}

@Preview(showBackground = true)
@Composable
fun GameOverScreenPreview() {
    SnakeGameTheme {
        GameOverScreen(
            score = 250,
            onPlayAgain = {},
            onBackToMenu = {}
        )
    }
}