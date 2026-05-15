package com.example.fitme.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitme.ui.navigation.Screen
import com.example.fitme.ui.screens.ProgressScreen
import com.example.fitme.ui.screens.SettingsScreen
import com.example.fitme.ui.screens.WelcomeScreen
import com.example.fitme.ui.screens.WorkoutsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("fitme_prefs", Context.MODE_PRIVATE) }

    val isFirstLaunch = remember { 
        mutableStateOf(sharedPreferences.getBoolean("is_first_launch", true)) 
    }

    if (isFirstLaunch.value) {
        WelcomeScreen(onStartClick = {
            sharedPreferences.edit { putBoolean("is_first_launch", false) }
            isFirstLaunch.value = false
        })
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FitMe") },
                    actions = {
                        IconButton(onClick = {
                            if (currentRoute == Screen.Settings.route) {
                                navController.navigate(Screen.Progress.route) {
                                    popUpTo(Screen.Progress.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(Screen.Settings.route)
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            bottomBar = {
                if (currentRoute == Screen.Progress.route || currentRoute == Screen.Workouts.route) {
                    NavigationBar {
                        val items = listOf(Screen.Progress, Screen.Workouts)
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Progress.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Progress.route) { ProgressScreen() }
                composable(Screen.Workouts.route) { WorkoutsScreen() }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }
    }
}
