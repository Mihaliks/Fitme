package com.example.fitme.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.enums.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(onBack: () -> Unit = {}) {
    val viewModel: WorkoutsViewModel = viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val exercises by viewModel.filteredExercises.collectAsState()
    val records by viewModel.exerciseRecords.collectAsState()
    val searchQuery by viewModel.exerciseSearchQuery.collectAsState()
    val selectedMuscleGroup by viewModel.selectedExerciseMuscleGroup.collectAsState()

    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var editingSlot by remember { mutableStateOf<RecordSlot?>(null) }
    var repsText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }

    LaunchedEffect(editingExercise?.id, editingSlot, records) {
        val exercise = editingExercise
        val slot = editingSlot
        val value = when {
            exercise == null || slot == null -> null
            slot == RecordSlot.LOW_REP -> records[exercise.id]?.lowRep
            slot == RecordSlot.HIGH_REP -> records[exercise.id]?.highRep
            else -> null
        }
        repsText = value?.reps?.toString().orEmpty()
        weightText = value?.weight?.toString().orEmpty()
        durationText = value?.duration?.toString().orEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рекорды упражнений") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Поиск и фильтры",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setExerciseSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Поиск упражнения") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf<MuscleGroup?>(null) + MuscleGroup.entries.toList()) { group ->
                    val selected = selectedMuscleGroup == group
                    FilterChip(
                        selected = selected,
                        onClick = {
                            viewModel.setSelectedExerciseMuscleGroup(
                                if (selected) null else group
                            )
                        },
                        label = {
                            Text(group?.toRussian() ?: "Все мышцы", maxLines = 1, softWrap = false)
                        }
                    )
                }
            }

            if (searchQuery.isNotBlank() || selectedMuscleGroup != null) {
                TextButton(onClick = viewModel::clearExerciseFilters) {
                    Text("Сбросить фильтры")
                }
            }

            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ничего не найдено",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(exercises) { exercise ->
                        val state = records[exercise.id]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                RecordSlotCard(
                                    label = RecordSlot.LOW_REP.displayName(),
                                    value = state?.lowRep,
                                    onEdit = {
                                        editingExercise = exercise
                                        editingSlot = RecordSlot.LOW_REP
                                    },
                                    onClear = if (state?.lowRep != null) { { viewModel.clearExerciseRecord(exercise.id, RecordSlot.LOW_REP) } } else null
                                )

                                RecordSlotCard(
                                    label = RecordSlot.HIGH_REP.displayName(),
                                    value = state?.highRep,
                                    onEdit = {
                                        editingExercise = exercise
                                        editingSlot = RecordSlot.HIGH_REP
                                    },
                                    onClear = if (state?.highRep != null) { { viewModel.clearExerciseRecord(exercise.id, RecordSlot.HIGH_REP) } } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingExercise != null && editingSlot != null) {
        val currentExercise = editingExercise!!
        val currentSlot = editingSlot!!
        val currentRecord = records[currentExercise.id]
        val hasCurrentRecord = when (currentSlot) {
            RecordSlot.LOW_REP -> currentRecord?.lowRep != null
            RecordSlot.HIGH_REP -> currentRecord?.highRep != null
        }
        val canSave = repsText.toIntOrNull() != null || weightText.toDoubleOrNull() != null || durationText.toIntOrNull() != null

        AlertDialog(
            onDismissRequest = {
                editingExercise = null
                editingSlot = null
            },
            title = { Text("${currentSlot.displayName()} рекорд") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = currentExercise.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it.replace(',', '.') },
                        label = { Text("Вес, кг") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Повторы") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Секунды") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Можно заполнить только те поля, которые нужны для конкретного рекорда.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveExerciseRecord(
                            exerciseId = currentExercise.id,
                            slot = currentSlot,
                            value = ExerciseRecordValue(
                                reps = repsText.toIntOrNull(),
                                weight = weightText.toDoubleOrNull(),
                                duration = durationText.toIntOrNull()
                            )
                        )
                        editingExercise = null
                        editingSlot = null
                    },
                    enabled = canSave
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasCurrentRecord) {
                        TextButton(
                            onClick = {
                                viewModel.clearExerciseRecord(currentExercise.id, currentSlot)
                                editingExercise = null
                                editingSlot = null
                            }
                        ) {
                            Text("Очистить")
                        }
                    }
                    TextButton(onClick = {
                        editingExercise = null
                        editingSlot = null
                    }) {
                        Text("Отмена")
                    }
                }
            }
        )
    }
}

@Composable
private fun RecordSlotCard(
    label: String,
    value: ExerciseRecordValue?,
    onEdit: () -> Unit,
    onClear: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value?.displayText() ?: "Рекорд не установлен",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                OutlinedButton(onClick = onEdit) {
                    Text(if (value == null) "Добавить" else "Изменить")
                }
                if (onClear != null) {
                    TextButton(onClick = onClear) {
                        Text("Очистить")
                    }
                }
            }
        }
    }
}




