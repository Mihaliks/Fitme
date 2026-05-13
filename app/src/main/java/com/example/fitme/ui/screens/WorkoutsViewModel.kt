package com.example.fitme.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.repositories.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WorkoutRepository(AppDatabase.getInstance(application))

    val plans: StateFlow<List<Plan>> = repository.getAllPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPlanTemplates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val selectedPlanTemplates: StateFlow<List<WorkoutTemplate>> = _selectedPlanTemplates.asStateFlow()

    fun loadTemplatesForPlan(planId: Int) {
        viewModelScope.launch {
            val result = repository.getWorkoutTemplatesByPlanId(planId)
            _selectedPlanTemplates.value = result?.workoutTemplates ?: emptyList()
        }
    }
}
