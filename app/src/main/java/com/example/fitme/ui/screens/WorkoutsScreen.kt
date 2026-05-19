package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import com.example.fitme.data.entities.relations.ExerciseWithDetails


enum class WorkoutsSubScreen {
    MAIN, READY_MADE, BY_MUSCLE, CONSTRUCTOR, HIDDEN_TEMPLATES, HIDDEN_PLANS
}

@Composable
fun WorkoutsScreen() {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val currentSession by viewModel.currentSession.collectAsState()
    var currentSubScreen by remember { mutableStateOf(WorkoutsSubScreen.MAIN) }

    if (currentSession != null) {
        WorkoutSessionScreen(viewModel = viewModel)
        return
    }

    BackHandler(enabled = currentSubScreen != WorkoutsSubScreen.MAIN) {
        currentSubScreen = when (currentSubScreen) {
            WorkoutsSubScreen.HIDDEN_TEMPLATES -> WorkoutsSubScreen.CONSTRUCTOR
            WorkoutsSubScreen.HIDDEN_PLANS -> WorkoutsSubScreen.CONSTRUCTOR
            else -> WorkoutsSubScreen.MAIN
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentSubScreen,
            label = "WorkoutsNav",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { screen ->
            when (screen) {
                WorkoutsSubScreen.MAIN -> WorkoutsMainSelection(onNavigate = { currentSubScreen = it })
                WorkoutsSubScreen.READY_MADE -> Level1Screen { currentSubScreen = WorkoutsSubScreen.MAIN }
                WorkoutsSubScreen.BY_MUSCLE -> Level2Screen { currentSubScreen = WorkoutsSubScreen.MAIN }
                WorkoutsSubScreen.CONSTRUCTOR -> Level3Screen { currentSubScreen = WorkoutsSubScreen.MAIN }
                WorkoutsSubScreen.HIDDEN_TEMPLATES -> HiddenTemplatesScreen {
                    currentSubScreen = WorkoutsSubScreen.CONSTRUCTOR
                }
                WorkoutsSubScreen.HIDDEN_PLANS -> HiddenPlansScreen {
                    currentSubScreen = WorkoutsSubScreen.CONSTRUCTOR
                }
            }
        }
    }
}

@Composable
fun WorkoutsMainSelection(onNavigate: (WorkoutsSubScreen) -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val activePlanId by viewModel.activePlanId.collectAsState()
    val plans by viewModel.activePlans.collectAsState()
    val activePlan = plans.find { it.id == activePlanId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Тренировки",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (activePlan != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(WorkoutsSubScreen.READY_MADE) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ваш план:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        Text(activePlan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = { viewModel.startWorkout(activePlan.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Text("Начать")
                    }
                }
            }
        }

        Text(
            text = "Выбери уровень",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        WorkoutCategoryCard(
            title = "lvl 1: Готовые планы тренировок",
            subtitle = "Для новичков",
            icon = Icons.Default.RocketLaunch,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(WorkoutsSubScreen.READY_MADE) }
        )

        WorkoutCategoryCard(
            title = "lvl 2: Тренировки по группам мышц",
            subtitle = "Если нужно быстро включить тренировку",
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
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(36.dp), tint = Color.Black.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 24.sp
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                modifier = Modifier.size(32.dp),
                tint = Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun PlanCard(plan: Plan, isActive: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isActive) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                else Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }
            Spacer(modifier = Modifier.height(12.dp))
            AssistChip(onClick = { }, label = { Text(if (isActive) "Активный план" else "Программа") }, leadingIcon = { Icon(if (isActive) Icons.Default.Star else Icons.AutoMirrored.Filled.Assignment, null, Modifier.size(16.dp)) })
        }
    }
}

