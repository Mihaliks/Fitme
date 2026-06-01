package com.example.fitme.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.User
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.entities.relations.ExerciseWithDetails
import com.example.fitme.data.models.NextWorkoutPlan
import com.example.fitme.data.models.NextWorkoutPreview
import com.example.fitme.data.repositories.ExerciseRepository
import com.example.fitme.data.repositories.NoteRepository
import com.example.fitme.data.repositories.UserRepository
import com.example.fitme.data.repositories.WorkoutRepository
import com.example.fitme.data.seed.DefaultSeedData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

enum class RecordSlot {
    LOW_REP,
    HIGH_REP
}

data class ExerciseRecordValue(
    val reps: Int? = null,
    val weight: Double? = null,
    val duration: Int? = null
)

fun formatDurationForUi(durationSeconds: Int): String {
    if (durationSeconds < 60) return "$durationSeconds сек"
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    return buildString {
        append(minutes)
        append(" мин")
        if (seconds > 0) {
            append(" ")
            append(seconds)
            append(" сек")
        }
    }
}

data class ExerciseRecordState(
    val lowRep: ExerciseRecordValue? = null,
    val highRep: ExerciseRecordValue? = null
)

fun RecordSlot.displayName(): String = when (this) {
    RecordSlot.LOW_REP -> "Малоповторный"
    RecordSlot.HIGH_REP -> "Многоповторный"
}

fun ExerciseRecordValue.displayText(): String = when {
    duration != null && weight != null -> "${weight} кг / ${formatDurationForUi(duration)}"
    duration != null -> formatDurationForUi(duration)
    weight != null && reps != null -> "${weight} кг × $reps"
    weight != null -> "${weight} кг"
    reps != null -> "$reps повторений"
    else -> "—"
}

fun PerformedSet.displayText(): String = when {
    duration != null && weight != null -> "${weight} кг / ${formatDurationForUi(duration)}"
    duration != null -> formatDurationForUi(duration)
    reps != null && weight != null -> "${reps} x ${weight} кг"
    reps != null -> "$reps повторений"
    weight != null -> "${weight} кг"
    else -> "—"
}

private fun ExerciseRecordValue.toJson(): JSONObject = JSONObject().apply {
    reps?.let { put("reps", it) }
    weight?.let { put("weight", it) }
    duration?.let { put("duration", it) }
}

private fun JSONObject.toRecordValue(): ExerciseRecordValue = ExerciseRecordValue(
    reps = if (has("reps")) optInt("reps").takeIf { !isNull("reps") } else null,
    weight = if (has("weight")) optDouble("weight").takeIf { !isNull("weight") } else null,
    duration = if (has("duration")) optInt("duration").takeIf { !isNull("duration") } else null,
)

data class PerformedExercise(
    val name: String,
    val plannedSets: Int,
    val plannedReps: Int,
    val plannedDuration: Int? = null,
    val actualSets: Int,
    val actualReps: Int,
    val actualDuration: Int? = null,
    val plannedWeight: Double?,
    val actualWeight: Double? = null,
    val performedSets: List<PerformedSet> = emptyList()
)

data class PerformedSet(
    val setIndex: Int,
    val reps: Int?,
    val weight: Double?,
    val duration: Int? = null
)

