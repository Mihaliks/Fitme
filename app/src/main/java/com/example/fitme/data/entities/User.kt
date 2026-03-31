package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["active_plan"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("active_plan")]
)

data class User(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "age") val age: Int,
    //only kg
    @ColumnInfo(name = "weight") val weight: Float,
    //only cm
    @ColumnInfo(name = "height") val height: Float,
    @ColumnInfo(name = "active_plan") val activePlan: Int? = null
)