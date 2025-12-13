package com.example.snakegame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snakegame.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClicked: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel()
    val scope = rememberCoroutineScope()

    // Ayarları topla
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState(initial = true)
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState(initial = true)
    val gameSpeed by viewModel.gameSpeed.collectAsState(initial = 150f)
    val isGridVisible by viewModel.isGridVisible.collectAsState(initial = true)

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // TEMA AYARLARI
            SettingsSection(title = "🎨 TEMA") {
                SettingsSwitch(
                    title = "Karanlık Mod",
                    description = if (isDarkMode) "Karanlık tema aktif" else "Açık tema aktif",
                    checked = isDarkMode,
                    icon = if (isDarkMode) Icons.Default.Star else Icons.Default.Star,
                    onCheckedChange = {
                        scope.launch {
                            viewModel.setDarkMode(it)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OYUN AYARLARI
            SettingsSection(title = "🎮 OYUN AYARLARI") {

                // Oyun Hızı
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Oyun Hızı",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = getSpeedLabel(gameSpeed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = gameSpeed,
                        onValueChange = {
                            scope.launch {
                                viewModel.setGameSpeed(it)
                            }
                        },
                        valueRange = 50f..300f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Grid Görünürlüğü
                SettingsSwitch(
                    title = "Grid Çizgileri",
                    description = if (isGridVisible) "Grid çizgileri gösteriliyor" else "Grid çizgileri gizli",
                    checked = isGridVisible,
                    icon = Icons.Default.Check,
                    onCheckedChange = {
                        scope.launch {
                            viewModel.setGridVisible(it)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SES VE TİTREŞİM
            SettingsSection(title = "🔊 SES VE TİTREŞİM") {
                SettingsSwitch(
                    title = "Ses Efektleri",
                    description = if (isSoundEnabled) "Sesler açık" else "Sesler kapalı",
                    checked = isSoundEnabled,
                    icon = if (isSoundEnabled) Icons.Default.Check else Icons.Default.Close,
                    onCheckedChange = {
                        scope.launch {
                            viewModel.setSoundEnabled(it)
                        }
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                SettingsSwitch(
                    title = "Titreşim",
                    description = if (isVibrationEnabled) "Titreşim açık" else "Titreşim kapalı",
                    checked = isVibrationEnabled,
                    icon = Icons.Default.Phone,
                    onCheckedChange = {
                        scope.launch {
                            viewModel.setVibrationEnabled(it)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SIFIRLA BUTONU
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ayarları Sıfırla")
            }
        }
    }

    // Sıfırlama Onay Dialogu
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Ayarları Sıfırla") },
            text = { Text("Tüm ayarlar varsayılan değerlerine döndürülecek. Emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.resetSettings()
                        }
                        showResetDialog = false
                    }
                ) {
                    Text("Sıfırla", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun getSpeedLabel(speed: Float): String {
    return when {
        speed < 100f -> "⚡ Çok Hızlı"
        speed < 150f -> "🚀 Hızlı"
        speed < 200f -> "🎯 Normal"
        speed < 250f -> "🐌 Yavaş"
        else -> "🐢 Çok Yavaş"
    }
}
