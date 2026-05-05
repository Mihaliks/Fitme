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
import java.time.LocalDate
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
class NoteRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: NoteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NoteRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun newPlan(name: String = "Plan"): Int =
        db.workoutPlanDao().insertPlan(Plan(name = name)).toInt()

    private suspend fun newTemplate(planId: Int, name: String = "A", order: Int = 1): Int =
        db.workoutPlanDao().insertWorkoutTemplate(
            WorkoutTemplate(name = name, planId = planId, order = order)
        ).toInt()

    private suspend fun newExercise(name: String = "Жим"): Int =
        db.exerciseDao().insertExercise(
            Exercise(name = name, bodyRegion = BodyRegion.CHEST)
        ).toInt()

    private suspend fun newExerciseToDo(
        templateId: Int,
        exerciseId: Int,
        order: Int = 1,
        trainingMode: TrainingMode = TrainingMode.HYPERTROPHY,
    ): Int = db.exerciseToDoDao().insertExerciseToDo(
        ExerciseToDo(
            exerciseId = exerciseId,
            workoutTemplateId = templateId,
            sets = 3,
            reps = 8,
            weight = 50.0,
            order = order,
            trainingMode = trainingMode,
        )
    ).toInt()

    private suspend fun newSession(
        templateId: Int,
        date: LocalDate = LocalDate.of(2026, 5, 5),
    ): Int = db.workoutSessionDao().insertWorkoutSession(
        WorkoutSession(workoutTemplateId = templateId, date = date)
    ).toInt()

    private suspend fun fixture(): Fixture {
        val planId = newPlan()
        val templateId = newTemplate(planId)
        val exerciseId = newExercise()
        val todoId = newExerciseToDo(templateId, exerciseId)
        val sessionId = newSession(templateId)
        return Fixture(
            templateId = templateId,
            exerciseId = exerciseId,
            todoId = todoId,
            sessionId = sessionId,
        )
    }

    @Test
    fun appendNoteAssignsIncrementalSetIndex() = runBlocking {
        val fx = fixture()

        repository.appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 8, weight = 50.0)
        repository.appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 9, weight = 50.0)
        repository.appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 7, weight = 52.5)

        val notes = repository.getNotesForExerciseInSession(fx.sessionId, fx.todoId).first()
        assertEquals(listOf(1, 2, 3), notes.map { it.setIndex })
    }

    @Test
    fun appendNoteSetIndexIsScopedToSessionAndExercise() = runBlocking {
        val fx = fixture()
        val secondExerciseId = newExercise(name = "Тяга")
        val secondTodoId = newExerciseToDo(fx.templateId, secondExerciseId, order = 2)
        val secondSessionId = newSession(fx.templateId, date = LocalDate.of(2026, 5, 6))

        repository.appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 8)
        repository.appendNote(fx.sessionId, secondTodoId, TrainingMode.HYPERTROPHY, reps = 10)
        repository.appendNote(secondSessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 9)
        repository.appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 7)

        val firstExerciseFirstSession = repository
            .getNotesForExerciseInSession(fx.sessionId, fx.todoId)
            .first()
        val secondExerciseFirstSession = repository
            .getNotesForExerciseInSession(fx.sessionId, secondTodoId)
            .first()
        val firstExerciseSecondSession = repository
            .getNotesForExerciseInSession(secondSessionId, fx.todoId)
            .first()

        assertEquals(listOf(1, 2), firstExerciseFirstSession.map { it.setIndex })
        assertEquals(listOf(1), secondExerciseFirstSession.map { it.setIndex })
        assertEquals(listOf(1), firstExerciseSecondSession.map { it.setIndex })
    }

    @Test
    fun getLastNotesByModeReturnsLatestSessionOfRequestedMode() = runBlocking {
        val fx = fixture()
        val oldSessionId = newSession(fx.templateId, date = LocalDate.of(2026, 5, 1))
        val newSessionId = newSession(fx.templateId, date = LocalDate.of(2026, 5, 3))

        repository.appendNote(oldSessionId, fx.todoId, TrainingMode.STRENGTH, reps = 5, weight = 100.0)
        repository.appendNote(oldSessionId, fx.todoId, TrainingMode.STRENGTH, reps = 4, weight = 105.0)
        repository.appendNote(newSessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 10, weight = 70.0)
        repository.appendNote(newSessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 9, weight = 72.5)

        val strengthNotes = repository.getLastNotesByMode(fx.todoId, TrainingMode.STRENGTH)
        val hypertrophyNotes = repository.getLastNotesByMode(fx.todoId, TrainingMode.HYPERTROPHY)

        assertEquals(listOf(5, 4), strengthNotes.map { it.reps })
        assertEquals(listOf(10, 9), hypertrophyNotes.map { it.reps })
        assertEquals(listOf(1, 2), hypertrophyNotes.map { it.setIndex })
    }

    @Test
    fun getLastNotesByModeReturnsEmptyWhenNoHistoryForMode() = runBlocking {
        val fx = fixture()

        repository.appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 8)

        assertTrue(repository.getLastNotesByMode(fx.todoId, TrainingMode.STRENGTH).isEmpty())
    }

    @Test
    fun getLastModeForReturnsModeFromLatestSet() = runBlocking {
        val fx = fixture()
        val oldSessionId = newSession(fx.templateId, date = LocalDate.of(2026, 5, 1))
        val newSessionId = newSession(fx.templateId, date = LocalDate.of(2026, 5, 3))

        repository.appendNote(oldSessionId, fx.todoId, TrainingMode.STRENGTH, reps = 5)
        repository.appendNote(newSessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 10)
        repository.appendNote(newSessionId, fx.todoId, TrainingMode.ENDURANCE, reps = 20)

        assertEquals(TrainingMode.ENDURANCE, repository.getLastModeFor(fx.todoId))
    }

    @Test
    fun getNotesForExerciseReturnsNotesAcrossTodoSlotsForSameExercise() = runBlocking {
        val fx = fixture()
        val secondPlanId = newPlan("Second")
        val secondTemplateId = newTemplate(secondPlanId, name = "B")
        val secondTodoId = newExerciseToDo(secondTemplateId, fx.exerciseId)
        val secondSessionId = newSession(secondTemplateId, date = LocalDate.of(2026, 5, 6))

        repository.appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 8)
        repository.appendNote(secondSessionId, secondTodoId, TrainingMode.STRENGTH, reps = 5)

        val notes = repository.getNotesForExercise(fx.exerciseId).first()
        assertEquals(2, notes.size)
        assertEquals(listOf(secondSessionId, fx.sessionId), notes.map { it.workoutSessionId })
    }

    @Test
    fun updateAndDeleteNote() = runBlocking {
        val fx = fixture()
        val noteId = repository
            .appendNote(fx.sessionId, fx.todoId, TrainingMode.HYPERTROPHY, reps = 8, weight = 50.0)
            .toInt()

        val saved = repository.getNoteById(noteId)
        assertNotNull(saved)

        repository.updateNote(saved!!.copy(reps = 9, weight = 52.5))
        val updated = repository.getNoteById(noteId)
        assertEquals(9, updated!!.reps)
        assertEquals(52.5, updated.weight!!, 0.0001)

        repository.deleteNote(updated)
        assertNull(repository.getNoteById(noteId))
    }

    private data class Fixture(
        val templateId: Int,
        val exerciseId: Int,
        val todoId: Int,
        val sessionId: Int,
    )
}
