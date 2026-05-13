package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class WorkoutsSubScreen {
    MAIN, READY_MADE, BY_MUSCLE, CONSTRUCTOR
}

@Composable
fun WorkoutsScreen() {
    var currentSubScreen by remember { mutableStateOf(WorkoutsSubScreen.MAIN) }

    BackHandler(enabled = currentSubScreen != WorkoutsSubScreen.MAIN) {
        currentSubScreen = WorkoutsSubScreen.MAIN
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(targetState = currentSubScreen, label = "WorkoutsNav") { screen ->
            when (screen) {
                WorkoutsSubScreen.MAIN -> WorkoutsMainSelection(onNavigate = { currentSubScreen = it })
                WorkoutsSubScreen.READY_MADE -> ReadyMadeWorkoutsScreen { currentSubScreen = WorkoutsSubScreen.MAIN }
                WorkoutsSubScreen.BY_MUSCLE -> WorkoutsByMuscleScreen { currentSubScreen = WorkoutsSubScreen.MAIN }
                WorkoutsSubScreen.CONSTRUCTOR -> WorkoutConstructorScreen { currentSubScreen = WorkoutsSubScreen.MAIN }
            }
        }
    }
}

@Composable
fun WorkoutsMainSelection(onNavigate: (WorkoutsSubScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Выбери уровень",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        WorkoutCategoryCard(
            title = "lvl 1: Готовые тренировки",
            subtitle = "Для новичков",
            icon = Icons.Default.RocketLaunch,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(WorkoutsSubScreen.READY_MADE) }
        )

        WorkoutCategoryCard(
            title = "lvl 2: По группам мышц",
            subtitle = "Помощник в создании тренировки",
            icon = Icons.Default.FitnessCenter,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(WorkoutsSubScreen.BY_MUSCLE) }
        )

        WorkoutCategoryCard(
            title = "lvl 3: Конструктор тренировки",
            subtitle = "Создай свою программу",
            icon = Icons.Default.AutoAwesome,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(WorkoutsSubScreen.CONSTRUCTOR) }
        )
    }
}

@Composable
fun WorkoutCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyMadeWorkoutsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("lvl 1: Готовые тренировки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Экран готовых тренировок")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsByMuscleScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("lvl 2: По группам мышц") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Экран тренировок по мышцам")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutConstructorScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("lvl 3: Конструктор") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text(text = "Экран конструктора ")
        }
    }
}
