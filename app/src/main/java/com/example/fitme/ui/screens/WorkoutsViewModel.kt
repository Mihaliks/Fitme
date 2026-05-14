package com.example.fitme.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.relations.ExerciseWithDetails
import com.example.fitme.data.repositories.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val workoutRepository = WorkoutRepository(db)

    private val _plans = workoutRepository.getAllPlans()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredPlans: StateFlow<List<Plan>> = combine(_plans, _searchQuery) { plans, query ->
        if (query.isBlank()) {
            plans
        } else {
            plans.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedPlanTemplates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val selectedPlanTemplates: StateFlow<List<WorkoutTemplate>> = _selectedPlanTemplates.asStateFlow()

    private val _templateExercises = MutableStateFlow<Map<Int, List<ExerciseWithDetails>>>(emptyMap())
    val templateExercises: StateFlow<Map<Int, List<ExerciseWithDetails>>> = _templateExercises.asStateFlow()

    // Lvl 2: Muscles
    private val _selectedRegion = MutableStateFlow<BodyRegion?>(null)
    val selectedRegion: StateFlow<BodyRegion?> = _selectedRegion.asStateFlow()

    val templatesByRegion: StateFlow<List<Pair<WorkoutTemplate, List<ExerciseWithDetails>>>> = combine(
        _plans,
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
                        // Для "Все тело" показываем тренировки, у которых в названии или в названии плана
                        // есть "фулбади" или "full body", либо есть упражнения с типом FULL_BODY
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
}
