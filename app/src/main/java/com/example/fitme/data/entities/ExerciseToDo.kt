package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises_todo",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["workout_template_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workout_template_id"), Index("exercise_id")]
)
data class ExerciseToDo(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "workout_template_id") val workoutTemplateId: Int,
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,
    val duration: Int? = null,
    val order: Int
)