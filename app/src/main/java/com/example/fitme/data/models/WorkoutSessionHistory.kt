package com.example.fitme.data.models

import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate

data class WorkoutSessionHistory(
    val workoutSession: WorkoutSession,
    val workoutTemplate: WorkoutTemplate,
    val exercises: List<WorkoutSessionExerciseHistory>,
)

data class WorkoutSessionExerciseHistory(
    val exerciseToDo: ExerciseToDo,
    val exercise: Exercise,
    val notes: List<Note>,
)
