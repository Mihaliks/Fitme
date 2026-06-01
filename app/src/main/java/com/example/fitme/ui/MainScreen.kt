package com.example.fitme.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import com.example.fitme.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.repositories.UserRepository
import com.example.fitme.ui.navigation.Screen
import com.example.fitme.ui.screens.ProgressScreen
import com.example.fitme.ui.screens.RecordsScreen
import com.example.fitme.ui.screens.SettingsScreen
import com.example.fitme.ui.screens.UserSetupScreen
import com.example.fitme.ui.screens.WelcomeScreen
import com.example.fitme.ui.screens.WorkoutHistoryScreen
import com.example.fitme.ui.screens.WorkoutsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppState { WELCOME, SETUP, MAIN }

class AppRootViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val userRepository = UserRepository(db.userDao())

    private val _appState = MutableStateFlow(AppState.WELCOME)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val user = userRepository.getUser()
            _appState.value = if (user != null) AppState.MAIN else AppState.WELCOME
        }
    }

    fun proceedToSetup() {
        _appState.value = AppState.SETUP
    }

    fun completeSetup() {
        _appState.value = AppState.MAIN
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val rootViewModel: AppRootViewModel = viewModel()
    val appState by rootViewModel.appState.collectAsState()

    when (appState) {
        AppState.WELCOME -> {
            WelcomeScreen(onStartClick = { rootViewModel.proceedToSetup() })
        }
        AppState.SETUP -> {
            UserSetupScreen(onSetupComplete = { rootViewModel.completeSetup() })
        }
        AppState.MAIN -> {
            MainAppScaffold()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            AppTopBar(
                currentRoute = currentRoute,
                onSettingsClick = { navigateWithToggle(navController, currentRoute) }
            )
        },
        bottomBar = {
            AppBottomBar(currentRoute = currentRoute, navController = navController)
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentRoute: String?,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

                val logoRes = if (isDarkTheme) {
                    R.drawable.fitness_winged_logo_white_transparent
                } else {
                    R.drawable.fitness_winged_logo_transparent
                }

                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "FitMe logo",
                    modifier = Modifier
                        .width(36.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("FitMe")
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

@Composable
private fun AppBottomBar(
    @Suppress("UNUSED_PARAMETER")
    currentRoute: String?,
    navController: androidx.navigation.NavController
) {
    if (currentRoute == Screen.Progress.route || currentRoute == Screen.Workouts.route) {
        NavigationBar {
            BOTTOM_NAV_ITEMS.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        screen.icon?.let {
                            Icon(it, contentDescription = screen.title)
                        }
                    },
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

@Composable
private fun AppNavHost(
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController as androidx.navigation.NavHostController,
        startDestination = Screen.Progress.route,
        modifier = modifier
    ) {
        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigateToHistory = { navController.navigate(Screen.WorkoutHistory.route) },
                onNavigateToRecords = { navController.navigate(Screen.Records.route) }
            )
        }
        composable(Screen.Workouts.route) { WorkoutsScreen() }
        composable(Screen.Records.route) { RecordsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.WorkoutHistory.route) {
            WorkoutHistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun navigateWithToggle(
    navController: androidx.navigation.NavController,
    currentRoute: String?
) {
    if (currentRoute == Screen.Settings.route) {
        navController.navigate(Screen.Progress.route) {
            popUpTo(Screen.Progress.route) { inclusive = false }
            launchSingleTop = true
        }
    } else {
        navController.navigate(Screen.Settings.route)
    }
}

private val BOTTOM_NAV_ITEMS = listOf(Screen.Progress, Screen.Workouts)

