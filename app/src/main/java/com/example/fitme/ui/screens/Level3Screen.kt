package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.entities.relations.ExerciseWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level3Screen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val editingPlan by viewModel.editingPlan.collectAsState()
    val editingTemplates by viewModel.editingTemplates.collectAsState()
    val hiddenEditingTemplates by viewModel.hiddenEditingTemplates.collectAsState()
    val editingExercises by viewModel.editingExercises.collectAsState()
    val activePlans by viewModel.activePlans.collectAsState()
    val activePlanId by viewModel.activePlanId.collectAsState()
    val isCreatingNewPlan by viewModel.isCreatingNewPlan.collectAsState()
    val showEmptyPlanWarning by viewModel.showEmptyPlanWarning.collectAsState()

    if (showEmptyPlanWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissEmptyPlanWarning() },
            title = { Text("План пуст") },
            text = { Text("В плане нет ни одного тренировочного дня. Вы уверены, что хотите выйти? Пустой план не будет сохранен.") },
            confirmButton = {
                TextButton(onClick = { viewModel.cancelPlanCreation() }) { Text("Выйти без сохранения", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissEmptyPlanWarning() }) { Text("Продолжить") }
            }
        )
    }

    BackHandler(enabled = editingPlan != null) {
        if (isCreatingNewPlan) viewModel.cancelPlanCreation() else viewModel.closeConstructor()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingPlan == null) "lvl 3: Конструктор" else if (isCreatingNewPlan) "Создание плана" else "Редактирование", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editingPlan != null) {
                            if (isCreatingNewPlan) viewModel.cancelPlanCreation() else viewModel.closeConstructor()
                        } else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (editingPlan == null) {
                        IconButton(onClick = { /* TODO: navigate to hidden plans */ }) { Icon(Icons.Default.VisibilityOff, null) }
                    } else if (isCreatingNewPlan) {
                        TextButton(onClick = { viewModel.cancelPlanCreation() }) {
                            Text("Отмена", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = { if (editingPlan == null) ExtendedFloatingActionButton(onClick = { viewModel.createNewPlan() }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Создать план") }) }
    ) { padding ->
        if (editingPlan == null) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("Ваши планы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                if (activePlans.isEmpty()) item { Text("Нет активных планов", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(activePlans) { plan ->
                    val isActivePlan = plan.id == activePlanId
                    PlanListItem(
                        plan = plan,
                        isActive = isActivePlan,
                        isBuiltIn = false,
                        onEdit = { viewModel.loadPlanForEditing(plan.id) },
                        onHide = { viewModel.togglePlanVisibility(plan) },
                        onStart = { viewModel.startWorkout(plan.id) },
                        onSelect = { viewModel.selectPlanAsActive(if (isActivePlan) null else plan.id) }
                    )
                }
            }
        } else {
            PlanEditor(editingPlan!!, editingTemplates, hiddenEditingTemplates, editingExercises, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
fun PlanEditor(plan: Plan, templates: List<WorkoutTemplate>, hiddenTemplates: List<WorkoutTemplate>, exercisesMap: Map<Int, List<ExerciseWithDetails>>, viewModel: WorkoutsViewModel, modifier: Modifier = Modifier) {
    val activePlanId by viewModel.activePlanId.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val isFollowing = plan.id == activePlanId
    val allPlanTemplates = templates + hiddenTemplates
    val allPlanExercises = allPlanTemplates.flatMap { exercisesMap[it.id].orEmpty() }
    val isPlanPeriodizationEnabled = allPlanExercises.isNotEmpty() && allPlanExercises.all { it.exerciseToDo.periodizationEnabled }

    var draftPlanName by remember(plan.id, plan.name) { mutableStateOf(plan.name) }
    var showValidationError by remember { mutableStateOf(false) }
    var isReorderMode by remember { mutableStateOf(false) }
    var localTemplates by remember(templates) { mutableStateOf(templates) }

    if (showValidationError && validationErrors.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            title = { Text("Ошибка при сохранении плана") },
            text = {
                LazyColumn {
                    items(validationErrors.size) { index ->
                        Text(validationErrors[index], style = MaterialTheme.typography.bodySmall)
                        if (index < validationErrors.size - 1) Spacer(Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showValidationError = false }) { Text("Ок") }
            }
        )
    }

    if (isReorderMode) {
        Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Изменение порядка", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(localTemplates) { index, template ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DragHandle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(16.dp))
                            Text(template.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)

                            IconButton(
                                onClick = {
                                    val newList = localTemplates.toMutableList()
                                    newList[index] = newList[index - 1].also { newList[index - 1] = newList[index] }
                                    localTemplates = newList
                                },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, null)
                            }

                            IconButton(
                                onClick = {
                                    val newList = localTemplates.toMutableList()
                                    newList[index] = newList[index + 1].also { newList[index + 1] = newList[index] }
                                    localTemplates = newList
                                },
                                enabled = index < localTemplates.size - 1
                            ) {
                                Icon(Icons.Default.ArrowDownward, null)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        localTemplates = templates
                        isReorderMode = false
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        viewModel.reorderTemplates(localTemplates.map { it.id })
                        isReorderMode = false
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Сохранить")
                }
            }
        }
        return
    }

     LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
         item {
             OutlinedTextField(
                 value = draftPlanName,
                 onValueChange = { draftPlanName = it },
                 label = { Text("Название плана") },
                 modifier = Modifier.fillMaxWidth(),
                 shape = RoundedCornerShape(16.dp),
                 singleLine = true
             )
         }
         item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                       Button(onClick = { viewModel.startWorkout(plan.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.PlayArrow, null); Text("Начать") }
                        OutlinedButton(onClick = {
                            if (plan.id <= 0) {
                                viewModel.savePlanName(draftPlanName)
                                if (viewModel.savePlanChanges(selectAsActive = true)) {
                                    viewModel.closeConstructor()
                                } else {
                                    showValidationError = true
                                }
                            } else {
                                    if (isFollowing) viewModel.selectPlanAsActive(null)
                                    else viewModel.activatePlan(plan)
                            }
                        }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = if (isFollowing) ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonColors()) {
                            Icon(if (isFollowing) Icons.Default.Close else Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isFollowing) "Отписаться" else if (plan.id <= 0) "Сохранить и выбрать" else "Выбрать")
                        }
                  }
         }
          item {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                  Checkbox(
                      checked = isPlanPeriodizationEnabled,
                      onCheckedChange = { enabled ->
                          setPeriodizationForExercises(allPlanExercises, enabled, viewModel)
                      },
                      enabled = allPlanExercises.isNotEmpty()
                  )
                  Spacer(Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                      Text("Периодизация всего плана", fontWeight = FontWeight.Medium)
                      Text("Включит или выключит периодизацию на всех днях и упражнениях", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
              }
          }
         items(templates) { template -> TemplateEditorCard(template, exercisesMap[template.id] ?: emptyList(), viewModel) }
         item { Button(onClick = { viewModel.addWorkoutDay() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, null); Text("Добавить день") } }
         item { Button(onClick = { isReorderMode = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Icon(Icons.Default.SwapVert, null); Spacer(Modifier.width(8.dp)); Text("Режим перестановки") } }
         item {
             Button(
                 onClick = {
                     viewModel.savePlanName(draftPlanName)
                      if (viewModel.savePlanChanges(selectAsActive = false)) {
                         viewModel.closeConstructor()
                     } else {
                         showValidationError = true
                     }
                 },
                 modifier = Modifier.fillMaxWidth().height(56.dp),
                 shape = RoundedCornerShape(16.dp),
                 colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
             ) {
                 Icon(Icons.Default.Check, null)
                 Spacer(Modifier.width(8.dp))
                 Text("Сохранить план")
             }
         }
     }
 }

@Composable
fun TemplateEditorCard(template: WorkoutTemplate, exercises: List<ExerciseWithDetails>, viewModel: WorkoutsViewModel) {
    var showPicker by remember { mutableStateOf(false) }
    val allExercises by viewModel.allExercises.collectAsState()
    val templatePeriodizationEnabled = exercises.isNotEmpty() && exercises.all { it.exerciseToDo.periodizationEnabled }

    if (showPicker) ExercisePickerDialog(allExercises, { showPicker = false }, { viewModel.addExerciseToTemplate(template.id, it); showPicker = false })

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(value = template.name, onValueChange = { viewModel.updateTemplateName(template, it) }, textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.startWorkoutFromTemplate(template.id) }) { Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = { viewModel.hideTemplate(template) }) { Icon(Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.outline) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = templatePeriodizationEnabled,
                    onCheckedChange = { enabled ->
                        setPeriodizationForExercises(exercises, enabled, viewModel)
                    },
                    enabled = exercises.isNotEmpty()
                )
                Spacer(Modifier.width(8.dp))
                Text("Периодизация дня", fontWeight = FontWeight.Medium)
            }
            exercises.forEach { detail -> ExerciseEditorItem(detail, { viewModel.updateExerciseDetails(it) }, { viewModel.removeExercise(detail.exerciseToDo) }) }
            TextButton(onClick = { showPicker = true }, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Default.Add, null); Text("Добавить упражнение") }
        }
    }
}

