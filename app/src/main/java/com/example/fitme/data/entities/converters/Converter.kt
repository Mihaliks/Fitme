package com.example.fitme.data.entities.converters

import androidx.room.TypeConverter
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import java.time.LocalDate

class Converter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long =
        date.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate =
        LocalDate.ofEpochDay(value)

    @TypeConverter
    fun fromBodyRegion(value: BodyRegion): String = value.name

    @TypeConverter
    fun toBodyRegion(value: String): BodyRegion = BodyRegion.valueOf(value)

    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup?): String? = value?.name

    @TypeConverter
    fun toMuscleGroup(value: String?): MuscleGroup? = value?.let(MuscleGroup::valueOf)
}