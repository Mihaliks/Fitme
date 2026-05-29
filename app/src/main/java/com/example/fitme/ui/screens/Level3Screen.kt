package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.relations.ExerciseWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level3Screen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val editingPlan by viewModel.editingPlan.collectAsState()
    val editingTemplates by viewModel.editingTemplates.collectAsState()
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
            PlanEditor(editingPlan!!, editingTemplates, editingExercises, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
fun PlanEditor(plan: Plan, templates: List<WorkoutTemplate>, exercisesMap: Map<Int, List<ExerciseWithDetails>>, viewModel: WorkoutsViewModel, modifier: Modifier = Modifier) {
    val activePlanId by viewModel.activePlanId.collectAsState()
    val isFollowing = plan.id == activePlanId

    var isReorderMode by remember { mutableStateOf(false) }
    var localTemplates by remember(templates) { mutableStateOf(templates) }

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
         item { OutlinedTextField(value = plan.name, onValueChange = { viewModel.updatePlanName(it) }, label = { Text("Название плана") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
         item {
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 Button(onClick = { viewModel.startWorkout(plan.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.PlayArrow, null); Text("Начать") }
                 OutlinedButton(onClick = { viewModel.selectPlanAsActive(if (isFollowing) null else plan.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = if (isFollowing) ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonColors()) {
                     Icon(if (isFollowing) Icons.Default.Close else Icons.Default.Check, null); Text(if (isFollowing) "Отписаться" else "Выбрать")
                 }
             }
         }
         items(templates) { template -> TemplateEditorCard(template, exercisesMap[template.id] ?: emptyList(), viewModel) }
         item { Button(onClick = { viewModel.addWorkoutDay() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, null); Text("Добавить день") } }
         item { Button(onClick = { isReorderMode = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Icon(Icons.Default.SwapVert, null); Spacer(Modifier.width(8.dp)); Text("Режим перестановки") } }
         item { Button(onClick = { viewModel.closeConstructor() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Icon(Icons.Default.Check, null); Text("Завершить") } }
     }
 }

@Composable
fun TemplateEditorCard(template: WorkoutTemplate, exercises: List<ExerciseWithDetails>, viewModel: WorkoutsViewModel) {
    var showPicker by remember { mutableStateOf(false) }
    val allExercises by viewModel.allExercises.collectAsState()

    if (showPicker) ExercisePickerDialog(allExercises, { showPicker = false }, { viewModel.addExerciseToTemplate(template.id, it); showPicker = false })

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(value = template.name, onValueChange = { viewModel.updateTemplateName(template, it) }, textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.startWorkoutFromTemplate(template.id) }) { Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = { viewModel.hideTemplate(template) }) { Icon(Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.outline) }
            }
            exercises.forEach { detail -> ExerciseEditorItem(detail, { viewModel.updateExerciseDetails(it) }, { viewModel.removeExercise(detail.exerciseToDo) }) }
            TextButton(onClick = { showPicker = true }, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Default.Add, null); Text("Добавить упражнение") }
        }
    }
}

@Composable
fun ExerciseEditorItem(detail: ExerciseWithDetails, onUpdate: (ExerciseToDo) -> Unit, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(detail.exercise.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) { Icon(Icons.Default.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.error) }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CompactNumberInput("Подходы", detail.exerciseToDo.sets, { onUpdate(detail.exerciseToDo.copy(sets = it)) }, Modifier.weight(1f))
            CompactNumberInput("Повторы", detail.exerciseToDo.reps, { onUpdate(detail.exerciseToDo.copy(reps = it)) }, Modifier.weight(1f))
        }
    }
}

@Composable
fun CompactNumberInput(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value = value.toString(), onValueChange = { it.toIntOrNull()?.let { v -> if (v >= 0) onValueChange(v) } }, label = { Text(label, fontSize = 10.sp) }, modifier = modifier, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true)
}








