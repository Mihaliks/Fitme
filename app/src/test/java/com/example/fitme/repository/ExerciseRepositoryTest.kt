package com.example.fitme.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import com.example.fitme.data.repositories.ExerciseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ExerciseRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ExerciseRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ExerciseRepository(db.exerciseDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun createCustomExerciseInsertsActiveExerciseWithGeneratedId() = runBlocking {
        val id = repository.createCustomExercise(
            exercise(
                id = 99,
                name = "Bench Press",
                isActive = false,
            )
        ).toInt()

        val saved = repository.getExerciseById(id)
        assertNotNull(saved)
        assertEquals(id, saved!!.id)
        assertEquals("Bench Press", saved.name)
        assertTrue(saved.isActive)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCustomExerciseRejectsBlankName() {
        runBlocking {
            repository.createCustomExercise(exercise(name = "   "))
        }
    }

    @Test
    fun createCustomExerciseTrimsName() = runBlocking {
        val id = repository.createCustomExercise(exercise(name = "  Bench Press  ")).toInt()

        val saved = repository.getExerciseById(id)
        assertEquals("Bench Press", saved!!.name)
    }

    @Test
    fun getAllActiveExercisesReturnsOnlyActiveExercises() = runBlocking {
        insertExercise(name = "Bench Press", isActive = true)
        insertExercise(name = "Archived Press", isActive = false)
        insertExercise(name = "Squat", bodyRegion = BodyRegion.LEGS, isActive = true)

        val exercises = repository.getAllActiveExercises().first()

        assertEquals(listOf("Bench Press", "Squat"), exercises.map { it.name })
    }

    @Test
    fun getAllInactiveExercisesReturnsOnlyInactiveExercises() = runBlocking {
        insertExercise(name = "Bench Press", isActive = true)
        insertExercise(name = "Archived Press", isActive = false)
        insertExercise(name = "Archived Squat", bodyRegion = BodyRegion.LEGS, isActive = false)

        val exercises = repository.getAllInactiveExercises().first()

        assertEquals(listOf("Archived Press", "Archived Squat"), exercises.map { it.name })
    }

    @Test
    fun filtersReturnOnlyActiveExercises() = runBlocking {
        insertExercise(
            name = "Bench Press",
            bodyRegion = BodyRegion.CHEST,
            muscle = MuscleGroup.MIDDLE_CHEST,
            isActive = true,
        )
        insertExercise(
            name = "Archived Chest Press",
            bodyRegion = BodyRegion.CHEST,
            muscle = MuscleGroup.MIDDLE_CHEST,
            isActive = false,
        )
        insertExercise(
            name = "Pull Up",
            bodyRegion = BodyRegion.BACK,
            muscle = MuscleGroup.LATS,
            isActive = true,
        )

        val byBodyRegion = repository
            .getAllExercisesByBodyRegion(BodyRegion.CHEST)
            .first()
        val byMuscle = repository
            .getAllExercisesByMuscleGroup(MuscleGroup.MIDDLE_CHEST)
            .first()
        val byBoth = repository
            .getAllExercisesByBodyRegionAndMuscleGroup(
                BodyRegion.CHEST,
                MuscleGroup.MIDDLE_CHEST,
            )
            .first()

        assertEquals(listOf("Bench Press"), byBodyRegion.map { it.name })
        assertEquals(listOf("Bench Press"), byMuscle.map { it.name })
        assertEquals(listOf("Bench Press"), byBoth.map { it.name })
    }

    @Test
    fun searchActiveExercisesTrimsQueryAndReturnsActiveMatches() = runBlocking {
        insertExercise(name = "Bench Press", isActive = true)
        insertExercise(name = "Incline Bench Press", isActive = true)
        insertExercise(name = "Archived Bench", isActive = false)
        insertExercise(name = "Squat", bodyRegion = BodyRegion.LEGS, isActive = true)

        val exercises = repository.searchActiveExercises("  Bench  ").first()

        assertEquals(listOf("Bench Press", "Incline Bench Press"), exercises.map { it.name })
    }

    @Test
    fun searchActiveExercisesWithBlankQueryReturnsAllActiveExercises() = runBlocking {
        insertExercise(name = "Bench Press", isActive = true)
        insertExercise(name = "Archived Bench", isActive = false)
        insertExercise(name = "Squat", bodyRegion = BodyRegion.LEGS, isActive = true)

        val exercises = repository.searchActiveExercises("   ").first()

        assertEquals(listOf("Bench Press", "Squat"), exercises.map { it.name })
    }

    @Test
    fun updateCustomExercisePersistsChanges() = runBlocking {
        val id = repository.createCustomExercise(
            exercise(name = "Bench Press", muscle = MuscleGroup.MIDDLE_CHEST)
        ).toInt()
        val saved = repository.getExerciseById(id)!!

        repository.updateCustomExercise(
            saved.copy(name = "Barbell Bench Press", muscle = MuscleGroup.UPPER_CHEST)
        )

        val updated = repository.getExerciseById(id)
        assertEquals("Barbell Bench Press", updated!!.name)
        assertEquals(MuscleGroup.UPPER_CHEST, updated.muscle)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateCustomExerciseRejectsBlankName() = runBlocking {
        val id = repository.createCustomExercise(exercise(name = "Bench Press")).toInt()
        val saved = repository.getExerciseById(id)!!

        repository.updateCustomExercise(saved.copy(name = " "))
    }

    @Test
    fun archiveCustomExerciseDeactivatesExercise() = runBlocking {
        val id = repository.createCustomExercise(exercise(name = "Bench Press")).toInt()
        val saved = repository.getExerciseById(id)!!

        repository.archiveCustomExercise(saved)

        val updated = repository.getExerciseById(id)
        assertNotNull(updated)
        assertEquals(false, updated!!.isActive)
        assertEquals(emptyList<Exercise>(), repository.getAllActiveExercises().first())
        assertEquals(listOf("Bench Press"), repository.getAllInactiveExercises().first().map { it.name })
    }

    @Test
    fun getExerciseByIdReturnsNullForMissingExercise() = runBlocking {
        assertNull(repository.getExerciseById(999))
    }

    private suspend fun insertExercise(
        name: String,
        bodyRegion: BodyRegion = BodyRegion.CHEST,
        muscle: MuscleGroup? = MuscleGroup.MIDDLE_CHEST,
        isActive: Boolean = true,
    ): Int = db.exerciseDao().insertExercise(
        exercise(
            name = name,
            bodyRegion = bodyRegion,
            muscle = muscle,
            isActive = isActive,
        )
    ).toInt()

    private fun exercise(
        id: Int = 0,
        name: String,
        bodyRegion: BodyRegion = BodyRegion.CHEST,
        muscle: MuscleGroup? = MuscleGroup.MIDDLE_CHEST,
        isActive: Boolean = true,
    ): Exercise = Exercise(
        id = id,
        name = name,
        bodyRegion = bodyRegion,
        muscle = muscle,
        isActive = isActive,
    )
}
