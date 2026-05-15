package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.relations.ExerciseWithDetails
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*


enum class WorkoutsSubScreen {
    MAIN, READY_MADE, BY_MUSCLE, CONSTRUCTOR, HIDDEN_TEMPLATES, HIDDEN_PLANS
}

@Composable
fun WorkoutsScreen() {
    val viewModel: WorkoutsViewModel = viewModel()
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
                WorkoutsSubScreen.READY_MADE -> ReadyMadeWorkoutsScreen { currentSubScreen = WorkoutsSubScreen.MAIN }
                WorkoutsSubScreen.BY_MUSCLE -> WorkoutsByMuscleScreen { currentSubScreen = WorkoutsSubScreen.MAIN }
                WorkoutsSubScreen.CONSTRUCTOR -> WorkoutConstructorScreen(
                    onBack = { currentSubScreen = WorkoutsSubScreen.MAIN },
                    onNavigateToHidden = { currentSubScreen = WorkoutsSubScreen.HIDDEN_TEMPLATES },
                    onNavigateToHiddenPlans = { currentSubScreen = WorkoutsSubScreen.HIDDEN_PLANS }
                )
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
    val viewModel: WorkoutsViewModel = viewModel()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyMadeWorkoutsScreen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = viewModel()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsByMuscleScreen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = viewModel()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val templates by viewModel.templatesByRegion.collectAsState()

    BackHandler(enabled = selectedRegion != null) { viewModel.selectRegion(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedRegion?.toRussian() ?: "lvl 2: По группам мышц", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { if (selectedRegion != null) viewModel.selectRegion(null) else onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (selectedRegion == null) {
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(BodyRegion.entries) { region -> RegionCard(region) { viewModel.selectRegion(region) } }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (templates.isEmpty()) {
                    item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("Тренировок не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                } else {
                    items(templates) { (template, exercises) -> TemplateCard(template, exercises, viewModel) }
                }
            }
        }
    }
}

@Composable
fun RegionCard(region: BodyRegion, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(110.dp).clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(region.toRussian(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
    }
}

fun BodyRegion.toRussian(): String = when (this) {
    BodyRegion.CHEST -> "Грудь"; BodyRegion.BACK -> "Спина"; BodyRegion.SHOULDERS -> "Плечи"; BodyRegion.ARMS -> "Руки"
    BodyRegion.CORE -> "Пресс / Корпус"; BodyRegion.GLUTES -> "Ягодицы"; BodyRegion.LEGS -> "Ноги"; BodyRegion.CALVES -> "Икры"
    BodyRegion.CARDIO -> "Кардио"; BodyRegion.FULL_BODY -> "Все тело"; BodyRegion.OTHER -> "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutConstructorScreen(onBack: () -> Unit, onNavigateToHidden: () -> Unit, onNavigateToHiddenPlans: () -> Unit) {
    val viewModel: WorkoutsViewModel = viewModel()
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
                        IconButton(onClick = onNavigateToHiddenPlans) { Icon(Icons.Default.VisibilityOff, null) }
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
            PlanEditor(editingPlan!!, editingTemplates, editingExercises, viewModel, onNavigateToHidden, Modifier.padding(padding))
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
fun HiddenPlansScreen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = viewModel()
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
fun PlanEditor(plan: Plan, templates: List<WorkoutTemplate>, exercisesMap: Map<Int, List<ExerciseWithDetails>>, viewModel: WorkoutsViewModel, onNavigateToHidden: () -> Unit, modifier: Modifier = Modifier) {
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
        item { TextButton(onClick = onNavigateToHidden, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.VisibilityOff, null); Text("Скрытые дни") } }
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

@Composable
fun ExercisePickerDialog(exercises: List<Exercise>, onDismiss: () -> Unit, onSelect: (Exercise) -> Unit) {
    var query by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Выберите упражнение") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Поиск...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                LazyColumn { items(exercises.filter { it.name.contains(query, true) }) { ex -> ListItem(headlineContent = { Text(ex.name) }, supportingContent = { Text(ex.bodyRegion.toRussian()) }, modifier = Modifier.clickable { onSelect(ex) }) } }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenTemplatesScreen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = viewModel()
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
