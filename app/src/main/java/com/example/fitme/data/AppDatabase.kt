package com.example.fitme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitme.data.dao.ExerciseDao
import com.example.fitme.data.dao.ExerciseToDoDao
import com.example.fitme.data.dao.NoteDao
import com.example.fitme.data.dao.UserDao
import com.example.fitme.data.dao.VisitDao
import com.example.fitme.data.dao.WorkoutPlanDao
import com.example.fitme.data.dao.WorkoutSessionDao
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.User
import com.example.fitme.data.entities.Visit
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.converters.Converter


// TODO : Инициализация базы — заполнение данными по умолчанию при создании. Причем такие данные сделать неизменяемыми.
// TODO : Тесты data слоя
// TODO : При необходимости сделать WorkoutRepository, ExerciseRepository и UserRepository. Они будут иметь более сложную бизнес логику и состаять из составных dao вызовов.
// TODO : Миграции — после первых тестов.
// На уровне идей для расширения:
// Hilt - автоматическое внедрение зависимостей.
// Подключение синхронизации с облаком и другими устройствами. Проверка наличия подписки.

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
    version = 2,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseToDoDao(): ExerciseToDoDao
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun visitDao(): VisitDao
    abstract fun workoutSessionDao(): WorkoutSessionDao

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
