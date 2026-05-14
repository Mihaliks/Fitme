package com.example.fitme.data.seed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitme.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class DatabaseSeederTest {

    private lateinit var db: AppDatabase
    private lateinit var seeder: DatabaseSeeder

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seeder = DatabaseSeeder(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun seedIfNeededCreatesDefaultExercisesAndWorkoutPlans() = runBlocking {
        seeder.seedIfNeeded()

        val exercises = db.exerciseDao().getActiveExercises().first()
        val plans = db.workoutPlanDao().getAllActivePlans().first()
        val builtInTemplates = db.workoutPlanDao().getBuiltInWorkoutTemplatesOnce()

        assertEquals(DefaultSeedData.exercises.size, exercises.size)
        assertTrue(exercises.all { it.isBuiltIn })
        assertEquals(
            DefaultSeedData.exercises.map { it.name }.sorted(),
            exercises.map { it.name },
        )
        assertEquals(DefaultSeedData.plans.map { it.name }, plans.map { it.name })
        assertEquals(DefaultSeedData.workoutTemplates.map { it.name }, builtInTemplates.map { it.name })
        assertEquals(DefaultSeedData.workoutTemplates.indices.map { it + 1 }, builtInTemplates.map { it.order })
        assertTrue(builtInTemplates.all { it.isBuiltIn })
        assertTrue(builtInTemplates.all { it.planId == null })

        val fullBodyPlan = plans.first { it.name == "Фулбади для новичка" }
        val fullBodyTemplates = db.workoutPlanDao().getWorkoutTemplatesForPlanOnce(fullBodyPlan.id)
        assertEquals(listOf("Фулбади A", "Фулбади B"), fullBodyTemplates.map { it.name })
        assertEquals(listOf(1, 2), fullBodyTemplates.map { it.order })
        assertEquals(listOf(false, false), fullBodyTemplates.map { it.isBuiltIn })

        val firstTemplateExercises = db.exerciseToDoDao()
            .getExerciseDetailsForWorkoutOnce(fullBodyTemplates.first().id)
        assertEquals(
            listOf("Приседания со штангой", "Жим лежа", "Тяга штанги в наклоне", "Планка"),
            firstTemplateExercises.map { it.exercise.name },
        )
        assertEquals(listOf(1, 2, 3, 4), firstTemplateExercises.map { it.exerciseToDo.order })
    }

    @Test
    fun seedIfNeededDoesNotDuplicateData() = runBlocking {
        seeder.seedIfNeeded()
        seeder.seedIfNeeded()

        val exercises = db.exerciseDao().getActiveExercises().first()
        val plans = db.workoutPlanDao().getAllActivePlans().first()
        val builtInTemplates = db.workoutPlanDao().getBuiltInWorkoutTemplatesOnce()

        assertEquals(DefaultSeedData.exercises.size, exercises.size)
        assertEquals(DefaultSeedData.plans.size, plans.size)
        assertEquals(DefaultSeedData.workoutTemplates.size, builtInTemplates.size)
    }

    @Test
    fun seedIfNeededReusesExistingExerciseByName() = runBlocking {
        val existingId = db.exerciseDao().insertExercise(
            DefaultSeedData.exercises.first().copy(id = 0, isActive = false, isBuiltIn = false)
        ).toInt()

        seeder.seedIfNeeded()

        val saved = db.exerciseDao().getExerciseByName(DefaultSeedData.exercises.first().name)
        assertNotNull(saved)
        assertEquals(existingId, saved!!.id)
        assertEquals(false, saved.isActive)
    }
}