@Composable
fun ExerciseEditorItem(detail: ExerciseWithDetails, onUpdate: (ExerciseToDo) -> Unit, onDelete: () -> Unit) {
    val exerciseToDo = detail.exerciseToDo
    val canEditWeight = exerciseSupportsWeight(detail)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(detail.exercise.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) { Icon(Icons.Default.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.error) }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = exerciseToDo.periodizationEnabled,
                onCheckedChange = { enabled ->
                    onUpdate(exerciseToDo.withPeriodization(enabled, canEditWeight))
                }
            )
            Spacer(Modifier.width(8.dp))
            Text("Периодизация", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!exerciseToDo.periodizationEnabled) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CompactNumberInput("Подходы", exerciseToDo.sets, { onUpdate(exerciseToDo.copy(sets = it)) }, Modifier.weight(1f))
                CompactNumberInput("Повторы", exerciseToDo.reps, { onUpdate(exerciseToDo.copy(reps = it)) }, Modifier.weight(1f))
            }

            if (canEditWeight) {
                Spacer(modifier = Modifier.height(8.dp))
                CompactDoubleInput(
                    label = "Вес",
                    value = exerciseToDo.weight ?: 0.0,
                    onValueChange = { onUpdate(exerciseToDo.copy(weight = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodizationBlockTitle(
                    title = "A",
                    subtitle = "Гипертрофия",
                    modifier = Modifier.weight(1f)
                )
                PeriodizationBlockTitle(
                    title = "B",
                    subtitle = "Сила",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CompactNumberInput("Подходы A", exerciseToDo.setsA ?: exerciseToDo.sets, { onUpdate(exerciseToDo.copy(setsA = it)) }, Modifier.weight(1f))
                CompactNumberInput("Подходы B", exerciseToDo.setsB ?: exerciseToDo.sets, { onUpdate(exerciseToDo.copy(setsB = it)) }, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CompactNumberInput("Повторы A", exerciseToDo.repsA ?: exerciseToDo.reps, { onUpdate(exerciseToDo.copy(repsA = it)) }, Modifier.weight(1f))
                CompactNumberInput("Повторы B", exerciseToDo.repsB ?: exerciseToDo.reps, { onUpdate(exerciseToDo.copy(repsB = it)) }, Modifier.weight(1f))
            }

            if (canEditWeight) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CompactDoubleInput(
                        label = "Вес A",
                        value = exerciseToDo.weightA ?: (exerciseToDo.weight ?: 0.0),
                        onValueChange = { onUpdate(exerciseToDo.copy(weightA = it)) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactDoubleInput(
                        label = "Вес B",
                        value = exerciseToDo.weightB ?: (exerciseToDo.weight ?: 0.0),
                        onValueChange = { onUpdate(exerciseToDo.copy(weightB = it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactNumberInput(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value = value.toString(), onValueChange = { it.toIntOrNull()?.let { v -> if (v >= 0) onValueChange(v) } }, label = { Text(label, fontSize = 10.sp) }, modifier = modifier, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true)
}

@Composable
fun CompactDoubleInput(label: String, value: Double, onValueChange: (Double) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString(),
        onValueChange = {
            it.replace(',', '.').toDoubleOrNull()?.let { v ->
                if (v >= 0.0) onValueChange(v)
            }
        },
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun PeriodizationBlockTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun exerciseSupportsWeight(detail: ExerciseWithDetails): Boolean {
    if (detail.exerciseToDo.duration != null && detail.exerciseToDo.duration > 0) return false
    return when (detail.exercise.bodyRegion) {
        BodyRegion.CORE,
        BodyRegion.CARDIO -> false
        else -> true
    }
}

fun setPeriodizationForExercises(
    exercises: List<ExerciseWithDetails>,
    enabled: Boolean,
    viewModel: WorkoutsViewModel
) {
    exercises.forEach { detail ->
        viewModel.updateExerciseDetails(detail.exerciseToDo.withPeriodization(enabled, exerciseSupportsWeight(detail)))
    }
}

fun ExerciseToDo.withPeriodization(enabled: Boolean, canEditWeight: Boolean): ExerciseToDo {
    return if (!enabled) {
        copy(periodizationEnabled = false)
    } else {
        copy(
            periodizationEnabled = true,
            modeA = modeA ?: TrainingMode.HYPERTROPHY,
            modeB = modeB ?: TrainingMode.STRENGTH,
            setsA = setsA ?: sets,
            setsB = setsB ?: sets,
            repsA = repsA ?: reps,
            repsB = repsB ?: reps,
            weightA = if (canEditWeight) weightA ?: weight else weightA,
            weightB = if (canEditWeight) weightB ?: weight else weightB,
        )
    }
}








