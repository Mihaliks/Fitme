package com.example.fitme.data.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate

data class WorkoutSessionWithExercises(
    @Embedded val workoutSession: WorkoutSession,
    @Relation(
        entity = WorkoutTemplate::class,
        parentColumn = "workout_template_id",
        entityColumn = "id"
    )
    val workoutTemplate: WorkoutTemplate,
    @Relation(
        entity = ExerciseToDo::class,
        parentColumn = "workout_template_id",
        entityColumn = "workout_template_id"
    )
    val exercises: List<ExerciseWithDetails>
)

