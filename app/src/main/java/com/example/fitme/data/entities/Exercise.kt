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
    // Два самых лучших
    @ColumnInfo(name = "node_1") val node1: Int? = null,
    @ColumnInfo(name = "node_2") val node2: Int? = null,
    @ColumnInfo(name = "body_region") val bodyRegion: BodyRegion,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "muscle") val muscle: MuscleGroup? = null
)