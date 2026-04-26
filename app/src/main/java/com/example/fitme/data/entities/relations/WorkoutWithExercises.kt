package com.example.fitme.data.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.WorkoutTemplate

data class WorkoutWithExercises(
    @Embedded val workoutTemplate: WorkoutTemplate,
    @Relation(
        entity = ExerciseToDo::class,
        parentColumn = "id",
        entityColumn = "workout_template_id"
    )
    val exercises: List<ExerciseWithDetails>
)

