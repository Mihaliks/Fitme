package com.example.fitme.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen (val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Workouts : Screen("workouts", "Workouts", Icons.Default.FitnessCenter)
    object Progress : Screen("progress", "Progress", Icons.AutoMirrored.Default.ShowChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)


}