package com.example.fitme.data.models

import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.TrainingMode

data class NextWorkoutPreview(
    val template: WorkoutTemplate,
    val exercises: List<NextExercisePlan>,
)

data class NextWorkoutPlan(
    val sessionId: Int,
    val template: WorkoutTemplate,
    val exercises: List<NextExercisePlan>,
)

data class NextExercisePlan(
    val exerciseToDo: ExerciseToDo,
    val exercise: Exercise,
    val chosenMode: TrainingMode,
    val plannedSets: Int,
    val plannedReps: Int,
    val plannedWeight: Double?,
    val prefillNotes: List<Note>,
)
