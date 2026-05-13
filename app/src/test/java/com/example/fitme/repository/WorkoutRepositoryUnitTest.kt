package com.example.fitme.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
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
import com.example.fitme.data.models.PlanWorkoutTemplates
import com.example.fitme.data.repositories.WorkoutRepository
import org.junit.Assert.assertEquals
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

        repository = WorkoutRepository(db)
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
            val plan: PlanWorkoutTemplates? = repository.getWorkoutTemplatesByPlanId(planId)
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

    @Test
    fun appendWorkoutTemplate() {
        runBlocking {
            val planId: Int = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
            repository.appendWorkoutTemplate("Ноги", planId)
            repository.appendWorkoutTemplate("Бицепс спина", planId)
            repository.appendWorkoutTemplate("Грудь трицепс", planId)
            val plan: PlanWorkoutTemplates? = repository.getWorkoutTemplatesByPlanId(planId)
            assert(plan != null)
            assertEquals(plan!!.workoutTemplates.size,3)
            assertEquals(plan.workoutTemplates[0].order,1)
            assertEquals(plan.workoutTemplates[1].order,2)
            assertEquals(plan.workoutTemplates[2].order,3)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun createNewPlanRejectsBlankName() {
        runBlocking {
            repository.createNewPlan("   ")
        }
    }

    @Test
    fun createNewPlanTrimsName() = runBlocking {
        val planId = repository.createNewPlan("  My plan  ").toInt()

        val plan = db.workoutPlanDao().getPlanById(planId)
        assertEquals("My plan", plan!!.name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updatePlanRejectsBlankName() = runBlocking {
        val planId = repository.createNewPlan("My plan").toInt()
        val plan = db.workoutPlanDao().getPlanById(planId)!!

        repository.updatePlan(plan.copy(name = " "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun appendWorkoutTemplateRejectsBlankName() {
        runBlocking {
            val planId = repository.createNewPlan("My plan").toInt()

            repository.appendWorkoutTemplate(" ", planId)
        }
    }

    @Test
    fun reorderWorkoutTemplates() {
        runBlocking {
            val planId: Int = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
            val id1 = repository.appendWorkoutTemplate("Ноги", planId).toInt()
            val id2 = repository.appendWorkoutTemplate("Бицепс спина", planId).toInt()
            val id3 = repository.appendWorkoutTemplate("Грудь трицепс", planId).toInt()
            repository.reorderWorkoutTemplates(planId, listOf(id3, id1, id2))
            val plan: PlanWorkoutTemplates? = repository.getWorkoutTemplatesByPlanId(planId)
            assert(plan != null)
            assertEquals(plan!!.workoutTemplates.size,3)
            assertEquals(listOf(id3, id1, id2), plan.workoutTemplates.map { it.id })
            assertEquals(listOf(1, 2, 3), plan.workoutTemplates.map { it.order })
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun reorderWorkoutTemplatesRejectsDuplicateIds() = runBlocking {
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
        val id1 = repository.appendWorkoutTemplate("Ноги", planId).toInt()
        repository.appendWorkoutTemplate("Верх", planId)

        repository.reorderWorkoutTemplates(planId, listOf(id1, id1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun reorderWorkoutTemplatesRejectsIdsFromOtherPlan() = runBlocking {
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
        val otherPlanId = db.workoutPlanDao().insertPlan(Plan(name = "Other plan")).toInt()
        val id1 = repository.appendWorkoutTemplate("Ноги", planId).toInt()
        val otherId = repository.appendWorkoutTemplate("Верх", otherPlanId).toInt()

        repository.reorderWorkoutTemplates(planId, listOf(id1, otherId))
    }

    @Test(expected = IllegalArgumentException::class)
    fun reorderWorkoutTemplatesRejectsMissingIds() = runBlocking {
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
        val id1 = repository.appendWorkoutTemplate("Ноги", planId).toInt()
        repository.appendWorkoutTemplate("Верх", planId)

        repository.reorderWorkoutTemplates(planId, listOf(id1))
    }

    @Test
    fun removeWorkoutTemplateForPlanDeletesTemplateAndCompactsOrder() = runBlocking {
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "My first plan")).toInt()
        val id1 = repository.appendWorkoutTemplate("Ноги", planId).toInt()
        val id2 = repository.appendWorkoutTemplate("Верх", planId).toInt()
        val id3 = repository.appendWorkoutTemplate("Фулбади", planId).toInt()
        val secondTemplate = db.workoutPlanDao().getWorkoutTemplateById(id2)!!

        repository.removeWorkoutTemplateForPlan(secondTemplate)

        val plan = repository.getWorkoutTemplatesByPlanId(planId)!!
        assertEquals(listOf(id1, id3), plan.workoutTemplates.map { it.id })
        assertEquals(listOf(1, 2), plan.workoutTemplates.map { it.order })
    }

    @Test
    fun appendExerciseToWorkoutTemplateAddsExerciseAtEnd() = runBlocking {
        val templateId = newTemplate()
        val benchId = newExercise("Жим")
        val rowId = newExercise("Тяга")

        val firstId = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, benchId, order = 99)
        ).toInt()
        val secondId = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, rowId, order = 99)
        ).toInt()

        val exercises = db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(templateId)
        assertEquals(listOf(firstId, secondId), exercises.map { it.exerciseToDo.id })
        assertEquals(listOf(1, 2), exercises.map { it.exerciseToDo.order })
    }

    @Test(expected = IllegalArgumentException::class)
    fun appendExerciseToWorkoutTemplateRejectsInvalidSets() {
        runBlocking {
            val templateId = newTemplate()
            val exerciseId = newExercise("Жим")

            repository.appendExerciseToWorkoutTemplate(
                exerciseToDo(templateId, exerciseId, sets = 0)
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun appendExerciseToWorkoutTemplateRejectsInvalidReps() {
        runBlocking {
            val templateId = newTemplate()
            val exerciseId = newExercise("Жим")

            repository.appendExerciseToWorkoutTemplate(
                exerciseToDo(templateId, exerciseId, reps = 0)
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun appendExerciseToWorkoutTemplateRejectsNegativeWeight() {
        runBlocking {
            val templateId = newTemplate()
            val exerciseId = newExercise("Жим")

            repository.appendExerciseToWorkoutTemplate(
                exerciseToDo(templateId, exerciseId, weight = -1.0)
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun appendExerciseToWorkoutTemplateRejectsInvalidPeriodization() {
        runBlocking {
            val templateId = newTemplate()
            val exerciseId = newExercise("Жим")

            repository.appendExerciseToWorkoutTemplate(
                exerciseToDo(templateId, exerciseId).copy(
                    periodizationEnabled = true,
                    modeA = TrainingMode.STRENGTH,
                    modeB = TrainingMode.STRENGTH,
                )
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun appendExerciseToWorkoutTemplateRejectsCustomModeWithoutName() {
        runBlocking {
            val templateId = newTemplate()
            val exerciseId = newExercise("Жим")

            repository.appendExerciseToWorkoutTemplate(
                exerciseToDo(templateId, exerciseId).copy(trainingMode = TrainingMode.CUSTOM)
            )
        }
    }

    @Test
    fun updateExerciseInWorkoutTemplatePersistsChanges() = runBlocking {
        val templateId = newTemplate()
        val exerciseId = newExercise("Жим")
        val todoId = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, exerciseId, sets = 3, reps = 8, weight = 50.0)
        ).toInt()
        val saved = db.exerciseToDoDao().getExerciseToDoById(todoId)!!

        repository.updateExerciseInWorkoutTemplate(
            saved.copy(sets = 5, reps = 5, weight = 100.0, trainingMode = TrainingMode.STRENGTH)
        )

        val updated = db.exerciseToDoDao().getExerciseToDoById(todoId)!!
        assertEquals(5, updated.sets)
        assertEquals(5, updated.reps)
        assertEquals(100.0, updated.weight!!, 0.0001)
        assertEquals(TrainingMode.STRENGTH, updated.trainingMode)
    }

    @Test
    fun removeExerciseFromWorkoutTemplateDeletesExerciseAndCompactsOrder() = runBlocking {
        val templateId = newTemplate()
        val firstId = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Жим"))
        ).toInt()
        val secondId = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Тяга"))
        ).toInt()
        val thirdId = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Присед"))
        ).toInt()
        val second = db.exerciseToDoDao().getExerciseToDoById(secondId)!!

        repository.removeExerciseFromWorkoutTemplate(second)

        val exercises = db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(templateId)
        assertEquals(listOf(firstId, thirdId), exercises.map { it.exerciseToDo.id })
        assertEquals(listOf(1, 2), exercises.map { it.exerciseToDo.order })
    }

    @Test
    fun reorderExercisesInWorkoutTemplateReordersOnlyCompleteTemplateList() = runBlocking {
        val templateId = newTemplate()
        val id1 = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Жим"))
        ).toInt()
        val id2 = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Тяга"))
        ).toInt()
        val id3 = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Присед"))
        ).toInt()

        repository.reorderExercisesInWorkoutTemplate(templateId, listOf(id3, id1, id2))

        val exercises = db.exerciseToDoDao().getExerciseDetailsForWorkoutOnce(templateId)
        assertEquals(listOf(id3, id1, id2), exercises.map { it.exerciseToDo.id })
        assertEquals(listOf(1, 2, 3), exercises.map { it.exerciseToDo.order })
    }

    @Test(expected = IllegalArgumentException::class)
    fun reorderExercisesInWorkoutTemplateRejectsDuplicateIds() = runBlocking {
        val templateId = newTemplate()
        val id1 = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Жим"))
        ).toInt()
        repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Тяга"))
        )

        repository.reorderExercisesInWorkoutTemplate(templateId, listOf(id1, id1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun reorderExercisesInWorkoutTemplateRejectsIdsFromOtherTemplate() = runBlocking {
        val templateId = newTemplate()
        val otherTemplateId = newTemplate(name = "Other")
        val id1 = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(templateId, newExercise("Жим"))
        ).toInt()
        val otherId = repository.appendExerciseToWorkoutTemplate(
            exerciseToDo(otherTemplateId, newExercise("Тяга"))
        ).toInt()

        repository.reorderExercisesInWorkoutTemplate(templateId, listOf(id1, otherId))
    }

    private suspend fun newTemplate(name: String = "A"): Int {
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "Plan $name")).toInt()
        return repository.appendWorkoutTemplate(name, planId).toInt()
    }

    private suspend fun newExercise(name: String): Int =
        db.exerciseDao().insertExercise(
            Exercise(name = name, bodyRegion = BodyRegion.CHEST)
        ).toInt()

    private fun exerciseToDo(
        templateId: Int,
        exerciseId: Int,
        order: Int = 1,
        sets: Int = 3,
        reps: Int = 8,
        weight: Double? = 50.0,
    ): ExerciseToDo = ExerciseToDo(
        exerciseId = exerciseId,
        workoutTemplateId = templateId,
        sets = sets,
        reps = reps,
        weight = weight,
        order = order,
        trainingMode = TrainingMode.HYPERTROPHY,
    )
}
