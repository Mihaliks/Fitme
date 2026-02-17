package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity
@TypeConverters(Converters::class)
data class Note (
    @PrimaryKey val id: Int,
    @ColumnInfo(name="date") val date: Date,
    @ColumnInfo(name="max1") val max1 : String,
    @ColumnInfo(name="max2") val max2 : String
    )