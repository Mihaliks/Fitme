package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitme.data.entities.enums.TrainingMode

// Один сет, фактически выполненный пользователем.
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseToDo::class,
            parentColumns = ["id"],
            childColumns = ["exercise_to_do_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["workout_session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("exercise_to_do_id"),
        Index("workout_session_id"),
        Index(value = ["workout_session_id", "exercise_to_do_id"])
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_to_do_id") val exerciseToDoId: Int,
    @ColumnInfo(name = "workout_session_id") val workoutSessionId: Int,
    @ColumnInfo(name = "set_index") val setIndex: Int,
    @ColumnInfo(name = "mode_used") val modeUsed: TrainingMode,
    val reps: Int? = null,
    val weight: Double? = null,
    val duration: Int? = null,
)
