package com.example.fitme.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.repositories.NoteRepository
import com.example.fitme.data.repositories.SessionRepository
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SessionRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: SessionRepository
    private lateinit var noteRepository: NoteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SessionRepository(db)
        noteRepository = NoteRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getWorkoutSessionHistoryReturnsExercisesWithActualNotes() = runBlocking {
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "Plan")).toInt()
        val templateId = db.workoutPlanDao().insertWorkoutTemplate(
            WorkoutTemplate(name = "Upper", planId = planId, order = 1)
        ).toInt()
        val benchId = newExercise("Жим")
        val rowId = newExercise("Тяга")
        val benchTodoId = newExerciseToDo(templateId, benchId, order = 1)
        val rowTodoId = newExerciseToDo(templateId, rowId, order = 2)
        val targetSessionId = newSession(templateId, LocalDate.of(2026, 5, 5))
        val otherSessionId = newSession(templateId, LocalDate.of(2026, 5, 6))

        noteRepository.appendNote(
            targetSessionId,
            benchTodoId,
            TrainingMode.HYPERTROPHY,
            reps = 8,
            weight = 50.0,
        )
        noteRepository.appendNote(
            targetSessionId,
            benchTodoId,
            TrainingMode.HYPERTROPHY,
            reps = 7,
            weight = 52.5,
        )
        noteRepository.appendNote(
            targetSessionId,
            rowTodoId,
            TrainingMode.STRENGTH,
            reps = 5,
            weight = 80.0,
        )
        noteRepository.appendNote(
            otherSessionId,
            benchTodoId,
            TrainingMode.HYPERTROPHY,
            reps = 12,
            weight = 40.0,
        )

        val history = repository.getWorkoutSessionHistory(targetSessionId)

        assertNotNull(history)
        assertEquals(targetSessionId, history!!.workoutSession.id)
        assertEquals("Upper", history.workoutTemplate.name)
        assertEquals(listOf("Жим", "Тяга"), history.exercises.map { it.exercise.name })

        val benchNotes = history.exercises[0].notes
        val rowNotes = history.exercises[1].notes
        assertEquals(listOf(1, 2), benchNotes.map { it.setIndex })
        assertEquals(listOf(8, 7), benchNotes.map { it.reps })
        assertEquals(listOf(1), rowNotes.map { it.setIndex })
        assertEquals(listOf(5), rowNotes.map { it.reps })
        assertEquals(listOf(targetSessionId, targetSessionId), benchNotes.map { it.workoutSessionId })
    }

    @Test
    fun getWorkoutSessionHistoryReturnsNullForMissingSession() = runBlocking {
        assertNull(repository.getWorkoutSessionHistory(workoutSessionId = 999))
    }

    private suspend fun newExercise(name: String): Int =
        db.exerciseDao().insertExercise(
            Exercise(name = name, bodyRegion = BodyRegion.CHEST)
        ).toInt()

    private suspend fun newExerciseToDo(
        templateId: Int,
        exerciseId: Int,
        order: Int,
    ): Int = db.exerciseToDoDao().insertExerciseToDo(
        ExerciseToDo(
            exerciseId = exerciseId,
            workoutTemplateId = templateId,
            sets = 3,
            reps = 8,
            weight = 50.0,
            order = order,
            trainingMode = TrainingMode.HYPERTROPHY,
        )
    ).toInt()

    private suspend fun newSession(templateId: Int, date: LocalDate): Int =
        db.workoutSessionDao().insertWorkoutSession(
            WorkoutSession(workoutTemplateId = templateId, date = date)
        ).toInt()
}
