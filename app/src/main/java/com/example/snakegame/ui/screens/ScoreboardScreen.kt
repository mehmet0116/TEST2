package com.example.snakegame.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.snakegame.R
import com.example.snakegame.data.ScoreRepository
import com.example.snakegame.ui.theme.SnakeGameTheme
import com.example.snakegame.ui.theme.SnakeGreen
import com.example.snakegame.ui.theme.Typography
import kotlinx.coroutines.flow.flowOf

@Composable
fun ScoreboardScreen(
    onBackClicked: () -> Unit
) {
    val highScore by ScoreRepository.highScore.collectAsState(initial = 0)
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_scoreboard),
            contentDescription = "Scoreboard Background",
            modifier = Modifier.fillMaxSize(),
            alpha = 0.1f
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık ve geri butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Geri",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Text(
                    text = "SKOR TABLOSU",
                    style = Typography.displayLarge,
                    color = SnakeGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.size(32.dp)) // Simetri için boşluk
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // En yüksek skor kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SnakeGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EN YÜKSEK SKOR",
                        style = Typography.titleLarge,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = highScore.toString(),
                        style = Typography.displayLarge.copy(fontSize = 48.sp),
                        color = SnakeGreen,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = getHighScoreMessage(highScore),
                        style = Typography.bodyLarge,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Son skorlar (basit liste - gerçek uygulamada daha fazla veri olabilir)
            Text(
                text = "SON SKORLAR",
                style = Typography.titleLarge,
                color = SnakeGreen,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            val recentScores = remember { listOf(highScore, highScore / 2, highScore / 3).filter { it > 0 } }
            
            if (recentScores.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(recentScores) { index, score ->
                        ScoreItem(
                            rank = index + 1,
                            score = score,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Henüz skor kaydedilmedi.\nOyun oynayarak skorunuzu kaydedin!",
                    style = Typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ana menü butonu
            Button(
                onClick = onBackClicked,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "ANA MENÜ", style = Typography.titleLarge)
            }
        }
    }
}

@Composable
fun ScoreItem(
    rank: Int,
    score: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sıralama
            Text(
                text = "#$rank",
                style = Typography.titleLarge,
                color = if (rank == 1) SnakeGreen else Color.Gray
            )
            
            // Skor
            Text(
                text = score.toString(),
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = SnakeGreen
            )
        }
    }
}

private fun getHighScoreMessage(score: Int): String {
    return when {
        score >= 500 -> "Efsanevi Seviye! 🏆"
        score >= 300 -> "Profesyonel Oyunçu! ⭐"
        score >= 200 -> "Harika Performans! 🎯"
        score >= 100 -> "İyi Skor! 👍"
        score > 0 -> "Başlangıç Seviyesi 🐍"
        else -> "Henüz skor yok"
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreboardScreenPreview() {
    SnakeGameTheme {
        ScoreboardScreen(onBackClicked = {})
    }
}