@Composable
fun TemplateCard(template: WorkoutTemplate, exercises: List<ExerciseWithDetails>, viewModel: WorkoutsViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.startWorkoutFromTemplate(template.id) }) { Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.height(12.dp))
            exercises.forEach { detail ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(detail.exercise.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    val duration = detail.exerciseToDo.duration
                    Text(text = if (duration != null && duration > 0) "${detail.exerciseToDo.sets} x $duration сек" else "${detail.exerciseToDo.sets} x ${detail.exerciseToDo.reps}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun BodyRegion.toRussian(): String = when (this) {
    BodyRegion.CHEST -> "Грудь"; BodyRegion.BACK -> "Спина"; BodyRegion.SHOULDERS -> "Плечи"; BodyRegion.ARMS -> "Руки"
    BodyRegion.CORE -> "Пресс / Корпус"; BodyRegion.GLUTES -> "Ягодицы"; BodyRegion.LEGS -> "Ноги"; BodyRegion.CALVES -> "Икры"
    BodyRegion.CARDIO -> "Кардио"; BodyRegion.FULL_BODY -> "Все тело"; BodyRegion.OTHER -> "Другое"
}

fun MuscleGroup.toRussian(): String = when (this) {
    MuscleGroup.UPPER_CHEST -> "Верх груди"; MuscleGroup.MIDDLE_CHEST -> "Середина груди"; MuscleGroup.LOWER_CHEST -> "Низ груди"
    MuscleGroup.LATS -> "Широчайшие"; MuscleGroup.MID_BACK -> "Средняя часть спины"; MuscleGroup.TRAPS -> "Трапеции"; MuscleGroup.LOWER_BACK -> "Поясница"
    MuscleGroup.FRONT_DELTS -> "Передняя дельта"; MuscleGroup.SIDE_DELTS -> "Средняя дельта"; MuscleGroup.REAR_DELTS -> "Задняя дельта"
    MuscleGroup.BICEPS -> "Бицепс"; MuscleGroup.BRACHIALIS -> "Брахиалис"; MuscleGroup.TRICEPS -> "Трицепс"; MuscleGroup.FOREARMS -> "Предплечья"
    MuscleGroup.ABS -> "Пресс"; MuscleGroup.OBLIQUES -> "Косые мышцы"
    MuscleGroup.GLUTE_MAXIMUS -> "Большая ягодичная"; MuscleGroup.GLUTE_MEDIUS -> "Средняя ягодичная"; MuscleGroup.GLUTE_MINIMUS -> "Малая ягодичная"
    MuscleGroup.QUADS -> "Квадрицепс"; MuscleGroup.HAMSTRINGS -> "Бицепс бедра"; MuscleGroup.ADDUCTORS -> "Приводящие"
    MuscleGroup.CALVES -> "Икры"; MuscleGroup.CARDIO -> "Кардио"; MuscleGroup.FULL_BODY -> "Все тело"; MuscleGroup.OTHER -> "Другое"
}

@Composable
fun WorkoutConstructorScreen(onBack: () -> Unit, onNavigateToHidden: () -> Unit, onNavigateToHiddenPlans: () -> Unit) {
    Level3Screen(onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenPlansScreen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val hiddenPlans by viewModel.hiddenPlans.collectAsState()
    val activePlanId by viewModel.activePlanId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Скрытые планы", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (hiddenPlans.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Скрытых планов нет", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(hiddenPlans) { plan -> PlanListItem(plan, plan.id == activePlanId, false, { viewModel.loadPlanForEditing(plan.id) }, { viewModel.togglePlanVisibility(plan) }, isArchiveScreen = true) }
            }
        }
    }
}

@Composable
fun PlanListItem(plan: Plan, isActive: Boolean, isBuiltIn: Boolean, onEdit: () -> Unit, onHide: () -> Unit, onStart: () -> Unit = {}, isArchiveScreen: Boolean = false, onSelect: (() -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onEdit() }, shape = RoundedCornerShape(20.dp), colors = if (isActive) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.name, style = MaterialTheme.typography.titleMedium)
                    if (isBuiltIn) Text("Стандартный", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                if (!isArchiveScreen && !isBuiltIn) IconButton(onClick = onStart) { Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onHide) { Icon(if (plan.isActive) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) }
            }
            if (onSelect != null && !isBuiltIn && !isArchiveScreen) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (isActive) ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(if (isActive) Icons.Default.Close else Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isActive) "Перестать следовать" else "Выбрать план")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenTemplatesScreen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val hiddenTemplates by viewModel.hiddenEditingTemplates.collectAsState()
    val exercisesMap by viewModel.editingExercises.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Скрытые дни", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (hiddenTemplates.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Скрытых дней нет", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(hiddenTemplates) { template ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${exercisesMap[template.id]?.size ?: 0} упр.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.restoreTemplate(template) }) { Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerDialog(exercises: List<Exercise>, onDismiss: () -> Unit, onSelect: (Exercise) -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf<BodyRegion?>(null) }

    val filteredExercises = exercises.filter { ex ->
        (selectedRegion == null || ex.bodyRegion == selectedRegion) &&
        ex.name.contains(query, true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите упражнение") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Поиск...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRegion == null,
                            onClick = { selectedRegion = null },
                            label = { Text("Все") }
                        )
                    }
                    items(BodyRegion.entries) { region ->
                        FilterChip(
                            selected = selectedRegion == region,
                            onClick = { selectedRegion = region },
                            label = { Text(region.toRussian()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredExercises) { ex ->
                        ListItem(
                            headlineContent = { Text(ex.name) },
                            supportingContent = {
                                Text(
                                    text = buildString {
                                        append(ex.bodyRegion.toRussian())
                                        ex.muscle?.let {
                                            append(" • ")
                                            append(it.toRussian())
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.clickable { onSelect(ex) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

