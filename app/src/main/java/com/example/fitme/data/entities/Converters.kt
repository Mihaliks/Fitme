package com.example.fitme.data.entities

import androidx.room.TypeConverter
import java.util.Date


//Это специальный класс для автоконвертации форматов Date - Long
class Converters {
    @TypeConverter
    fun fromDate(date: Date): Long = date.time

    @TypeConverter
    fun toDate(timestamp: Long): Date = Date(timestamp)
}