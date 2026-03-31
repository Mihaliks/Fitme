package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["workout_template_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workout_template_id")]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "workout_template_id") val workoutTemplateId: Int,
    val date: LocalDate,
    @ColumnInfo(name = "total_duration") val totalDuration: Int? = null
)
