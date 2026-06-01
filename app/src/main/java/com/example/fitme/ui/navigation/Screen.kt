package com.example.fitme.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen (val route: String, val title: String, val icon: ImageVector? = null) {
    object Workouts : Screen("workouts", "Тренировки", Icons.Default.FitnessCenter)
    object Progress : Screen("progress", "Прогресс", Icons.AutoMirrored.Default.ShowChart)
    object Records : Screen("records", "Рекорды", Icons.Default.EmojiEvents)
    object WorkoutHistory : Screen("workoutHistory", "История", Icons.Default.History)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)

    object Welcome : Screen("welcome", "Welcome")
}