package com.example.fitme.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.relations.PlanWithWorkouts
import com.example.fitme.data.repositories.WorkoutRepository
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
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
    fun getWorkoutTemplatesByPlan() {
        runBlocking {
            val planId: Int = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
            db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name = "Ноги", planId = planId,order=1))
            db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name = "Бицепс спина", planId = planId,order=2))
            db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name = "Грудь трицепс", planId = planId,order=3))
            val plan: PlanWithWorkouts? = repository.getWorkoutTemplatesByPlanId(planId)
            assert(plan != null)
            assertEquals(plan!!.workoutTemplates.size,3)
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun getCreatingWorkoutWithoutPlanException() {
        runBlocking {
            val planId: Int = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
            db.workoutPlanDao().insertWorkoutTemplate(WorkoutTemplate(name = "Ноги", planId = planId + 1, order = 1))
        }
    }
}
