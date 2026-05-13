package com.example.fitme.data.models

import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate

data class PlanWorkoutTemplates(
    val plan: Plan,
    val workoutTemplates: List<WorkoutTemplate>,
)
