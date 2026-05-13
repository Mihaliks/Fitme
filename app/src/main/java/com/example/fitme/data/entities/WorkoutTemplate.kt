package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_templates",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["plan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plan_id")]
)
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val order: Int,
    @ColumnInfo(name = "plan_id") val planId: Int
)
