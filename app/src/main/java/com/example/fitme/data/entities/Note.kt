package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

//Записи дневника о силовых показателях в определенный день, по сути.
@Entity(tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exercise_id")]

    )
data class Note (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="date") val date: LocalDate,
    @ColumnInfo(name="exercise_id") val exerciseId : Int,
    @ColumnInfo(name="reps") val reps : String?,
    @ColumnInfo(name="weight") val weight : String?,
    @ColumnInfo(name="duration") val duration : String?
    )