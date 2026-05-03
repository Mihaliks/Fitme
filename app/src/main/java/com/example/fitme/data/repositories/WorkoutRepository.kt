package com.example.fitme.data.repositories

import androidx.room.withTransaction
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.relations.PlanWithWorkouts

class WorkoutRepository(private val db: AppDatabase) {

    private val workoutPlanDao = db.workoutPlanDao()
    private val workoutSessionDao = db.workoutSessionDao()

    //прочитать список тренировок по id плана
    suspend fun getWorkoutTemplatesByPlanId(planId: Int): PlanWithWorkouts? =
        workoutPlanDao.getPlanWithWorkouts(planId)

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

    //прочитать упражнения в тренировке по шаблону тренировки
    suspend fun getExercisesByWorkoutTemplateId(workoutTemplateId: Int) =
        workoutPlanDao.getWorkoutWithExercises(workoutTemplateId)

    //создать следующую тренировку на основе предыдущей тренировки с таким же order в плане:
    suspend fun createNextWorkoutSession(planId: Intну ){

    }

}
