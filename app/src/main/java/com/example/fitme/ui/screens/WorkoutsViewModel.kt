package com.example.fitme.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.entities.relations.ExerciseWithDetails
import com.example.fitme.data.repositories.ExerciseRepository
import com.example.fitme.data.repositories.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val workoutRepository = WorkoutRepository(db)
    private val exerciseRepository = ExerciseRepository(db.exerciseDao())
    private val prefs = application.getSharedPreferences("workout_ui_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _plans = workoutRepository.getAllPlans()

    private val _hiddenTemplateIds = MutableStateFlow<Set<Int>>(loadHiddenTemplateIds())

    private fun loadHiddenTemplateIds(): Set<Int> {
        return prefs.getStringSet("hidden_template_ids", emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    private fun saveHiddenTemplateIds(ids: Set<Int>) {
        prefs.edit().putStringSet("hidden_template_ids", ids.map { it.toString() }.toSet()).apply()
    }

    val filteredPlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery) { plans, query ->
        if (query.isBlank()) plans else plans.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _planBuiltInStatus = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val planBuiltInStatus = _planBuiltInStatus.asStateFlow()

    val activePlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery) { plans, query ->
        val active = plans.filter { it.isActive }
        if (query.isBlank()) active else active.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val hiddenPlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery) { plans, query ->
        val inactive = plans.filter { !it.isActive }
        if (query.isBlank()) inactive else inactive.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedPlanTemplates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val selectedPlanTemplates: StateFlow<List<WorkoutTemplate>> = _selectedPlanTemplates.asStateFlow()

    private val _templateExercises = MutableStateFlow<Map<Int, List<ExerciseWithDetails>>>(emptyMap())
    val templateExercises: StateFlow<Map<Int, List<ExerciseWithDetails>>> = _templateExercises.asStateFlow()

    private val _selectedRegion = MutableStateFlow<BodyRegion?>(null)
    val selectedRegion: StateFlow<BodyRegion?> = _selectedRegion.asStateFlow()

    val templatesByRegion: StateFlow<List<Pair<WorkoutTemplate, List<ExerciseWithDetails>>>> = combine(
        filteredPlans,
        _selectedRegion
    ) { plans, region ->
        plans to region
    }.flatMapLatest { (plans, region) ->
        if (region == null) return@flatMapLatest flowOf(emptyList())
        flow {
            val result = mutableListOf<Pair<WorkoutTemplate, List<ExerciseWithDetails>>>()
            plans.forEach { plan ->
                val templates = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(plan.id)
                templates.forEach { template ->
                    val exercises = db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(template.id)

                    val isMatch = if (region == BodyRegion.FULL_BODY) {
                        plan.name.contains("фулбади", ignoreCase = true) ||
                        plan.name.contains("full body", ignoreCase = true) ||
                        template.name.contains("фулбади", ignoreCase = true) ||
                        template.name.contains("full body", ignoreCase = true) ||
                        exercises.any { it.exercise.bodyRegion == BodyRegion.FULL_BODY }
                    } else {
                        exercises.any { it.exercise.bodyRegion == region }
                    }

                    if (isMatch) {
                        result.add(template to exercises)
                    }
                }
            }
            emit(result)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _editingPlan = MutableStateFlow<Plan?>(null)
    val editingPlan: StateFlow<Plan?> = _editingPlan.asStateFlow()

    private val _editingTemplates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val editingTemplates: StateFlow<List<WorkoutTemplate>> = _editingTemplates.asStateFlow()

    private val _hiddenEditingTemplates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val hiddenEditingTemplates: StateFlow<List<WorkoutTemplate>> = _hiddenEditingTemplates.asStateFlow()

    private val _editingExercises = MutableStateFlow<Map<Int, List<ExerciseWithDetails>>>(emptyMap())
    val editingExercises: StateFlow<Map<Int, List<ExerciseWithDetails>>> = _editingExercises.asStateFlow()

    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val allExercises: StateFlow<List<Exercise>> = _allExercises.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.getAllActiveExercises().collect {
                _allExercises.value = it
            }
        }
        viewModelScope.launch {
            _plans.collect { plans ->
                val statusMap = mutableMapOf<Int, Boolean>()
                plans.forEach { plan ->
                    statusMap[plan.id] = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(plan.id).any { it.isBuiltIn }
                }
                _planBuiltInStatus.value = statusMap
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadTemplatesForPlan(planId: Int) {
        viewModelScope.launch {
            val result = workoutRepository.getWorkoutTemplatesByPlanId(planId)
            val templates = result?.workoutTemplates ?: emptyList()
            _selectedPlanTemplates.value = templates

            val exerciseMap = templates.associate { template ->
                template.id to db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(template.id)
            }
            _templateExercises.value = exerciseMap
        }
    }

    fun selectRegion(region: BodyRegion?) {
        _selectedRegion.value = region
    }

    fun startWorkout(planId: Int) {
        viewModelScope.launch {
            workoutRepository.createNextWorkoutSession(planId)
        }
    }

    fun createNewPlan() {
        viewModelScope.launch {
            val planId = workoutRepository.createNewPlan("Новый план")
            loadPlanForEditing(planId.toInt())
        }
    }

    fun loadPlanForEditing(planId: Int) {
        viewModelScope.launch {
            val plan = db.workoutPlanDao().getPlanById(planId)
            _editingPlan.value = plan
            if (plan != null) {
                refreshEditingData(plan.id)
            }
        }
    }

    private suspend fun refreshEditingData(planId: Int) {
        val allTemplates = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(planId)
        val hiddenIds = _hiddenTemplateIds.value

        _editingTemplates.value = allTemplates.filter { it.id !in hiddenIds }
        _hiddenEditingTemplates.value = allTemplates.filter { it.id in hiddenIds }

        val exerciseMap = allTemplates.associate { template ->
            template.id to db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(template.id)
        }
        _editingExercises.value = exerciseMap
    }

    fun updatePlanName(name: String) {
        val current = _editingPlan.value ?: return
        viewModelScope.launch {
            val updated = current.copy(name = name)
            workoutRepository.updatePlan(updated)
            _editingPlan.value = updated
        }
    }

    fun addWorkoutDay() {
        val planId = _editingPlan.value?.id ?: return
        viewModelScope.launch {
            val count = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(planId).size + 1
            workoutRepository.appendWorkoutTemplate("День $count", planId)
            refreshEditingData(planId)
        }
    }

    fun updateTemplateName(template: WorkoutTemplate, newName: String) {
        viewModelScope.launch {
            db.workoutPlanDao().updateWorkoutTemplate(template.copy(name = newName))
            _editingPlan.value?.let { refreshEditingData(it.id) }
        }
    }

    fun hideTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            val currentHidden = _hiddenTemplateIds.value.toMutableSet()
            currentHidden.add(template.id)
            _hiddenTemplateIds.value = currentHidden
            saveHiddenTemplateIds(currentHidden)
            _editingPlan.value?.let { refreshEditingData(it.id) }
        }
    }

    fun restoreTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            val currentHidden = _hiddenTemplateIds.value.toMutableSet()
            currentHidden.remove(template.id)
            _hiddenTemplateIds.value = currentHidden
            saveHiddenTemplateIds(currentHidden)
            _editingPlan.value?.let { refreshEditingData(it.id) }
        }
    }

    fun addExerciseToTemplate(templateId: Int, exercise: Exercise) {
        viewModelScope.launch {
            val exerciseToDo = ExerciseToDo(
                exerciseId = exercise.id,
                workoutTemplateId = templateId,
                sets = 3,
                reps = 12,
                order = 0,
                trainingMode = TrainingMode.HYPERTROPHY
            )
            workoutRepository.appendExerciseToWorkoutTemplate(exerciseToDo)
            _editingPlan.value?.let { refreshEditingData(it.id) }
        }
    }

    fun updateExerciseDetails(exerciseToDo: ExerciseToDo) {
        viewModelScope.launch {
            workoutRepository.updateExerciseInWorkoutTemplate(exerciseToDo)
            _editingPlan.value?.let { refreshEditingData(it.id) }
        }
    }

    fun removeExercise(exerciseToDo: ExerciseToDo) {
        viewModelScope.launch {
            workoutRepository.removeExerciseFromWorkoutTemplate(exerciseToDo)
            _editingPlan.value?.let { refreshEditingData(it.id) }
        }
    }

    fun closeConstructor() {
        _editingPlan.value = null
        _editingTemplates.value = emptyList()
        _hiddenEditingTemplates.value = emptyList()
        _editingExercises.value = emptyMap()
    }

    fun togglePlanVisibility(plan: Plan) {
        viewModelScope.launch {
            if (plan.isActive) {
                workoutRepository.archivePlan(plan)
            } else {
                workoutRepository.restorePlan(plan)
            }
        }
    }
}
