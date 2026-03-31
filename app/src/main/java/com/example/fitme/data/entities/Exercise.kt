package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "lastmax1") val lastmax1: String? = null,
    @ColumnInfo(name = "lastmax2") val lastmax2: String? = null,
    @ColumnInfo(name = "body_region") val bodyRegion: BodyRegion,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "muscle") val muscle: MuscleGroup? = null
)