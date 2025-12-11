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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.snakegame.R
import com.example.snakegame.ui.theme.SnakeGameTheme
import com.example.snakegame.ui.theme.SnakeGreen
import com.example.snakegame.ui.theme.Typography

@Composable
fun MainMenuScreen(
    onPlayClicked: () -> Unit,
    onScoreboardClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onExitClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Arka plan deseni (basit bir çizim)
        Image(
            painter = painterResource(id = R.drawable.ic_snake_pattern),
            contentDescription = "Snake Pattern Background",
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
            // Başlık
            Text(
                text = "YILAN OYUNU",
                style = Typography.displayLarge,
                color = SnakeGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )
            
            // Yılan ikonu
            Image(
                painter = painterResource(id = R.drawable.ic_snake_head),
                contentDescription = "Snake Icon",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 40.dp)
            )
            
            // Oyna Butonu
            MenuButton(
                text = "OYNA",
                onClick = onPlayClicked,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skor Tablosu Butonu
            MenuButton(
                text = "SKOR TABLOSU",
                onClick = onScoreboardClicked,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ayarlar Butonu
            MenuButton(
                text = "AYARLAR",
                onClick = onSettingsClicked,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Çıkış Butonu
            Button(
                onClick = onExitClicked,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.DarkGray
                )
            ) {
                Text(text = "ÇIKIŞ", style = Typography.titleLarge)
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SnakeGreen,
            contentColor = Color.White
        )
    ) {
        Text(text = text, style = Typography.titleLarge)
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