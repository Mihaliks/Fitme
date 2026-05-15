package com.example.fitme.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.entities.relations.ExerciseWithDetails
import com.example.fitme.data.models.NextWorkoutPlan
import com.example.fitme.data.models.NextWorkoutPreview
import com.example.fitme.data.repositories.ExerciseRepository
import com.example.fitme.data.repositories.UserRepository
import com.example.fitme.data.repositories.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.sqlite.db.SimpleSQLiteQuery
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers

data class HistoryItem(
    val session: WorkoutSession,
    val templateName: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val workoutRepository = WorkoutRepository(db)
    private val userRepository = UserRepository(db.userDao())
    private val exerciseRepository = ExerciseRepository(db.exerciseDao())
    private val prefs = application.getSharedPreferences("workout_ui_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _plans = workoutRepository.getAllPlans()

    private val seedPlanNames = listOf("Фулбади для новичка", "Верх / низ")
    private val seedTemplateNames = listOf("Фулбади A", "Фулбади B", "Верх", "Низ")

    val planBuiltInStatus: StateFlow<Map<Int, Boolean>> = _plans.flatMapLatest { plans ->
        flow {
            val statusMap = plans.associate { plan ->
                val templates = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(plan.id)
                val isBuiltIn = templates.any { it.isBuiltIn || it.name in seedTemplateNames } || 
                               seedPlanNames.any { it.equals(plan.name.trim(), ignoreCase = true) }
                plan.id to isBuiltIn
            }
            emit(statusMap)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val filteredPlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery, planBuiltInStatus) { plans, query, status ->
        val builtIn = plans.filter { status[it.id] == true }
        if (query.isBlank()) builtIn else builtIn.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery, planBuiltInStatus) { plans, query, status ->
        val activeCustom = plans.filter { it.isActive && status[it.id] == false }
        if (query.isBlank()) activeCustom else activeCustom.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenPlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery, planBuiltInStatus) { plans, query, status ->
        val inactiveCustom = plans.filter { !it.isActive && status[it.id] == false }
        if (query.isBlank()) inactiveCustom else inactiveCustom.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlanId: StateFlow<Int?> = userRepository.observeActivePlan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _nextWorkoutPreview = MutableStateFlow<NextWorkoutPreview?>(null)
    val nextWorkoutPreview: StateFlow<NextWorkoutPreview?> = _nextWorkoutPreview.asStateFlow()

    private val _selectedPlan = MutableStateFlow<Plan?>(null)
    val selectedPlan: StateFlow<Plan?> = _selectedPlan.asStateFlow()

    private val _selectedPlanTemplates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val selectedPlanTemplates: StateFlow<List<WorkoutTemplate>> = _selectedPlanTemplates.asStateFlow()

    private val _templateExercises = MutableStateFlow<Map<Int, List<ExerciseWithDetails>>>(emptyMap())
    val templateExercises: StateFlow<Map<Int, List<ExerciseWithDetails>>> = _templateExercises.asStateFlow()

    private val _currentSession = MutableStateFlow<NextWorkoutPlan?>(null)
    val currentSession = _currentSession.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex = _currentExerciseIndex.asStateFlow()

    private val _selectedRegion = MutableStateFlow<BodyRegion?>(null)
    val selectedRegion: StateFlow<BodyRegion?> = _selectedRegion.asStateFlow()

    val templatesByRegion: StateFlow<List<Pair<WorkoutTemplate, List<ExerciseWithDetails>>>> = _selectedRegion.flatMapLatest { region ->
        if (region == null) return@flatMapLatest flowOf(emptyList())
        workoutRepository.getBuiltInWorkoutTemplates().flatMapLatest { templates ->
            flow {
                val result = templates.mapNotNull { template ->
                    val exercises = db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(template.id)
                    val isMatch = if (region == BodyRegion.FULL_BODY) {
                        template.name.contains("фулбади", ignoreCase = true) ||
                        template.name.contains("full body", ignoreCase = true) ||
                        exercises.any { it.exercise.bodyRegion == BodyRegion.FULL_BODY }
                    } else {
                        exercises.any { it.exercise.bodyRegion == region }
                    }
                    if (isMatch) template to exercises else null
                }
                emit(result)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _isCreatingNewPlan = MutableStateFlow(false)
    val isCreatingNewPlan: StateFlow<Boolean> = _isCreatingNewPlan.asStateFlow()

    private val _showEmptyPlanWarning = MutableStateFlow(false)
    val showEmptyPlanWarning: StateFlow<Boolean> = _showEmptyPlanWarning.asStateFlow()

    private val _hiddenTemplateIds = MutableStateFlow<Set<Int>>(loadHiddenTemplateIds())

    private val _skippedSessionIds = MutableStateFlow<Set<Int>>(emptySet())
    val skippedSessionIds: StateFlow<Set<Int>> = _skippedSessionIds.asStateFlow()

    private val _workoutHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val workoutHistory: StateFlow<List<HistoryItem>> = _workoutHistory.asStateFlow()

    private var workoutStartTime: Long = 0L

    init {
        viewModelScope.launch {
            exerciseRepository.getAllActiveExercises().collect { _allExercises.value = it }
        }
        viewModelScope.launch {
            _skippedSessionIds.value = prefs.getStringSet("skipped_sessions", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        }
        viewModelScope.launch {
            activePlanId.collectLatest { planId ->
                if (planId != null) {
                    val preview = workoutRepository.peekNextWorkoutSession(planId)
                    _nextWorkoutPreview.value = preview
                } else {
                    _nextWorkoutPreview.value = null
                }
            }
        }
    }

    private fun loadHiddenTemplateIds(): Set<Int> {
        return prefs.getStringSet("hidden_template_ids", emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val query = """
                SELECT ws.id, ws.workout_template_id, ws.date, ws.total_duration, wt.name 
                FROM workout_sessions ws 
                LEFT JOIN workout_templates wt ON wt.id = ws.workout_template_id 
                ORDER BY ws.date DESC, ws.id DESC
            """.trimIndent()

            val cursor = db.query(SimpleSQLiteQuery(query))
            val list = mutableListOf<HistoryItem>()
            val dateCol = cursor.getColumnIndex("date")
            val nameCol = cursor.getColumnIndex("name")
            val durCol = cursor.getColumnIndex("total_duration")
            val idCol = cursor.getColumnIndex("id")
            val templateIdCol = cursor.getColumnIndex("workout_template_id")

            while (cursor.moveToNext()) {
                val id = cursor.getInt(idCol)
                val templateId = if (cursor.isNull(templateIdCol)) null else cursor.getInt(templateIdCol)
                // SQLite dates are long timestamps sometimes? Let's check converters later! Wait! Room stores LocalDate as String or Long depending on DateConverter.
                // Let's assume Room uses DateConverter "java.time.LocalDate.toString()" based on usual practices.
                val dateString = cursor.getString(dateCol)
                val duration = if (cursor.isNull(durCol)) null else cursor.getInt(durCol)
                val templateName = if (nameCol >= 0 && !cursor.isNull(nameCol)) cursor.getString(nameCol) else "Неизвестная тренировка"

                try {
                    val date = if (dateString != null && dateString.contains("-")) {
                        LocalDate.parse(dateString)
                    } else if (dateString != null) {
                        try { LocalDate.ofEpochDay(dateString.toLong()) } catch(e: Exception) { LocalDate.now() }
                    } else {
                        LocalDate.now()
                    }
                    val session = WorkoutSession(id, templateId, date, duration)
                    list.add(HistoryItem(session, templateName))
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
            cursor.close()
            _workoutHistory.value = list
        }
    }

    fun markWorkoutSkipped(planId: Int) {
        viewModelScope.launch {
            val session = workoutRepository.createNextWorkoutSession(planId)
            if (session != null) {
                val current = _skippedSessionIds.value.toMutableSet()
                current.add(session.sessionId)
                _skippedSessionIds.value = current
                prefs.edit().putStringSet("skipped_sessions", current.map { it.toString() }.toSet()).apply()
                _nextWorkoutPreview.value = workoutRepository.peekNextWorkoutSession(planId)
                loadHistory()
            }
        }
    }

    fun markWorkoutVisited(planId: Int) {
        viewModelScope.launch {
            val session = workoutRepository.createNextWorkoutSession(planId)
            if (session != null) {
                _nextWorkoutPreview.value = workoutRepository.peekNextWorkoutSession(planId)
                loadHistory()
            }
        }
    }

    private fun saveHiddenTemplateIds(ids: Set<Int>) {
        prefs.edit().putStringSet("hidden_template_ids", ids.map { it.toString() }.toSet()).apply()
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun selectPlan(plan: Plan?) {
        _selectedPlan.value = plan
        if (plan != null) loadTemplatesForPlan(plan.id)
        else {
            _selectedPlanTemplates.value = emptyList()
            _templateExercises.value = emptyMap()
        }
    }

    fun loadTemplatesForPlan(planId: Int) {
        viewModelScope.launch {
            val result = workoutRepository.getWorkoutTemplatesByPlanId(planId)
            val templates = result?.workoutTemplates ?: emptyList()
            _selectedPlanTemplates.value = templates
            _templateExercises.value = templates.associate { 
                it.id to db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(it.id) 
            }
        }
    }

    fun selectRegion(region: BodyRegion?) { _selectedRegion.value = region }

    fun startWorkout(planId: Int) {
        viewModelScope.launch {
            _currentSession.value = workoutRepository.createNextWorkoutSession(planId)
            _currentExerciseIndex.value = 0
            workoutStartTime = System.currentTimeMillis()
        }
    }

    fun startWorkoutFromTemplate(templateId: Int) {
        viewModelScope.launch {
            val session = workoutRepository.createWorkoutSessionFromTemplate(templateId)
            if (session != null) {
                _currentSession.value = session
                _currentExerciseIndex.value = 0
                workoutStartTime = System.currentTimeMillis()
            }
        }
    }

    fun nextExercise() {
        val session = _currentSession.value ?: return
        if (_currentExerciseIndex.value < session.exercises.size - 1) _currentExerciseIndex.value++
    }

    fun previousExercise() { if (_currentExerciseIndex.value > 0) _currentExerciseIndex.value-- }

    fun finishSession() {
        val sessionId = _currentSession.value?.sessionId
        val durationMinutes = if (workoutStartTime > 0) ((System.currentTimeMillis() - workoutStartTime) / 60000).toInt() else 0

        _currentSession.value = null
        _currentExerciseIndex.value = 0
        workoutStartTime = 0L

        viewModelScope.launch {
            if (sessionId != null) {
                val session = db.workoutSessionDao().getWorkoutSessionById(sessionId)
                if (session != null) {
                    db.workoutSessionDao().updateWorkoutSession(session.copy(totalDuration = durationMinutes))
                }
                loadHistory()
            }
            val planId = activePlanId.value
            if (planId != null) {
                _nextWorkoutPreview.value = workoutRepository.peekNextWorkoutSession(planId)
            }
        }
    }

    fun selectPlanAsActive(planId: Int?) { viewModelScope.launch { userRepository.setActivePlan(planId) } }

    fun createNewPlan() {
        viewModelScope.launch {
            val planId = workoutRepository.createNewPlan("Новый план")
            _isCreatingNewPlan.value = true
            loadPlanForEditing(planId.toInt())
        }
    }

    fun loadPlanForEditing(planId: Int) {
        viewModelScope.launch {
            if (planBuiltInStatus.value[planId] == true) return@launch
            val plan = db.workoutPlanDao().getPlanById(planId)
            if (!_isCreatingNewPlan.value) _isCreatingNewPlan.value = false
            _editingPlan.value = plan
            if (plan != null) refreshEditingData(plan.id)
        }
    }

    private suspend fun refreshEditingData(planId: Int) {
        val allTemplates = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(planId)
        val hiddenIds = _hiddenTemplateIds.value
        _editingTemplates.value = allTemplates.filter { it.id !in hiddenIds }
        _hiddenEditingTemplates.value = allTemplates.filter { it.id in hiddenIds }
        _editingExercises.value = allTemplates.associate { 
            it.id to db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(it.id)
        }
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

    fun reorderTemplates(orderedIds: List<Int>) {
        val planId = _editingPlan.value?.id ?: return
        viewModelScope.launch {
            workoutRepository.reorderWorkoutTemplates(planId, orderedIds)
            refreshEditingData(planId)
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
                sets = 3, reps = 12, order = 0,
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

    fun dismissEmptyPlanWarning() {
        _showEmptyPlanWarning.value = false
    }

    fun closeConstructor(force: Boolean = false) {
        val templates = _editingTemplates.value
        if (!force && templates.isEmpty()) {
            _showEmptyPlanWarning.value = true
            return
        }
        _editingPlan.value = null
        _editingTemplates.value = emptyList()
        _hiddenEditingTemplates.value = emptyList()
        _editingExercises.value = emptyMap()
        _isCreatingNewPlan.value = false
        _showEmptyPlanWarning.value = false
    }

    fun cancelPlanCreation() {
        val plan = _editingPlan.value
        if (plan != null && _isCreatingNewPlan.value) {
            viewModelScope.launch {
                db.workoutPlanDao().deletePlan(plan)
                closeConstructor(force = true)
            }
        } else {
            closeConstructor(force = true)
        }
    }

    fun togglePlanVisibility(plan: Plan) {
        viewModelScope.launch {
            if (plan.isActive) workoutRepository.archivePlan(plan)
            else workoutRepository.restorePlan(plan)
        }
    }
}
