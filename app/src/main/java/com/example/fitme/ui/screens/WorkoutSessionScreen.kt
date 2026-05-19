package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutsViewModel
) {
    val session by viewModel.currentSession.collectAsState()
    val currentIndex by viewModel.currentExerciseIndex.collectAsState()
    val periodizationDisplayEnabled by viewModel.periodizationDisplayEnabled.collectAsState()

    val currentSession = session ?: return

    var showEditDialog by remember { mutableStateOf(false) }
    var editedSets by remember { mutableStateOf(currentSession.exercises.getOrNull(currentIndex)?.plannedSets ?: 0) }
    var editedReps by remember { mutableStateOf(currentSession.exercises.getOrNull(currentIndex)?.plannedReps ?: 0) }

    LaunchedEffect(currentIndex, currentSession) {
        editedSets = currentSession.exercises.getOrNull(currentIndex)?.plannedSets ?: 0
        editedReps = currentSession.exercises.getOrNull(currentIndex)?.plannedReps ?: 0
    }

    BackHandler {
        viewModel.finishSession()
    }

    if (showEditDialog) {
        EditSetsRepsDialog(
            sets = editedSets,
            reps = editedReps,
            onConfirm = { sets, reps ->
                viewModel.updateCurrentExercisePlanned(currentIndex, sets, reps)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    val progress = if (currentSession.exercises.isNotEmpty()) {
        (currentIndex + 1) / currentSession.exercises.size.toFloat()
    } else {
        0f
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentSession.template.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Упражнение ${currentIndex + 1} из ${currentSession.exercises.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.finishSession() }) {
                        Icon(Icons.Default.Close, contentDescription = "Выход")
                    }
                }
            )
        }
    ) { padding ->
        val exercise = currentSession.exercises.getOrNull(currentIndex)

        if (exercise != null) {
            val exerciseToDo = exercise.exerciseToDo
            val duration = exerciseToDo.duration
            val isTimeBased = duration != null && duration > 0
            val modeLabel = exercise.chosenMode.toRussian()
            val muscleLabel = exercise.exercise.muscle?.toRussian() ?: "Не указана"

            val displayedSets: Int = if (exerciseToDo.periodizationEnabled && periodizationDisplayEnabled) {
                when (exercise.chosenMode) {
                    exerciseToDo.modeA -> exerciseToDo.setsA ?: exercise.plannedSets
                    exerciseToDo.modeB -> exerciseToDo.setsB ?: exercise.plannedSets
                    else -> exercise.plannedSets
                }
            } else exercise.plannedSets

            val displayedReps: Int = if (exerciseToDo.periodizationEnabled && periodizationDisplayEnabled) {
                when (exercise.chosenMode) {
                    exerciseToDo.modeA -> exerciseToDo.repsA ?: exercise.plannedReps
                    exerciseToDo.modeB -> exerciseToDo.repsB ?: exercise.plannedReps
                    else -> exercise.plannedReps
                }
            } else exercise.plannedReps

            val displayedWeight: Double? = if (exerciseToDo.periodizationEnabled && periodizationDisplayEnabled) {
                when (exercise.chosenMode) {
                    exerciseToDo.modeA -> exerciseToDo.weightA ?: exercise.plannedWeight
                    exerciseToDo.modeB -> exerciseToDo.weightB ?: exercise.plannedWeight
                    else -> exercise.plannedWeight
                }
            } else exercise.plannedWeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = exercise.exercise.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Режим: $modeLabel",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (exerciseToDo.periodizationEnabled) {
                            Surface(
                                color = if (periodizationDisplayEnabled) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (periodizationDisplayEnabled) {
                                        "Периодизация включена"
                                    } else {
                                        "Периодизация скрыта"
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (periodizationDisplayEnabled) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailInfoCard(
                            label = "Регион тела",
                            value = exercise.exercise.bodyRegion.toRussian(),
                            modifier = Modifier.weight(1f)
                        )
                        DetailInfoCard(
                            label = "Группа мышц",
                            value = muscleLabel,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Spacer(modifier = Modifier.height(48.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Подходы", value = displayedSets.toString())

                        StatItem(
                            label = if (isTimeBased) "Секунды" else "Повторы",
                            value = if (isTimeBased) duration.toString() else displayedReps.toString()
                        )


                        if (displayedWeight != null) {
                            StatItem(label = "Вес", value = "${displayedWeight} кг")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Редактировать подходы")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { viewModel.previousExercise() },
                        enabled = currentIndex > 0,
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(32.dp))
                    }

                    if (currentIndex < currentSession.exercises.size - 1) {
                        Button(
                            onClick = { viewModel.nextExercise() },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Следующее", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.finishSession() },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Завершить", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DetailInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSetsRepsDialog(
    sets: Int,
    reps: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var localSets by remember { mutableStateOf(sets.toString()) }
    var localReps by remember { mutableStateOf(reps.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать подходы и повторения") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = localSets,
                    onValueChange = { localSets = it },
                    label = { Text("Количество подходов") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = localReps,
                    onValueChange = { localReps = it },
                    label = { Text("Количество повторений") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(localSets.toIntOrNull() ?: sets, localReps.toIntOrNull() ?: reps)
                    onDismiss()
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}


