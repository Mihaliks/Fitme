package com.example.fitme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.User
import com.example.fitme.data.entities.Visit
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.converters.Converter

// TODO : Заполнить полный список сущностей и DAO, а также добавить миграции при необходимости
@Database(
    entities = [
        Exercise::class,
        ExerciseToDo::class,
        Note::class,
        Plan::class,
        User::class,
        Visit::class,
        WorkoutSession::class,
        WorkoutTemplate::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    // TODO: Вот тут накидать все DAO
    // abstract fun planDao(): PlanDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "fitme_database"
                            ).fallbackToDestructiveMigration(true) //версия бд не совпадает -> пересборка с нуля
                    .build().also { instance = it }
            }
    }
}
