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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.snakegame.R
import com.example.snakegame.ui.theme.SnakeGameTheme
import com.example.snakegame.ui.theme.SnakeGreen
import com.example.snakegame.ui.theme.Typography

@Composable
fun SettingsScreen(
    onBackClicked: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var gameSpeed by remember { mutableStateOf(150f) }
    var gridVisible by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = "Settings Background",
            modifier = Modifier.fillMaxSize(),
            alpha = 0.1f
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Başlık ve geri butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    text = "AYARLAR",
                    style = Typography.displayLarge,
                    color = SnakeGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.size(32.dp)) // Simetri için boşluk
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ayarlar kartları
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ses Ayarları
                SettingsCard(
                    title = "SES AYARLARI",
                    iconId = R.drawable.ic_sound
                ) {
                    SettingsSwitch(
                        text = "Ses Efektleri",
                        isChecked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
                
                // Görsel Ayarlar
                SettingsCard(
                    title = "GÖRSEL AYARLAR",
                    iconId = R.drawable.ic_visual
                ) {
                    SettingsSwitch(
                        text = "Izgara Görünürlüğü",
                        isChecked = gridVisible,
                        onCheckedChange = { gridVisible = it }
                    )
                }
                
                // Oyun Ayarları
                SettingsCard(
                    title = "OYUN AYARLARI",
                    iconId = R.drawable.ic_game
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsSwitch(
                            text = "Titreşim",
                            isChecked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
                        )
                        
                        SettingsSlider(
                            text = "Oyun Hızı",
                            value = gameSpeed,
                            onValueChange = { gameSpeed = it },
                            valueRange = 50f..300f,
                            showValueAsText = true,
                            valueSuffix = "ms"
                        )
                    }
                }
                
                // Hakkında
                SettingsCard(
                    title = "HAKKINDA",
                    iconId = R.drawable.ic_info
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Yılan Oyunu v1.0",
                            style = Typography.bodyLarge,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Klasik yılan oyununun modern bir uygulaması",
                            style = Typography.bodyLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = "© 2024 Tüm hakları saklıdır",
                            style = Typography.bodyLarge,
                            color = Color.LightGray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sıfırlama butonu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray.copy(alpha = 0.3f)
                ),
                onClick = {
                    // Ayarları varsayılana sıfırla
                    soundEnabled = true
                    vibrationEnabled = true
                    gameSpeed = 150f
                    gridVisible = true
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_reset),
                        contentDescription = "Sıfırla",
                        tint = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.size(8.dp))
                    
                    Text(
                        text = "AYARLARI SIFIRLA",
                        style = Typography.titleLarge,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    iconId: Int? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Kart başlığı
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                iconId?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = title,
                        tint = SnakeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                
                Text(
                    text = title,
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SnakeGreen
                )
            }
            
            // İçerik
            content()
        }
    }
}

@Composable
fun SettingsSwitch(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isChecked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = Typography.bodyLarge,
            color = Color.DarkGray
        )
        
        Switch(
            checked = isChecked,
            onCheckedChange = null // null because we handle it in the row's toggleable
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSlider(
    text: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    showValueAsText: Boolean = false,
    valueSuffix: String = ""
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = Typography.bodyLarge,
                color = Color.DarkGray
            )
            
            if (showValueAsText) {
                Text(
                    text = "${value.toInt()}$valueSuffix",
                    style = Typography.bodyLarge,
                    color = SnakeGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SnakeGameTheme {
        SettingsScreen(onBackClicked = {})
    }
}