package com.example.fitme.data.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate

data class PlanWithWorkouts(
    @Embedded val plan: Plan,
    @Relation(
        parentColumn = "id",
        entityColumn = "plan_id"
    )
    val workoutTemplates: List<WorkoutTemplate>
)