data class HistoryItem(
    val session: WorkoutSession,
    val templateName: String,
    val performedExercises: List<PerformedExercise> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val workoutRepository = WorkoutRepository(db)
    private val userRepository = UserRepository(db.userDao())
    private val exerciseRepository = ExerciseRepository(db.exerciseDao())
    private val prefs = application.getSharedPreferences("workout_ui_prefs", Context.MODE_PRIVATE)
    private val recordsPrefs = application.getSharedPreferences("exercise_records_ui_prefs", Context.MODE_PRIVATE)
    private val periodizationDisplayEnabledKey = "periodization_display_enabled"
    private val exerciseRecordsKey = "exercise_records"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _exerciseSearchQuery = MutableStateFlow("")
    val exerciseSearchQuery: StateFlow<String> = _exerciseSearchQuery.asStateFlow()

    private val _selectedExerciseMuscleGroup = MutableStateFlow<MuscleGroup?>(null)
    val selectedExerciseMuscleGroup: StateFlow<MuscleGroup?> = _selectedExerciseMuscleGroup.asStateFlow()

    private val _plans = workoutRepository.getAllPlans()

    private val seedPlanNames: List<String> = DefaultSeedData.plans.map { it.name.trim() }
    private val seedTemplateNames: List<String> = DefaultSeedData.workoutTemplates.map { it.name.trim() }

    val planBuiltInStatus: StateFlow<Map<Int, Boolean>> = _plans.flatMapLatest { plans ->
        flow {
            val statusMap = plans.associate { plan ->
                val templates = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(plan.id)
                val isBuiltIn = templates.any { it.isBuiltIn } ||
                        templates.any { tpl -> seedTemplateNames.any { seedName -> seedName.equals(tpl.name.trim(), ignoreCase = true) } } ||
                        seedPlanNames.any { seedPlan -> seedPlan.equals(plan.name.trim(), ignoreCase = true) }

                plan.id to isBuiltIn
            }
            emit(statusMap)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val filteredPlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery, planBuiltInStatus) { plans, query, status ->
        val builtIn = plans.filter { status[it.id] == true }
        if (query.isBlank()) builtIn else builtIn.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExercises: StateFlow<List<Exercise>> = combine(
        _exerciseSearchQuery,
        _selectedExerciseMuscleGroup
    ) { query, muscleGroup -> query.trim() to muscleGroup }
        .flatMapLatest { (query, muscleGroup) ->
            val baseFlow = when {
                query.isBlank() && muscleGroup == null -> exerciseRepository.getAllActiveExercises()
                query.isBlank() -> exerciseRepository.getAllExercisesByMuscleGroup(muscleGroup!!)
                muscleGroup == null -> exerciseRepository.searchActiveExercises(query)
                else -> exerciseRepository.searchActiveExercises(query).map { exercises ->
                    exercises.filter { it.muscle == muscleGroup }
                }
            }
            baseFlow
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _nextWorkoutPreview = MutableStateFlow<NextWorkoutPreview?>(null)
    val nextWorkoutPreview: StateFlow<NextWorkoutPreview?> = _nextWorkoutPreview.asStateFlow()

    private val _selectedPlan = MutableStateFlow<Plan?>(null)
    val selectedPlan: StateFlow<Plan?> = _selectedPlan.asStateFlow()

    private val _selectedPlanTemplates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val selectedPlanTemplates: StateFlow<List<WorkoutTemplate>> = _selectedPlanTemplates.asStateFlow()

    private val _templateExercises = MutableStateFlow<Map<Int, List<ExerciseWithDetails>>>(emptyMap())
    val templateExercises: StateFlow<Map<Int, List<ExerciseWithDetails>>> = _templateExercises.asStateFlow()

    private val _currentSession = MutableStateFlow<NextWorkoutPlan?>(null)
    private val _periodizationDisplayEnabled = MutableStateFlow(
         prefs.getBoolean(periodizationDisplayEnabledKey, true)
     )
     val periodizationDisplayEnabled: StateFlow<Boolean> = _periodizationDisplayEnabled.asStateFlow()

     private val _planPeriodizationMode = MutableStateFlow<Map<Int, String>>(loadPlanPeriodizationModes())
      private var nextDraftTemplateId = -1
      private var nextDraftExerciseId = -1

    val currentSession: StateFlow<NextWorkoutPlan?> = combine(_currentSession, periodizationDisplayEnabled) { session, enabled ->
        if (enabled) session else session?.let { transformSessionForDisabledPeriodization(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    private val _validationErrors = MutableStateFlow<List<String>>(emptyList())
    val validationErrors: StateFlow<List<String>> = _validationErrors.asStateFlow()

    private val _hiddenTemplateIds = MutableStateFlow<Set<Int>>(loadHiddenTemplateIds())

    private val _skippedSessionIds = MutableStateFlow<Set<Int>>(emptySet())
    val skippedSessionIds: StateFlow<Set<Int>> = _skippedSessionIds.asStateFlow()

    private val _workoutHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val workoutHistory: StateFlow<List<HistoryItem>> = _workoutHistory.asStateFlow()

    private val _recentHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    private val _exerciseRecords = MutableStateFlow(loadExerciseRecords())
    val exerciseRecords: StateFlow<Map<Int, ExerciseRecordState>> = _exerciseRecords.asStateFlow()

    private var workoutStartTime: Long = 0L
    private val noteDao = db.noteDao()

    init {
        viewModelScope.launch {
            _user.value = userRepository.getUser()
        }
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

    fun setPeriodizationDisplayEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(periodizationDisplayEnabledKey, enabled) }
        _periodizationDisplayEnabled.value = enabled
     }

    fun setExerciseSearchQuery(query: String) {
        _exerciseSearchQuery.value = query
    }

    fun setSelectedExerciseMuscleGroup(muscleGroup: MuscleGroup?) {
        _selectedExerciseMuscleGroup.value = muscleGroup
    }

    fun clearExerciseFilters() {
        _exerciseSearchQuery.value = ""
        _selectedExerciseMuscleGroup.value = null
    }

    private fun transformSessionForDisabledPeriodization(session: NextWorkoutPlan): NextWorkoutPlan {
        val transformedExercises = session.exercises.map { exercise ->
            val exerciseToDo = exercise.exerciseToDo
            if (!exerciseToDo.periodizationEnabled) {
                exercise.copy(
                    plannedSets = exerciseToDo.sets,
                    plannedReps = exerciseToDo.reps,
                    plannedWeight = exerciseToDo.weight
                )
            } else {
                exercise.copy(
                    chosenMode = TrainingMode.NONE,
                    plannedSets = exerciseToDo.sets,
                    plannedReps = exerciseToDo.reps,
                    plannedWeight = exerciseToDo.weight
                )
            }
        }

        return session.copy(exercises = transformedExercises)
    }

    private fun loadHiddenTemplateIds(): Set<Int> {
         return prefs.getStringSet("hidden_template_ids", emptySet())
             ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
     }

     private fun loadPlanPeriodizationModes(): Map<Int, String> {
         val modeString = prefs.getString("plan_periodization_modes", "") ?: ""
         if (modeString.isEmpty()) return emptyMap()
         return modeString.split("|").mapNotNull {
             val parts = it.split(":")
             if (parts.size == 2) parts[0].toIntOrNull()?.let { planId -> planId to parts[1] } else null
         }.toMap()
     }

     private fun savePlanPeriodizationMode(planId: Int, mode: String) {
         val current = _planPeriodizationMode.value.toMutableMap()
         current[planId] = mode
         _planPeriodizationMode.value = current
         val modeString = current.map { "${it.key}:${it.value}" }.joinToString("|")
         prefs.edit {
             putString("plan_periodization_modes", modeString)
         }
     }

     private fun getPlanPeriodizationMode(planId: Int): String {
         return _planPeriodizationMode.value[planId] ?: "A"
     }

     private fun togglePlanPeriodizationMode(planId: Int) {
         val currentMode = getPlanPeriodizationMode(planId)
         val newMode = if (currentMode == "A") "B" else "A"
         savePlanPeriodizationMode(planId, newMode)
     }

    fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val dbItems = loadHistoryItems()
            _workoutHistory.value = (_recentHistory.value + dbItems)
                .distinctBy { it.session.id }
                .sortedWith(compareByDescending<HistoryItem> { it.session.date }
                    .thenByDescending { it.session.id })
        }
    }

     private suspend fun loadHistoryItems(): List<HistoryItem> {
         val workoutSessionDao = db.workoutSessionDao()
         val workoutPlanDao = db.workoutPlanDao()
         val exerciseToDoDao = db.exerciseToDoDao()
         val noteRepository = NoteRepository(db)
         val sessions = mutableListOf<WorkoutSession>()
         val pagingSource: PagingSource<Int, WorkoutSession> = workoutSessionDao.getAllWorkoutSessions()
         var key: Int? = null

         while (true) {
             val params: PagingSource.LoadParams<Int> = if (key == null) {
                 PagingSource.LoadParams.Refresh(
                     key = null,
                     loadSize = HISTORY_PAGE_SIZE,
                     placeholdersEnabled = false,
                 )
             } else {
                 PagingSource.LoadParams.Append(
                     key = key,
                     loadSize = HISTORY_PAGE_SIZE,
                     placeholdersEnabled = false,
                 )
             }

             when (val result = pagingSource.load(params)) {
                 is PagingSource.LoadResult.Page -> {
                     sessions += result.data
                     key = result.nextKey ?: break
                 }

                 is PagingSource.LoadResult.Error -> throw result.throwable
                 is PagingSource.LoadResult.Invalid -> break
             }
         }

         return sessions.map { session ->
             val templateName = session.workoutTemplateId
                 ?.let { workoutPlanDao.getWorkoutTemplateById(it)?.name }
                 ?: "Неизвестная тренировка"

             // Восстанавливаем информацию о выполненных упражнениях из Notes
             val performedExercises = if (session.workoutTemplateId != null) {
                 val exercises = exerciseToDoDao.getExerciseDetailsForWorkoutOnce(session.workoutTemplateId)
                 exercises.map { detail ->
                     val notes = noteRepository.getNotesForExerciseInSessionOnce(
                         workoutSessionId = session.id,
                         exerciseToDoId = detail.exerciseToDo.id
                     )
                      PerformedExercise(
                         name = detail.exercise.name,
                         plannedSets = detail.exerciseToDo.sets,
                         plannedReps = detail.exerciseToDo.reps,
                          plannedDuration = detail.exerciseToDo.duration,
                         actualSets = if (notes.isEmpty()) detail.exerciseToDo.sets else notes.size,
                         actualReps = notes.lastOrNull()?.reps ?: detail.exerciseToDo.reps,
                          actualDuration = notes.lastOrNull()?.duration ?: detail.exerciseToDo.duration,
                          plannedWeight = detail.exerciseToDo.weight,
                          actualWeight = notes.lastOrNull { it.weight != null }?.weight ?: detail.exerciseToDo.weight,
                          performedSets = notes.map { note ->
                              PerformedSet(
                                  setIndex = note.setIndex,
                                  reps = note.reps,
                                  weight = note.weight,
                                  duration = note.duration
                              )
                          }
                     )
                 }
             } else {
                 emptyList()
             }

             HistoryItem(session = session, templateName = templateName, performedExercises = performedExercises)
         }
     }

    private companion object {
        const val HISTORY_PAGE_SIZE = 50
    }

    fun markWorkoutSkipped(planId: Int) {
        viewModelScope.launch {
            val session = workoutRepository.createNextWorkoutSession(planId)
            if (session != null) {
                val current = _skippedSessionIds.value.toMutableSet()
                current.add(session.sessionId)
                _skippedSessionIds.value = current
                prefs.edit { putStringSet("skipped_sessions", current.map { it.toString() }.toSet()) }
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
              var session = workoutRepository.createNextWorkoutSession(planId)
              if (session != null) {
                  // Respect periodization display setting: if enabled, apply plan mode, otherwise force NONE
                  session = if (periodizationDisplayEnabled.value) applyPlanPeriodizationMode(session, planId) else transformSessionForDisabledPeriodization(session)
                  _currentSession.value = session
                  _currentExerciseIndex.value = 0
                  workoutStartTime = System.currentTimeMillis()
              }
          }
      }

      fun startWorkoutFromTemplate(templateId: Int) {
          viewModelScope.launch {
              val session = workoutRepository.createWorkoutSessionFromTemplate(templateId)
              if (session != null) {
                  val prepared = if (periodizationDisplayEnabled.value) session else transformSessionForDisabledPeriodization(session)
                  _currentSession.value = prepared
                  _currentExerciseIndex.value = 0
                  workoutStartTime = System.currentTimeMillis()
              }
          }
      }

      private suspend fun applyPlanPeriodizationMode(session: NextWorkoutPlan, planId: Int): NextWorkoutPlan {
          val mode = getPlanPeriodizationMode(planId)
          val transformedExercises = session.exercises.map { exercise ->
              val exerciseToDo = exercise.exerciseToDo
              if (!exerciseToDo.periodizationEnabled) {
                  exercise
              } else {
                  val newMode = when (mode) {
                      "A" -> exerciseToDo.modeA ?: exercise.chosenMode
                      "B" -> exerciseToDo.modeB ?: exercise.chosenMode
                      else -> exercise.chosenMode
                  }
                  val newParams = when (newMode) {
                      exerciseToDo.modeA -> {
                          exercise.copy(
                              chosenMode = newMode,
                              plannedSets = exerciseToDo.setsA ?: exercise.plannedSets,
                              plannedReps = exerciseToDo.repsA ?: exercise.plannedReps,
                              plannedWeight = exerciseToDo.weightA ?: exercise.plannedWeight
                          )
                      }
                      exerciseToDo.modeB -> {
                          exercise.copy(
                              chosenMode = newMode,
                              plannedSets = exerciseToDo.setsB ?: exercise.plannedSets,
                              plannedReps = exerciseToDo.repsB ?: exercise.plannedReps,
                              plannedWeight = exerciseToDo.weightB ?: exercise.plannedWeight
                          )
                      }
                      else -> exercise
                  }
                  newParams
              }
          }
          return session.copy(exercises = transformedExercises)
      }

     fun nextExercise() {
         val session = _currentSession.value ?: return
         if (_currentExerciseIndex.value < session.exercises.size - 1) _currentExerciseIndex.value++
     }

     fun previousExercise() { if (_currentExerciseIndex.value > 0) _currentExerciseIndex.value-- }

      fun finishSession() {
          val session = _currentSession.value
          val sessionId = session?.sessionId
          val durationMinutes = if (workoutStartTime > 0) ((System.currentTimeMillis() - workoutStartTime) / 60000).toInt() else 0

          val performed = run {
              if (session == null) return@run emptyList<PerformedExercise>()
              val list = mutableListOf<PerformedExercise>()
              kotlinx.coroutines.runBlocking {
                  for (ex in session.exercises) {
                      val notes = noteDao.getNotesForExerciseInSessionOnce(session.sessionId, ex.exerciseToDo.id)

                      val actualSets = if (notes.isEmpty()) ex.plannedSets else notes.size
                      val actualReps = notes.lastOrNull()?.reps ?: ex.plannedReps
                       list += PerformedExercise(
                          name = ex.exercise.name,
                          plannedSets = ex.plannedSets,
                          plannedReps = ex.plannedReps,
                          plannedDuration = ex.exerciseToDo.duration,
                          actualSets = actualSets,
                          actualReps = actualReps,
                          actualDuration = notes.lastOrNull()?.duration ?: ex.exerciseToDo.duration,
                          plannedWeight = ex.plannedWeight,
                          actualWeight = notes.lastOrNull { it.weight != null }?.weight ?: ex.plannedWeight,
                          performedSets = notes.map { note ->
                              PerformedSet(
                                  setIndex = note.setIndex,
                                  reps = note.reps,
                                  weight = note.weight,
                                  duration = note.duration
                              )
                          }
                     )
                 }
              }
              list
          }

          if (session != null) {
              val templateName = session.template.name
              val historyItem = HistoryItem(
                  session = WorkoutSession(id = session.sessionId, workoutTemplateId = session.template.id, date = java.time.LocalDate.now(), totalDuration = durationMinutes),
                  templateName = templateName,
                  performedExercises = performed
              )
              _recentHistory.value = listOf(historyItem) + _recentHistory.value
          }

          _currentSession.value = null
          _currentExerciseIndex.value = 0
          workoutStartTime = 0L

          viewModelScope.launch {
              if (sessionId != null) {
                  val dbSession = db.workoutSessionDao().getWorkoutSessionById(sessionId)
                  if (dbSession != null) {
                      db.workoutSessionDao().updateWorkoutSession(dbSession.copy(totalDuration = durationMinutes))
                  }
              }

               val planId = activePlanId.value
               if (planId != null && session != null) {
                   checkAndTogglePeriodizationIfCycleComplete(planId, session.template.id)
               }

               loadHistory()
               activePlanId.value?.let { planId ->
                   _nextWorkoutPreview.value = workoutRepository.peekNextWorkoutSession(planId)
               }
          }
      }

     private suspend fun checkAndTogglePeriodizationIfCycleComplete(planId: Int, currentTemplateId: Int) {
         val plan = db.workoutPlanDao().getPlanById(planId) ?: return
         val templates = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(planId)
         if (templates.isEmpty()) return
         val currentTemplate = templates.find { it.id == currentTemplateId } ?: return
         val isLastTemplate = currentTemplate.order == templates.maxOf { it.order }

         if (isLastTemplate) {
             togglePlanPeriodizationMode(planId)
         }
     }

    fun appendNoteForCurrentExercise(reps: Int? = null, weight: Double? = null, duration: Int? = null) {
        val session = _currentSession.value ?: return
        val index = _currentExerciseIndex.value
        val exercise = session.exercises.getOrNull(index) ?: return
        viewModelScope.launch {
            val noteRepo = com.example.fitme.data.repositories.NoteRepository(db)
            noteRepo.appendNote(
                workoutSessionId = session.sessionId,
                exerciseToDoId = exercise.exerciseToDo.id,
                modeUsed = exercise.chosenMode,
                reps = reps,
                weight = weight,
                duration = duration
            )
        }
    }

    fun observeNotesForCurrentExercise(): kotlinx.coroutines.flow.Flow<List<com.example.fitme.data.entities.Note>> {
        val session = _currentSession.value ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        val index = _currentExerciseIndex.value
        val exercise = session.exercises.getOrNull(index) ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return noteDao.getNotesForExerciseInSession(session.sessionId, exercise.exerciseToDo.id)
    }

    fun saveExerciseRecord(
        exerciseId: Int,
        slot: RecordSlot,
        value: ExerciseRecordValue
    ) {
        if (value.reps == null && value.weight == null && value.duration == null) return

        val current = _exerciseRecords.value.toMutableMap()
        val currentState = current[exerciseId] ?: ExerciseRecordState()
        val updatedState = when (slot) {
            RecordSlot.LOW_REP -> currentState.copy(lowRep = value)
            RecordSlot.HIGH_REP -> currentState.copy(highRep = value)
        }
        current[exerciseId] = updatedState
        _exerciseRecords.value = current
        persistExerciseRecords(current)
    }

    fun clearExerciseRecord(exerciseId: Int, slot: RecordSlot) {
        val current = _exerciseRecords.value.toMutableMap()
        val currentState = current[exerciseId] ?: return
        val updatedState = when (slot) {
            RecordSlot.LOW_REP -> currentState.copy(lowRep = null)
            RecordSlot.HIGH_REP -> currentState.copy(highRep = null)
        }

        if (updatedState.lowRep == null && updatedState.highRep == null) {
            current.remove(exerciseId)
        } else {
            current[exerciseId] = updatedState
        }

        _exerciseRecords.value = current
        persistExerciseRecords(current)
    }

    fun updateCurrentExercisePlanned(index: Int, sets: Int, reps: Int) {
        val session = _currentSession.value ?: return
        if (index < 0 || index >= session.exercises.size) return
        val updatedExercises = session.exercises.mapIndexed { i, ex ->
            if (i == index) ex.copy(plannedSets = sets, plannedReps = reps) else ex
        }
        _currentSession.value = session.copy(exercises = updatedExercises)
    }

    fun selectPlanAsActive(planId: Int?) { viewModelScope.launch { userRepository.setActivePlan(planId) } }

    fun activatePlan(plan: Plan) {
        viewModelScope.launch {
            if (!plan.isActive) {
                workoutRepository.restorePlan(plan)
            }
            userRepository.setActivePlan(plan.id)
        }
    }

    fun createNewPlan() {
        _isCreatingNewPlan.value = true
        _editingPlan.value = Plan(id = 0, name = "Новый план", isActive = true)
        _editingTemplates.value = emptyList()
        _hiddenEditingTemplates.value = emptyList()
        _editingExercises.value = emptyMap()
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

    fun savePlanName(name: String) {
        val current = _editingPlan.value ?: return
        val normalized = name.trim()
        _editingPlan.value = current.copy(name = normalized)
    }

    private fun validatePlan(): List<String> {
        val errors = mutableListOf<String>()
        val currentPlan = _editingPlan.value
        val draftTemplates = _editingTemplates.value
        val draftExercises = _editingExercises.value

        if (currentPlan == null || currentPlan.name.trim().isBlank()) {
            errors.add("• Название плана не может быть пустым")
        }

        if (draftTemplates.isEmpty()) {
            errors.add("• План должен содержать хотя бы один день тренировки")
        } else {
            draftTemplates.forEachIndexed { index, template ->
                if (template.name.trim().isBlank()) {
                    errors.add("• День ${index + 1}: необходимо указать название")
                }

                val exercises = draftExercises[template.id].orEmpty()
                if (exercises.isEmpty()) {
                    errors.add("• День \"${template.name}\": необходимо добавить хотя бы одно упражнение")
                }
            }
        }

        return errors
    }

    fun savePlanChanges(selectAsActive: Boolean = false): Boolean {
        val validationErrors = validatePlan()
        _validationErrors.value = validationErrors

        if (validationErrors.isNotEmpty()) {
            return false
        }

        val currentPlan = _editingPlan.value ?: return false
        val normalizedName = currentPlan.name.trim()

        val draftTemplates = _editingTemplates.value
        val hiddenTemplates = _hiddenEditingTemplates.value
        val draftExercises = _editingExercises.value
        val isCreating = _isCreatingNewPlan.value

        viewModelScope.launch {
            val persistedPlanId = if (isCreating || currentPlan.id <= 0) {
                workoutRepository.createNewPlan(normalizedName).toInt()
            } else {
                workoutRepository.updatePlan(currentPlan.copy(name = normalizedName))
                currentPlan.id
            }

            if (selectAsActive) {
                userRepository.setActivePlan(persistedPlanId)
            }

            val templateIdMap = mutableMapOf<Int, Int>()
            draftTemplates.forEach { draftTemplate ->
                if (draftTemplate.id > 0) {
                    db.workoutPlanDao().updateWorkoutTemplate(draftTemplate.copy(planId = persistedPlanId))
                    templateIdMap[draftTemplate.id] = draftTemplate.id
                } else {
                    val newId = workoutRepository
                        .appendWorkoutTemplate(draftTemplate.name, persistedPlanId)
                        .toInt()
                    templateIdMap[draftTemplate.id] = newId
                }
            }

            val hiddenIds = hiddenTemplates.map { it.id }
            val orderedVisibleIds = draftTemplates.mapNotNull { templateIdMap[it.id] ?: it.id.takeIf { id -> id > 0 } }
            val orderedIds = orderedVisibleIds + hiddenIds
            if (orderedIds.isNotEmpty()) {
                workoutRepository.reorderWorkoutTemplates(persistedPlanId, orderedIds)
            }

            draftTemplates.forEach { draftTemplate ->
                val persistedTemplateId = templateIdMap[draftTemplate.id] ?: draftTemplate.id
                if (persistedTemplateId <= 0) return@forEach

                val currentExerciseIds = db.exerciseToDoDao()
                    .getExerciseDetailsForWorkoutOnce(persistedTemplateId)
                    .map { it.exerciseToDo.id }
                    .toMutableSet()

                val exerciseDraft = draftExercises[draftTemplate.id].orEmpty()
                val orderedExerciseIds = mutableListOf<Int>()

                exerciseDraft.forEach { detail ->
                    val draftExercise = detail.exerciseToDo.copy(workoutTemplateId = persistedTemplateId)
                    if (draftExercise.id > 0) {
                        workoutRepository.updateExerciseInWorkoutTemplate(draftExercise)
                        orderedExerciseIds.add(draftExercise.id)
                        currentExerciseIds.remove(draftExercise.id)
                    } else {
                        val insertedId = workoutRepository.appendExerciseToWorkoutTemplate(
                            draftExercise.copy(id = 0, order = 0)
                        ).toInt()
                        orderedExerciseIds.add(insertedId)
                    }
                }

                currentExerciseIds.forEach { staleId ->
                    val stale = db.exerciseToDoDao().getExerciseToDoById(staleId) ?: return@forEach
                    workoutRepository.removeExerciseFromWorkoutTemplate(stale)
                }

                if (orderedExerciseIds.isNotEmpty()) {
                    workoutRepository.reorderExercisesInWorkoutTemplate(persistedTemplateId, orderedExerciseIds)
                }
            }

            _isCreatingNewPlan.value = false
            _editingPlan.value = db.workoutPlanDao().getPlanById(persistedPlanId)
            refreshEditingData(persistedPlanId)
        }

        return true
    }

    fun addWorkoutDay() {
        val currentTemplates = _editingTemplates.value
        val count = currentTemplates.size + _hiddenEditingTemplates.value.size + 1
        val draftTemplate = WorkoutTemplate(
            id = nextDraftTemplateId--,
            planId = _editingPlan.value?.id,
            name = "День $count",
            order = currentTemplates.size + 1,
            isBuiltIn = false,
        )
        _editingTemplates.value = currentTemplates + draftTemplate
        _editingExercises.value = _editingExercises.value + (draftTemplate.id to emptyList())
    }

    fun updateTemplateName(template: WorkoutTemplate, newName: String) {
        _editingTemplates.value = _editingTemplates.value.map {
            if (it.id == template.id) it.copy(name = newName) else it
        }
    }

    fun reorderTemplates(orderedIds: List<Int>) {
        val byId = _editingTemplates.value.associateBy { it.id }
        _editingTemplates.value = orderedIds.mapNotNull { byId[it] }
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
        val current = _editingExercises.value
        val currentList = current[templateId].orEmpty()
        val exerciseToDo = ExerciseToDo(
            id = nextDraftExerciseId--,
            exerciseId = exercise.id,
            workoutTemplateId = templateId,
            sets = 3,
            reps = 12,
            order = currentList.size + 1,
            trainingMode = TrainingMode.HYPERTROPHY
        )
        _editingExercises.value = current + (templateId to (currentList + ExerciseWithDetails(exerciseToDo, exercise)))
    }

    fun updateExerciseDetails(exerciseToDo: ExerciseToDo) {
        val templateId = exerciseToDo.workoutTemplateId
        val current = _editingExercises.value
        val updated = current[templateId].orEmpty().map { detail ->
            if (detail.exerciseToDo.id == exerciseToDo.id) detail.copy(exerciseToDo = exerciseToDo) else detail
        }
        _editingExercises.value = current + (templateId to updated)
    }

    private fun loadExerciseRecords(): Map<Int, ExerciseRecordState> {
        val raw = recordsPrefs.getString(exerciseRecordsKey, null).orEmpty()
        if (raw.isBlank()) return emptyMap()

        return runCatching {
            val root = JSONObject(raw)
            root.keys().asSequence().mapNotNull { key ->
                val exerciseId = key.toIntOrNull() ?: return@mapNotNull null
                val entry = root.optJSONObject(key) ?: return@mapNotNull null
                val low = entry.optJSONObject("lowRep")?.toRecordValue()
                val high = entry.optJSONObject("highRep")?.toRecordValue()
                exerciseId to ExerciseRecordState(lowRep = low, highRep = high)
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    private fun persistExerciseRecords(records: Map<Int, ExerciseRecordState>) {
        val root = JSONObject()
        records.forEach { (exerciseId, state) ->
            val entry = JSONObject()
            state.lowRep?.let { entry.put("lowRep", it.toJson()) }
            state.highRep?.let { entry.put("highRep", it.toJson()) }
            root.put(exerciseId.toString(), entry)
        }
        recordsPrefs.edit { putString(exerciseRecordsKey, root.toString()) }
    }

    fun removeExercise(exerciseToDo: ExerciseToDo) {
        val templateId = exerciseToDo.workoutTemplateId
        val current = _editingExercises.value
        val updated = current[templateId].orEmpty()
            .filterNot { it.exerciseToDo.id == exerciseToDo.id }
            .mapIndexed { index, detail ->
                detail.copy(exerciseToDo = detail.exerciseToDo.copy(order = index + 1))
            }
        _editingExercises.value = current + (templateId to updated)
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
        if (plan != null && _isCreatingNewPlan.value && plan.id > 0) {
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
