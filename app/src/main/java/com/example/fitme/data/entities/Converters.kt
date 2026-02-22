package com.example.fitme.data.entities

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long =
        date.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate =
        LocalDate.ofEpochDay(value)
}