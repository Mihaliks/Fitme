package com.example.fitme.data.repositories

import androidx.room.withTransaction
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.entities.relations.PlanWithWorkouts
import java.time.LocalDate

class WorkoutRepository(private val db: AppDatabase) {

    private val workoutPlanDao = db.workoutPlanDao()
    private val workoutSessionDao = db.workoutSessionDao()
    private val noteDao = db.noteDao()

    //прочитать список тренировок по id плана
    suspend fun getWorkoutTemplatesByPlanId(planId: Int): PlanWithWorkouts? =
        workoutPlanDao.getPlanWithWorkouts(planId)

    //создать новый план
    suspend fun createNewPlan(name: String): Long = workoutPlanDao.insertPlan(Plan(name=name))
    //добавить тренировку в конец плана
    suspend fun appendWorkoutTemplate(name: String, planId: Int): Long =
        workoutPlanDao.appendWorkoutTemplate(name, planId)

    //поменять список тренировок в новом порядке
    suspend fun reorderWorkoutTemplates(orderedIds: List<Int>) {
        db.withTransaction {
            orderedIds.forEachIndexed { index, id ->
                workoutPlanDao.setWorkoutTemplateOrder(id, index + 1)
            }
        }
    }

    //показывает какая сессия будет создана, собирает сессию, но не создает запись в бд
    suspend fun peekNextWorkoutSession(planId: Int): NextWorkoutPreview? {
        val nextTemplate = pickNextTemplate(planId) ?: return null
        val templateWithExercises = workoutPlanDao.getWorkoutWithExercises(nextTemplate.id)
            ?: return null
        val plans = templateWithExercises.exercises.map { details ->
            val etd = details.exerciseToDo
            val mode = chooseMode(etd)
            val planned = pickPlannedParams(etd, mode)
            val prefill = noteDao.getLastNotesByMode(etd.id, mode)
            NextExercisePlan(
                exerciseToDo = etd,
                exercise = details.exercise,
                chosenMode = mode,
                plannedSets = planned.sets,
                plannedReps = planned.reps,
                plannedWeight = planned.weight,
                prefillNotes = prefill,
            )
        }
        return NextWorkoutPreview(template = nextTemplate, exercises = plans)
    }


    //создает экземпляр сессии и запускает тренировку.
    suspend fun createNextWorkoutSession(planId: Int): NextWorkoutPlan? = db.withTransaction {
        val preview = peekNextWorkoutSession(planId) ?: return@withTransaction null
        val sessionId = workoutSessionDao.insertWorkoutSession(
            WorkoutSession(
                workoutTemplateId = preview.template.id,
                date = LocalDate.now(),
            )
        ).toInt()

        NextWorkoutPlan(
            sessionId = sessionId,
            template = preview.template,
            exercises = preview.exercises,
        )
    }

    // берет следующий шаблон тренировки по плану относительно последней сессии, или первый, если сессий не было
    private suspend fun pickNextTemplate(planId: Int): WorkoutTemplate? {
        val lastSession = workoutSessionDao.getLastSessionForPlan(planId)
            ?: return workoutPlanDao.getFirstWorkoutTemplate(planId)
        val lastTemplate = workoutPlanDao.getWorkoutTemplateById(lastSession.workoutTemplateId)
            ?: return workoutPlanDao.getFirstWorkoutTemplate(planId)
        return workoutPlanDao.getNextWorkoutTemplateAfter(planId, lastTemplate.order)
            ?: workoutPlanDao.getFirstWorkoutTemplate(planId)
    }

    // переключает режим у упражнения с включенной периодизацией относительно прошлого режима
    private suspend fun chooseMode(etd: ExerciseToDo): TrainingMode {
        if (!etd.periodizationEnabled) return etd.trainingMode
        val a = etd.modeA
        val b = etd.modeB
        if (a == null || b == null || a == b) return etd.trainingMode
        return when (noteDao.getLastModeFor(etd.id)) {
            a -> b
            b -> a
            else -> a
        }
    }
    //выбирает какие ожидания от пользователя в этом упражнении
    private fun pickPlannedParams(etd: ExerciseToDo, mode: TrainingMode): PlannedParams {
        if (!etd.periodizationEnabled) {
            return PlannedParams(etd.sets, etd.reps, etd.weight)
        }
        return when (mode) {
            etd.modeA -> PlannedParams(
                sets = etd.setsA ?: etd.sets,
                reps = etd.repsA ?: etd.reps,
                weight = etd.weightA ?: etd.weight,
            )
            etd.modeB -> PlannedParams(
                sets = etd.setsB ?: etd.sets,
                reps = etd.repsB ?: etd.reps,
                weight = etd.weightB ?: etd.weight,
            )
            else -> PlannedParams(etd.sets, etd.reps, etd.weight)
        }
    }

    // сущность для выкидывания во фронтенд
    private data class PlannedParams(val sets: Int, val reps: Int, val weight: Double?)
}

// сущности для выкидывания во фронтенд

data class NextWorkoutPreview(
    val template: WorkoutTemplate,
    val exercises: List<NextExercisePlan>,
)

data class NextWorkoutPlan(
    val sessionId: Int,
    val template: WorkoutTemplate,
    val exercises: List<NextExercisePlan>,
)

data class NextExercisePlan(
    val exerciseToDo: ExerciseToDo,
    val exercise: Exercise,
    val chosenMode: TrainingMode,
    val plannedSets: Int,
    val plannedReps: Int,
    val plannedWeight: Double?,
    // Сеты предыдущей сессии того же режима — для подсказки в UI. Пусто, если истории нет.
    val prefillNotes: List<Note>,
)
