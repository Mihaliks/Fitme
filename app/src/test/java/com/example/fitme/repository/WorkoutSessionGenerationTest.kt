package com.example.fitme.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.repositories.NoteRepository
import com.example.fitme.data.repositories.WorkoutRepository
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
class WorkoutSessionGenerationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorkoutRepository
    private lateinit var noteRepository: NoteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkoutRepository(db)
        noteRepository = NoteRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun newPlan(name: String = "Plan"): Int =
        db.workoutPlanDao().insertPlan(Plan(name = name)).toInt()

    private suspend fun newTemplate(planId: Int, name: String, order: Int): Int =
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
        sets: Int = 3,
        reps: Int = 8,
        weight: Double? = 50.0,
        trainingMode: TrainingMode = TrainingMode.HYPERTROPHY,
        periodizationEnabled: Boolean = false,
        modeA: TrainingMode? = null,
        modeB: TrainingMode? = null,
        setsA: Int? = null, repsA: Int? = null, weightA: Double? = null,
        setsB: Int? = null, repsB: Int? = null, weightB: Double? = null,
    ): Int = db.exerciseToDoDao().insertExerciseToDo(
        ExerciseToDo(
            exerciseId = exerciseId,
            workoutTemplateId = templateId,
            sets = sets,
            reps = reps,
            weight = weight,
            order = order,
            trainingMode = trainingMode,
            periodizationEnabled = periodizationEnabled,
            modeA = modeA, modeB = modeB,
            setsA = setsA, repsA = repsA, weightA = weightA,
            setsB = setsB, repsB = repsB, weightB = weightB,
        )
    ).toInt()


    @Test
    fun returnsNullForEmptyPlan() = runBlocking {
        val planId = newPlan()
        assertNull(repository.createNextWorkoutSession(planId))
    }

    @Test
    fun createWorkoutSessionFromTemplateUsesRequestedTemplate() = runBlocking {
        val planId = newPlan()
        val firstTemplateId = newTemplate(planId, "A", order = 1)
        val secondTemplateId = newTemplate(planId, "B", order = 2)
        val exerciseId = newExercise("Жим")
        newExerciseToDo(secondTemplateId, exerciseId)

        val session = repository.createWorkoutSessionFromTemplate(secondTemplateId)

        assertNotNull(session)
        assertEquals(secondTemplateId, session!!.template.id)
        assertEquals(listOf(exerciseId), session.exercises.map { it.exercise.id })
        assertEquals(
            secondTemplateId,
            db.workoutSessionDao().getWorkoutSessionById(session.sessionId)!!.workoutTemplateId,
        )
        assertEquals(firstTemplateId, repository.peekNextWorkoutSession(planId)!!.template.id)
    }

    @Test
    fun createWorkoutSessionFromTemplateReturnsNullForMissingTemplate() = runBlocking {
        assertNull(repository.createWorkoutSessionFromTemplate(workoutTemplateId = 999))
    }

    @Test
    fun picksFirstTemplateWhenNoSessionsYet() = runBlocking {
        val planId = newPlan()
        val firstId = newTemplate(planId, "A", order = 1)
        newTemplate(planId, "B", order = 2)
        newTemplate(planId, "C", order = 3)

        val next = repository.createNextWorkoutSession(planId)
        assertNotNull(next)
        assertEquals(firstId, next!!.template.id)
    }

    @Test
    fun picksNextByOrderAfterLastSession() = runBlocking {
        val planId = newPlan()
        newTemplate(planId, "A", order = 1)
        val secondId = newTemplate(planId, "B", order = 2)
        newTemplate(planId, "C", order = 3)

        val first = repository.createNextWorkoutSession(planId)!!
        assertEquals(1, first.template.order)

        val second = repository.createNextWorkoutSession(planId)!!
        assertEquals(secondId, second.template.id)
        assertEquals(2, second.template.order)
    }

    @Test
    fun wrapsAroundAfterLastTemplate() = runBlocking {
        val planId = newPlan()
        val firstId = newTemplate(planId, "A", order = 1)
        newTemplate(planId, "B", order = 2)
        newTemplate(planId, "C", order = 3)

        repeat(3) { repository.createNextWorkoutSession(planId) }     // 1, 2, 3
        val wrap = repository.createNextWorkoutSession(planId)!!      // → обратно на 1

        assertEquals(firstId, wrap.template.id)
        assertEquals(1, wrap.template.order)
    }

    // тесты что не меняется режим периодизации если не включать параметр
    @Test
    fun usesBaseTrainingModeWhenPeriodizationOff() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        newExerciseToDo(templateId, exerciseId, trainingMode = TrainingMode.STRENGTH)

        val plan = repository.createNextWorkoutSession(planId)!!
        assertEquals(TrainingMode.STRENGTH, plan.exercises.single().chosenMode)
    }

    @Test
    fun plannedParamsAreBaseWhenPeriodizationOff() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        newExerciseToDo(
            templateId, exerciseId,
            sets = 4, reps = 10, weight = 60.0,
        )

        val plan = repository.createNextWorkoutSession(planId)!!
        val ex = plan.exercises.single()
        assertEquals(4, ex.plannedSets)
        assertEquals(10, ex.plannedReps)
        assertEquals(60.0, ex.plannedWeight!!, 0.0001)
    }

    //периодизация

    @Test
    fun periodizationStartsWithModeAWhenNoHistory() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        newExerciseToDo(
            templateId, exerciseId,
            periodizationEnabled = true,
            modeA = TrainingMode.STRENGTH,
            modeB = TrainingMode.HYPERTROPHY,
        )

        val plan = repository.createNextWorkoutSession(planId)!!
        assertEquals(TrainingMode.STRENGTH, plan.exercises.single().chosenMode)
    }

    @Test
    fun periodizationAlternatesBetweenAandB() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        val todoId = newExerciseToDo(
            templateId, exerciseId,
            periodizationEnabled = true,
            modeA = TrainingMode.STRENGTH,
            modeB = TrainingMode.HYPERTROPHY,
        )

        val s1 = repository.createNextWorkoutSession(planId)!!
        assertEquals(TrainingMode.STRENGTH, s1.exercises.single().chosenMode)
        noteRepository.appendNote(s1.sessionId, todoId, TrainingMode.STRENGTH, reps = 5, weight = 100.0)

        val s2 = repository.createNextWorkoutSession(planId)!!
        assertEquals(TrainingMode.HYPERTROPHY, s2.exercises.single().chosenMode)
        noteRepository.appendNote(s2.sessionId, todoId, TrainingMode.HYPERTROPHY, reps = 10, weight = 70.0)

        val s3 = repository.createNextWorkoutSession(planId)!!
        assertEquals(TrainingMode.STRENGTH, s3.exercises.single().chosenMode)
    }

    @Test
    fun plannedParamsForModeAandModeB() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        val todoId = newExerciseToDo(
            templateId, exerciseId,
            periodizationEnabled = true,
            modeA = TrainingMode.STRENGTH,
            modeB = TrainingMode.ENDURANCE,
            setsA = 5, repsA = 5, weightA = 120.0,
            setsB = 3, repsB = 20, weightB = 40.0,
        )

        val s1 = repository.createNextWorkoutSession(planId)!!
        val ex1 = s1.exercises.single()
        assertEquals(TrainingMode.STRENGTH, ex1.chosenMode)
        assertEquals(5, ex1.plannedSets)
        assertEquals(5, ex1.plannedReps)
        assertEquals(120.0, ex1.plannedWeight!!, 0.0001)
        noteRepository.appendNote(s1.sessionId, todoId, TrainingMode.STRENGTH, reps = 5, weight = 120.0)

        val s2 = repository.createNextWorkoutSession(planId)!!
        val ex2 = s2.exercises.single()
        assertEquals(TrainingMode.ENDURANCE, ex2.chosenMode)
        assertEquals(3, ex2.plannedSets)
        assertEquals(20, ex2.plannedReps)
        assertEquals(40.0, ex2.plannedWeight!!, 0.0001)
    }


    @Test
    fun prefillIsEmptyWhenNoHistory() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        newExerciseToDo(templateId, exerciseId, trainingMode = TrainingMode.HYPERTROPHY)

        val plan = repository.createNextWorkoutSession(planId)!!
        assertTrue(plan.exercises.single().prefillNotes.isEmpty())
    }

    @Test
    fun prefillTakesNotesFromPreviousSessionOfSameMode() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        val todoId = newExerciseToDo(
            templateId, exerciseId,
            periodizationEnabled = true,
            modeA = TrainingMode.STRENGTH,
            modeB = TrainingMode.HYPERTROPHY,
        )

        val s1 = repository.createNextWorkoutSession(planId)!!
        repeat(3) {
            noteRepository.appendNote(s1.sessionId, todoId, TrainingMode.STRENGTH, reps = 5, weight = 100.0)
        }

        val s2 = repository.createNextWorkoutSession(planId)!!
        repeat(3) {
            noteRepository.appendNote(s2.sessionId, todoId, TrainingMode.HYPERTROPHY, reps = 10, weight = 70.0)
        }

        // снова STRENGTH; prefill должен показать 3 сета по 5x100, не гипертрофию
        val s3 = repository.createNextWorkoutSession(planId)!!
        val ex3 = s3.exercises.single()
        assertEquals(TrainingMode.STRENGTH, ex3.chosenMode)
        assertEquals(3, ex3.prefillNotes.size)
        ex3.prefillNotes.forEach {
            assertEquals(5, it.reps)
            assertEquals(100.0, it.weight!!, 0.0001)
            assertEquals(TrainingMode.STRENGTH, it.modeUsed)
        }
        assertEquals(listOf(1, 2, 3), ex3.prefillNotes.map { it.setIndex })
    }

    // автоинкремент set_index
    @Test
    fun appendNoteAssignsIncrementalSetIndex() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId = newExercise()
        val todoId = newExerciseToDo(templateId, exerciseId)

        val s = repository.createNextWorkoutSession(planId)!!

        noteRepository.appendNote(s.sessionId, todoId, TrainingMode.HYPERTROPHY, reps = 8, weight = 50.0)
        noteRepository.appendNote(s.sessionId, todoId, TrainingMode.HYPERTROPHY, reps = 9, weight = 50.0)
        noteRepository.appendNote(s.sessionId, todoId, TrainingMode.HYPERTROPHY, reps = 7, weight = 52.5)

        val notes = noteRepository.getLastNotesByMode(todoId, TrainingMode.HYPERTROPHY)
        assertEquals(listOf(1, 2, 3), notes.map { it.setIndex })
    }

    @Test
    fun appendNoteSetIndexIsScopedToSessionAndExercise() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "A", order = 1)
        val exerciseId1 = newExercise(name = "Жим")
        val exerciseId2 = newExercise(name = "Тяга")
        val todoId1 = newExerciseToDo(templateId, exerciseId1, order = 1)
        val todoId2 = newExerciseToDo(templateId, exerciseId2, order = 2)

        val s = repository.createNextWorkoutSession(planId)!!

        // вперемешку: жим, тяга, жим, тяга, жим — у каждого свой счётчик
        noteRepository.appendNote(s.sessionId, todoId1, TrainingMode.HYPERTROPHY, reps = 8, weight = 50.0)
        noteRepository.appendNote(s.sessionId, todoId2, TrainingMode.HYPERTROPHY, reps = 8, weight = 60.0)
        noteRepository.appendNote(s.sessionId, todoId1, TrainingMode.HYPERTROPHY, reps = 8, weight = 50.0)
        noteRepository.appendNote(s.sessionId, todoId2, TrainingMode.HYPERTROPHY, reps = 8, weight = 60.0)
        noteRepository.appendNote(s.sessionId, todoId1, TrainingMode.HYPERTROPHY, reps = 7, weight = 50.0)

        val notesEx1 = noteRepository.getLastNotesByMode(todoId1, TrainingMode.HYPERTROPHY)
        val notesEx2 = noteRepository.getLastNotesByMode(todoId2, TrainingMode.HYPERTROPHY)
        assertEquals(listOf(1, 2, 3), notesEx1.map { it.setIndex })
        assertEquals(listOf(1, 2), notesEx2.map { it.setIndex })
    }

    @Test
    fun peekReturnsNullForEmptyPlan() = runBlocking {
        val planId = newPlan()
        assertNull(repository.peekNextWorkoutSession(planId))
    }

    @Test
    fun peekReturnsFirstTemplateWhenNoSessionsYet() = runBlocking {
        val planId = newPlan()
        val firstId = newTemplate(planId, "Ноги", order = 1)
        newTemplate(planId, "Верх", order = 2)

        val preview = repository.peekNextWorkoutSession(planId)!!
        assertEquals(firstId, preview.template.id)
        assertEquals("Ноги", preview.template.name)
    }

    @Test
    fun peekDoesNotCreateSession() = runBlocking {
        val planId = newPlan()
        newTemplate(planId, "Ноги", order = 1)
        repository.peekNextWorkoutSession(planId)
        val any = db.workoutSessionDao().getLastSessionForPlan(planId)
        assertNull(any)
    }

    @Test
    fun peekReturnsSameTemplateUntilSessionIsCreated() = runBlocking {
        val planId = newPlan()
        val firstId = newTemplate(planId, "Ноги", order = 1)
        newTemplate(planId, "Верх", order = 2)

        val a = repository.peekNextWorkoutSession(planId)!!
        val b = repository.peekNextWorkoutSession(planId)!!
        assertEquals(firstId, a.template.id)
        assertEquals(firstId, b.template.id)
    }

    @Test
    fun peekAdvancesAfterCreateNextWorkoutSession() = runBlocking {
        val planId = newPlan()
        val firstId = newTemplate(planId, "Ноги", order = 1)
        val secondId = newTemplate(planId, "Верх", order = 2)

        val before = repository.peekNextWorkoutSession(planId)!!
        assertEquals(firstId, before.template.id)

        repository.createNextWorkoutSession(planId)

        val after = repository.peekNextWorkoutSession(planId)!!
        assertEquals(secondId, after.template.id)
    }

    @Test
    fun peekIncludesExercisesAndPlannedParams() = runBlocking {
        val planId = newPlan()
        val templateId = newTemplate(planId, "Ноги", order = 1)
        val exerciseId = newExercise(name = "Присед")
        newExerciseToDo(templateId, exerciseId, sets = 4, reps = 6, weight = 100.0)

        val preview = repository.peekNextWorkoutSession(planId)!!
        val ex = preview.exercises.single()
        assertNotNull(ex)
        assertEquals("Присед", ex.exercise.name)
        assertEquals(4, ex.plannedSets)
        assertEquals(6, ex.plannedReps)
        assertEquals(100.0, ex.plannedWeight!!, 0.0001)
    }
}
