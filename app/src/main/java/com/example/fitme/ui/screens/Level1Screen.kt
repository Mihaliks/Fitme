package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.relations.ExerciseWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level1Screen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val plans by viewModel.filteredPlans.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val templates by viewModel.selectedPlanTemplates.collectAsState()
    val templateExercises by viewModel.templateExercises.collectAsState()
    val activePlanId by viewModel.activePlanId.collectAsState()
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }

    if (selectedPlan != null) {
        BackHandler { selectedPlan = null }
    }

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
                            selectedPlan = null
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
                                selectedPlan = plan
                                viewModel.loadTemplatesForPlan(plan.id)
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
                        val isFollowing = selectedPlan?.id == activePlanId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startWorkout(selectedPlan!!.id) },
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
                                    if (isFollowing) viewModel.selectPlanAsActive(null)
                                    else viewModel.selectPlanAsActive(selectedPlan?.id)
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
                        TemplateCard(template, exercises, viewModel)
                    }
                }
            }
        }
    }
}

