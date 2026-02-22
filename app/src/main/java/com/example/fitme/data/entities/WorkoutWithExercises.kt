package com.example.fitme.data.entities

import ExerciseWithDetails
import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        entity = ExerciseToDo::class,
        parentColumn = "id",
        entityColumn = "workout_id"
    )
    val exercises: List<ExerciseWithDetails>
)

