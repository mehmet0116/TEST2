package com.snakegame.pro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.snakegame.pro.ui.theme.SnakeGameProTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var adView: AdView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SnakeGameProTheme {
                MainScreen(
                    onPlayClick = { startGame() },
                    onSettingsClick = { openSettings() },
                    onAboutClick = { openAbout() },
                    onExitClick = { finish() }
                )
            }
        }
    }
    
    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
    
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun openAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        adView.resume()
    }
    
    override fun onPause() {
        adView.pause()
        super.onPause()
    }
    
    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}

@Composable
fun MainScreen(
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val context = LocalContext.current
    var showAd by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Reklam yüklenmesi simülasyonu
        kotlinx.coroutines.delay(1000)
        showAd = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF283593),
                        Color(0xFF303F9F)
                    )
                )
            )
    ) {
        // Arka plan resmi
        Image(
            painter = painterResource(id = R.drawable.bg_main),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "🐍 SNAKE GAME PRO 🐍",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )
            
            // Oyun logosu
            Image(
                painter = painterResource(id = R.drawable.ic_snake_logo),
                contentDescription = "Snake Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 30.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Oyna Butonu
            GameButton(
                text = "OYNA",
                onClick = onPlayClick,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ayarlar Butonu
            GameButton(
                text = "AYARLAR",
                onClick = onSettingsClick,
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hakkında Butonu
            GameButton(
                text = "HAKKINDA",
                onClick = onAboutClick,
                color = Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Çıkış Butonu
            GameButton(
                text = "ÇIKIŞ",
                onClick = onExitClick,
                color = Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // En Yüksek Skor
            val highScore = remember { mutableStateOf(0) }
            Text(
                text = "En Yüksek Skor: ${highScore.value}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Yellow
            )
            
            // Reklam banner'ı
            if (showAd) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(top = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.3))
                ) {
                    Text(
                        text = "Reklam Alanı",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}