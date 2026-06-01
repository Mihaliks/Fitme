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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitme.data.entities.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutsViewModel
) {
    val session by viewModel.currentSession.collectAsState()
    val currentIndex by viewModel.currentExerciseIndex.collectAsState()
    val periodizationDisplayEnabled by viewModel.periodizationDisplayEnabled.collectAsState()
    val currentNotes by viewModel.observeNotesForCurrentExercise().collectAsState(initial = emptyList())

    val currentSession = session ?: return

    var actualRepsText by remember(currentIndex, currentSession) { mutableStateOf("") }
    var actualWeightText by remember(currentIndex, currentSession) { mutableStateOf("") }
    var minutesText by remember(currentIndex, currentSession) { mutableStateOf("") }
    var secondsText by remember(currentIndex, currentSession) { mutableStateOf("") }
    var recordCandidateNote by remember(currentIndex, currentSession) { mutableStateOf<Note?>(null) }

    BackHandler {
        viewModel.finishSession()
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

            val shownWeight = if (isTimeBased) null else displayedWeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            label = if (isTimeBased) "Время" else "Повторы",
                            value = if (isTimeBased) formatDurationForUi(duration) else displayedReps.toString()
                        )

                        if (shownWeight != null) {
                            StatItem(label = "Вес", value = "${shownWeight} кг")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Фактические подходы",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (currentNotes.isEmpty()) {
                            Text(
                                text = "Пока нет записанных подходов",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            currentNotes.forEachIndexed { index, note ->
                                val summary = "Подход ${index + 1}: ${note.displaySummary()}"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = summary,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    TextButton(
                                        onClick = { recordCandidateNote = note },
                                        enabled = note.reps != null || note.weight != null || note.duration != null
                                    ) {
                                        Text("В рекорд")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isTimeBased) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = minutesText,
                                onValueChange = { minutesText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Минуты") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = secondsText,
                                onValueChange = { secondsText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Секунды") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = actualRepsText,
                                onValueChange = { actualRepsText = it },
                                label = { Text("Повторы") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = actualWeightText,
                                onValueChange = { actualWeightText = it.replace(',', '.') },
                                label = { Text("Вес") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val minutesCandidate = minutesText.toIntOrNull()
                    val secondsCandidate = secondsText.toIntOrNull()
                    val durationCandidate = if (isTimeBased) {
                        if (minutesText.isBlank() && secondsText.isBlank()) null
                        else {
                            val m = minutesCandidate ?: 0
                            val s = secondsCandidate ?: 0
                            val total = m * 60 + s
                            total.takeIf { it > 0 }
                        }
                    } else null
                    val repsCandidate = if (isTimeBased) null else actualRepsText.toIntOrNull()
                    val weightCandidate = if (isTimeBased) null else actualWeightText.toDoubleOrNull()
                    val addEnabled = if (isTimeBased) durationCandidate != null else repsCandidate != null || weightCandidate != null

                    Button(
                        onClick = {
                            val repsValue = repsCandidate
                            val weightValue = weightCandidate
                            val durationValue = durationCandidate
                            if (repsValue != null || weightValue != null || durationValue != null) {
                                viewModel.appendNoteForCurrentExercise(
                                    reps = repsValue,
                                    weight = weightValue,
                                    duration = durationValue
                                )
                                if (isTimeBased) {
                                    minutesText = ""
                                    secondsText = ""
                                } else {
                                    actualRepsText = ""
                                    actualWeightText = ""
                                }
                            }
                        },
                        enabled = addEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить подход")
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

    recordCandidateNote?.let { note ->
        AlertDialog(
            onDismissRequest = { recordCandidateNote = null },
            title = { Text("Добавить в рекорд") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = currentSession.exercises.getOrNull(currentIndex)?.exercise?.name.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = note.displaySummary(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Выбери, в какой из двух рекордов сохранить этот подход.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                currentSession.exercises.getOrNull(currentIndex)?.let { exercise ->
                                    viewModel.saveExerciseRecord(
                                        exerciseId = exercise.exercise.id,
                                        slot = RecordSlot.LOW_REP,
                                        value = ExerciseRecordValue(
                                            reps = note.reps,
                                            weight = note.weight,
                                            duration = note.duration
                                        )
                                    )
                                }
                                recordCandidateNote = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Малоповторный")
                        }
                        OutlinedButton(
                            onClick = {
                                currentSession.exercises.getOrNull(currentIndex)?.let { exercise ->
                                    viewModel.saveExerciseRecord(
                                        exerciseId = exercise.exercise.id,
                                        slot = RecordSlot.HIGH_REP,
                                        value = ExerciseRecordValue(
                                            reps = note.reps,
                                            weight = note.weight,
                                            duration = note.duration
                                        )
                                    )
                                }
                                recordCandidateNote = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Многоповторный")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { recordCandidateNote = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

private fun Note.displaySummary(): String = when {
    duration != null && weight != null -> "${weight} кг / ${formatDurationForUi(duration)}"
    duration != null -> formatDurationForUi(duration)
    reps != null && weight != null -> "${weight} кг × ${reps}"
    reps != null -> "${reps} повторений"
    weight != null -> "${weight} кг"
    else -> "—"
}

private @Composable
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

private @Composable
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

