package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level1Screen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val plans by viewModel.filteredPlans.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPlan by viewModel.selectedPlan.collectAsState()
    val templates by viewModel.selectedPlanTemplates.collectAsState()
    val templateExercises by viewModel.templateExercises.collectAsState()
    val activePlanId by viewModel.activePlanId.collectAsState()

    BackHandler(enabled = selectedPlan != null) { viewModel.selectPlan(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedPlan?.name ?: "lvl 1: Готовые тренировки",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedPlan != null) {
                            viewModel.selectPlan(null)
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (selectedPlan == null) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Поиск планов...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(plans) { plan ->
                        PlanCard(
                            plan = plan,
                            isActive = plan.id == activePlanId,
                            onClick = {
                                viewModel.selectPlan(plan)
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        val currentPlan = selectedPlan ?: return@item
                        val isFollowing = currentPlan.id == activePlanId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startWorkout(currentPlan.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Начать", style = MaterialTheme.typography.titleMedium)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isFollowing) {
                                        viewModel.selectPlanAsActive(null)
                                    } else {
                                        viewModel.activatePlan(currentPlan)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = if (isFollowing) ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Icon(if (isFollowing) Icons.Default.Close else Icons.Default.Check, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (isFollowing) "Перестать следовать" else "Выбрать план")
                            }
                        }
                    }

                    items(templates) { template ->
                         val exercises = templateExercises[template.id] ?: emptyList()
                         Level1TemplateCard(
                             template = template,
                             exercises = exercises
                         )
                     }
                }
            }
        }
    }
}

@Composable
fun Level1TemplateCard(
    template: com.example.fitme.data.entities.WorkoutTemplate,
    exercises: List<com.example.fitme.data.entities.relations.ExerciseWithDetails>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                template.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            exercises.forEach { detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(detail.exercise.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    val duration = detail.exerciseToDo.duration
                    Text(
                        text = if (duration != null && duration > 0) "${detail.exerciseToDo.sets} x $duration сек" else "${detail.exerciseToDo.sets} x ${detail.exerciseToDo.reps}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
