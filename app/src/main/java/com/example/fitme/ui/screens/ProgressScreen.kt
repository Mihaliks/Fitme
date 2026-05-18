package com.example.fitme.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.time.LocalTime

@Composable
fun ProgressScreen(onNavigateToHistory: () -> Unit = {}) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val activePlanId by viewModel.activePlanId.collectAsState()
    val plans by viewModel.activePlans.collectAsState()
    val builtInPlans by viewModel.filteredPlans.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val history by viewModel.workoutHistory.collectAsState()
    val user by viewModel.user.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    if (currentSession != null) {
        WorkoutSessionScreen(viewModel = viewModel)
        return
    }

    val activePlan = plans.find { it.id == activePlanId } ?: builtInPlans.find { it.id == activePlanId }
    val nextWorkoutPreview by viewModel.nextWorkoutPreview.collectAsState()

    val greeting = when (LocalTime.now().hour) {
        in 6..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..23 -> "Добрый вечер"
        else -> "Доброй ночи"
    }
    val userName = user?.name ?: ""
    val welcomeText = if (userName.isNotEmpty()) "$greeting, $userName!" else "$greeting!"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = welcomeText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ваш Прогресс",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateToHistory) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "История тренировок",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (activePlan != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Текущий план:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = activePlan.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (nextWorkoutPreview != null) {
                        Text(
                            text = "Следующая тренировка:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = nextWorkoutPreview!!.template.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Упражнения (${nextWorkoutPreview!!.exercises.size}):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        nextWorkoutPreview!!.exercises.take(3).forEach { exp ->
                            Text(
                                text = "- ${exp.exercise.name} ${if(exp.plannedSets > 0) "(${exp.plannedSets}x${exp.plannedReps})" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (nextWorkoutPreview!!.exercises.size > 3) {
                            Text(
                                text = "...и ещё ${nextWorkoutPreview!!.exercises.size - 3}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.startWorkout(activePlan.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Начать тренировку")
                        }
                    } else {
                        Text("Нет доступных тренировок в этом плане.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "План тренировок не выбран",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AchievementsSection(history = history)
    }
}