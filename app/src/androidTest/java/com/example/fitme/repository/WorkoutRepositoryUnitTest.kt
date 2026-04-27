package com.example.fitme.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.repositories.WorkoutRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRepositoryUnitTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: WorkoutRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = WorkoutRepository(
            workoutPlanDao = db.workoutPlanDao(),
            workoutSessionDao = db.workoutSessionDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getWorkoutTemplatesByPlan() = runBlocking {
        val planId: Int =db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
        db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name ="Ноги", planId = planId))
        db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name ="Бицепс спина", planId = planId))
        db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name ="Грудь трицепс", planId = planId))
    }
    @Test(expected = SQLiteConstraintException::class)
    fun getCreatingWorkoutWithoutPlanException() = runBlocking {
        val planId: Int = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
        db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name ="Ноги", planId = planId+1))
    }
}