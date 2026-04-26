package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate

@Entity(
    tableName = "visits"
)

data class Visit(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean
)