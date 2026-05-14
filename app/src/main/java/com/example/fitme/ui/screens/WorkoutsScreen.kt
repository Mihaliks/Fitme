package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.enums.BodyRegion

enum class WorkoutsSubScreen {
    MAIN, READY_MADE, BY_MUSCLE, CONSTRUCTOR
}

@Composable
fun WorkoutsScreen() {
    var currentSubScreen by remember { mutableStateOf(WorkoutsSubScreen.MAIN) }

    BackHandler(enabled = currentSubScreen != WorkoutsSubScreen.MAIN) {
        currentSubScreen = WorkoutsSubScreen.MAIN
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
                WorkoutsSubScreen.CONSTRUCTOR -> WorkoutConstructorScreen { currentSubScreen = WorkoutsSubScreen.MAIN }
            }
        }
    }
}

@Composable
fun WorkoutsMainSelection(onNavigate: (WorkoutsSubScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Выбери уровень",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        WorkoutCategoryCard(
            title = "lvl 1: Готовые тренировки",
            subtitle = "Для новичков",
            icon = Icons.Default.RocketLaunch,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(WorkoutsSubScreen.READY_MADE) }
        )

        WorkoutCategoryCard(
            title = "lvl 2: По группам мышц",
            subtitle = "Помощник в создании тренировки",
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
                        Button(
                            onClick = { viewModel.startWorkout(selectedPlan!!.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Начать тренировку", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    items(templates) { template ->
                        val exercises = templateExercises[template.id] ?: emptyList()
                        TemplateCard(template.name, exercises)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanCard(plan: Plan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            AssistChip(
                onClick = { },
                label = { Text("Готовый план") },
                leadingIcon = { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) }
            )
        }
    }
}

@Composable
fun TemplateCard(name: String, exercises: List<com.example.fitme.data.entities.relations.ExerciseWithDetails>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detail.exercise.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (detail.exerciseToDo.duration != null && detail.exerciseToDo.duration!! > 0) {
                            "${detail.exerciseToDo.sets} x ${detail.exerciseToDo.duration} сек"
                        } else {
                            "${detail.exerciseToDo.sets} x ${detail.exerciseToDo.reps}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    val exercises by viewModel.exercisesByRegion.collectAsState()

    BackHandler(enabled = selectedRegion != null) {
        viewModel.selectRegion(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        selectedRegion?.toRussian() ?: "lvl 2: По группам мышц",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedRegion != null) {
                            viewModel.selectRegion(null)
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
        if (selectedRegion == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(BodyRegion.entries) { region ->
                    RegionCard(region) { viewModel.selectRegion(region) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(exercises) { exercise ->
                    ExerciseListItem(exercise.name)
                }
            }
        }
    }
}

@Composable
fun RegionCard(region: BodyRegion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = region.toRussian(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ExerciseListItem(name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.FitnessCenter, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun BodyRegion.toRussian(): String = when (this) {
    BodyRegion.CHEST -> "Грудь"
    BodyRegion.BACK -> "Спина"
    BodyRegion.SHOULDERS -> "Плечи"
    BodyRegion.ARMS -> "Руки"
    BodyRegion.CORE -> "Пресс / Корпус"
    BodyRegion.GLUTES -> "Ягодицы"
    BodyRegion.LEGS -> "Ноги"
    BodyRegion.CALVES -> "Икры"
    BodyRegion.CARDIO -> "Кардио"
    BodyRegion.FULL_BODY -> "Все тело"
    BodyRegion.OTHER -> "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutConstructorScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("lvl 3: Конструктор", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Build, 
                    null, 
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Экран конструктора находится в разработке",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
