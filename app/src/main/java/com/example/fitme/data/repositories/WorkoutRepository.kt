package com.example.fitme.data.repositories

import androidx.room.withTransaction
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.enums.TrainingMode
import com.example.fitme.data.models.NextExercisePlan
import com.example.fitme.data.models.NextWorkoutPlan
import com.example.fitme.data.models.NextWorkoutPreview
import com.example.fitme.data.models.PlanWorkoutTemplates
import java.time.LocalDate

class WorkoutRepository(private val db: AppDatabase) {

    private val workoutPlanDao = db.workoutPlanDao()
    private val workoutSessionDao = db.workoutSessionDao()
    private val exerciseToDoDao = db.exerciseToDoDao()
    private val noteRepository = NoteRepository(db)

    //прочитать список тренировок по id плана
    suspend fun getWorkoutTemplatesByPlanId(planId: Int): PlanWorkoutTemplates? {
        val plan = workoutPlanDao.getPlanById(planId) ?: return null
        return PlanWorkoutTemplates(
            plan = plan,
            workoutTemplates = workoutPlanDao.getWorkoutTemplatesForPlanOnce(planId),
        )
    }

    suspend fun createNewPlan(name: String): Long =
        workoutPlanDao.insertPlan(Plan(name = validateName(name, "Plan name")))

    fun getActivePlans() = workoutPlanDao.getAllActivePlans()
    fun getInactivePlans() = workoutPlanDao.getAllInactivePlans()
    fun getAllPlans() = workoutPlanDao.getAllPlans()
    suspend fun archivePlan(plan: Plan) = workoutPlanDao.updatePlan(plan.copy(isActive = false))
    suspend fun restorePlan(plan: Plan) = workoutPlanDao.updatePlan(plan.copy(isActive = true))
    suspend fun updatePlan(plan: Plan) =
        workoutPlanDao.updatePlan(plan.copy(name = validateName(plan.name, "Plan name")))

    suspend fun removeWorkoutTemplateForPlan(workoutTemplate: WorkoutTemplate) = db.withTransaction {
        workoutPlanDao.deleteWorkoutTemplate(workoutTemplate)
        val remainingIds = workoutPlanDao.getWorkoutTemplateIdsForPlan(workoutTemplate.planId)
        remainingIds.forEachIndexed { index, id ->
            workoutPlanDao.setWorkoutTemplateOrder(id, index + 1)
        }
    }

    //добавить тренировку в конец плана
    suspend fun appendWorkoutTemplate(name: String, planId: Int): Long =
        workoutPlanDao.appendWorkoutTemplate(validateName(name, "Workout template name"), planId)

    //поменять список тренировок в новом порядке
    suspend fun reorderWorkoutTemplates(
        planId: Int,
        orderedIds: List<Int>,
    ) = db.withTransaction {
        val currentIds = workoutPlanDao.getWorkoutTemplateIdsForPlan(planId)
        require(orderedIds.size == orderedIds.toSet().size) {
            "Workout order contains duplicate ids"
        }
        require(orderedIds.toSet() == currentIds.toSet()) {
            "Workout order must contain all and only workouts from this plan"
        }
        orderedIds.forEachIndexed { index, id ->
            workoutPlanDao.setWorkoutTemplateOrder(id, index + 1)
        }
    }

    suspend fun appendExerciseToWorkoutTemplate(exerciseToDo: ExerciseToDo): Long = db.withTransaction {
        validateExerciseToDo(exerciseToDo)
        val nextOrder = (exerciseToDoDao.getMaxOrderForWorkoutTemplate(
            exerciseToDo.workoutTemplateId
        ) ?: 0) + 1
        exerciseToDoDao.insertExerciseToDo(
            exerciseToDo.copy(id = 0, order = nextOrder)
        )
    }

    suspend fun updateExerciseInWorkoutTemplate(exerciseToDo: ExerciseToDo) {
        validateExerciseToDo(exerciseToDo)
        exerciseToDoDao.updateExerciseToDo(exerciseToDo)
    }

