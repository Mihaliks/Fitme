package com.example.fitme.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitme.ui.theme.ColorVariant
import com.example.fitme.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val colorVariant by viewModel.colorVariant.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SettingsSection(title = "Тема оформления", icon = Icons.Default.SettingsBrightness) {
                Column {
                    ThemeModeOption("Системная", ThemeMode.SYSTEM, themeMode) { viewModel.setThemeMode(it) }
                    ThemeModeOption("Светлая", ThemeMode.LIGHT, themeMode) { viewModel.setThemeMode(it) }
                    ThemeModeOption("Тёмная", ThemeMode.DARK, themeMode) { viewModel.setThemeMode(it) }
                }
            }
        }

        item {
            SettingsSection(title = "Цветовая схема", icon = Icons.Default.Palette) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ColorOption(Color(0xFF6650a4), ColorVariant.PURPLE, colorVariant) { viewModel.setColorVariant(it) }
                    ColorOption(Color(0xFF3B7D64), ColorVariant.GREEN, colorVariant) { viewModel.setColorVariant(it) }
                    ColorOption(Color(0xFF4361EE), ColorVariant.BLUE, colorVariant) { viewModel.setColorVariant(it) }
                    ColorOption(Color(0xFFE76F51), ColorVariant.ORANGE, colorVariant) { viewModel.setColorVariant(it) }
                }
            }
        }

        item {
            SettingsSection(title = "Обратная связь", icon = Icons.Default.Email) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "FitMe Feedback")
                        }
                        context.startActivity(Intent.createChooser(intent, "Отправить письмо"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Написать разработчикам")
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun ThemeModeOption(
    label: String,
    mode: ThemeMode,
    selectedMode: ThemeMode,
    onClick: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(mode) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = mode == selectedMode, onClick = { onClick(mode) })
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ColorOption(
    color: Color,
    variant: ColorVariant,
    selectedVariant: ColorVariant,
    onClick: (ColorVariant) -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick(variant) }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (variant == selectedVariant) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}