    suspend fun removeExerciseFromWorkoutTemplate(exerciseToDo: ExerciseToDo) = db.withTransaction {
        exerciseToDoDao.deleteExerciseToDo(exerciseToDo)
        val remainingIds = exerciseToDoDao.getExerciseToDoIdsForWorkoutTemplate(
            exerciseToDo.workoutTemplateId
        )
        remainingIds.forEachIndexed { index, id ->
            exerciseToDoDao.setExerciseToDoOrder(id, index + 1)
        }
    }

    suspend fun reorderExercisesInWorkoutTemplate(
        workoutTemplateId: Int,
        orderedIds: List<Int>,
    ) = db.withTransaction {
        val currentIds = exerciseToDoDao.getExerciseToDoIdsForWorkoutTemplate(workoutTemplateId)
        require(orderedIds.size == orderedIds.toSet().size) {
            "Exercise order contains duplicate ids"
        }
        require(orderedIds.toSet() == currentIds.toSet()) {
            "Exercise order must contain all and only exercises from this workout template"
        }
        orderedIds.forEachIndexed { index, id ->
            exerciseToDoDao.setExerciseToDoOrder(id, index + 1)
        }
    }

    //показывает какая сессия будет создана, собирает сессию, но не создает запись в бд
    suspend fun peekNextWorkoutSession(planId: Int): NextWorkoutPreview? {
        val nextTemplate = pickNextTemplate(planId) ?: return null
        val exercises = exerciseToDoDao.getExerciseDetailsForWorkoutOnce(nextTemplate.id)
        val plans = exercises.map { details ->
            val etd = details.exerciseToDo
            val mode = chooseMode(etd)
            val planned = pickPlannedParams(etd, mode)
            val prefill = noteRepository.getLastNotesByMode(etd.id, mode)
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
        val lastTemplateId = lastSession.workoutTemplateId
            ?: return workoutPlanDao.getFirstWorkoutTemplate(planId)
        val lastTemplate = workoutPlanDao.getWorkoutTemplateById(lastTemplateId)
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
        return when (noteRepository.getLastModeFor(etd.id)) {
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

    private fun validateName(name: String, fieldName: String): String {
        val normalized = name.trim()
        require(normalized.isNotBlank()) { "$fieldName must not be blank" }
        return normalized
    }

    private fun validateExerciseToDo(exerciseToDo: ExerciseToDo) {
        require(exerciseToDo.sets > 0) { "Exercise sets must be greater than zero" }
        require(exerciseToDo.reps > 0) { "Exercise reps must be greater than zero" }
        validateNonNegative(exerciseToDo.weight, "Exercise weight")
        validateNonNegative(exerciseToDo.duration, "Exercise duration")
        validateCustomModeName(
            customTrainingModeName = exerciseToDo.customTrainingModeName,
            modes = listOf(exerciseToDo.trainingMode, exerciseToDo.modeA, exerciseToDo.modeB),
        )
        if (exerciseToDo.periodizationEnabled) {
            val modeA = exerciseToDo.modeA
            val modeB = exerciseToDo.modeB
            require(modeA != null) { "Periodization modeA must be set" }
            require(modeB != null) { "Periodization modeB must be set" }
            require(modeA != modeB) { "Periodization modes must be different" }
            validatePositive(exerciseToDo.setsA, "Periodization setsA")
            validatePositive(exerciseToDo.repsA, "Periodization repsA")
            validateNonNegative(exerciseToDo.weightA, "Periodization weightA")
            validatePositive(exerciseToDo.setsB, "Periodization setsB")
            validatePositive(exerciseToDo.repsB, "Periodization repsB")
            validateNonNegative(exerciseToDo.weightB, "Periodization weightB")
        }
    }

    private fun validateCustomModeName(
        customTrainingModeName: String?,
        modes: List<TrainingMode?>,
    ) {
        if (modes.any { it == TrainingMode.CUSTOM }) {
            require(!customTrainingModeName.isNullOrBlank()) {
                "Custom training mode name must not be blank"
            }
        }
    }

    private fun validatePositive(value: Int?, fieldName: String) {
        require(value == null || value > 0) { "$fieldName must be greater than zero" }
    }

    private fun validateNonNegative(value: Int?, fieldName: String) {
        require(value == null || value >= 0) { "$fieldName must not be negative" }
    }

    private fun validateNonNegative(value: Double?, fieldName: String) {
        require(value == null || value >= 0.0) { "$fieldName must not be negative" }
    }
}
