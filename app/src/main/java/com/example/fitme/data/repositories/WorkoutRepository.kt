package com.example.fitme.data.repositories
import com.example.fitme.data.dao.WorkoutPlanDao
import com.example.fitme.data.dao.WorkoutSessionDao
import com.example.fitme.data.entities.relations.PlanWithWorkouts

class WorkoutRepository(private val workoutPlanDao: WorkoutPlanDao, private val workoutSessionDao: WorkoutSessionDao) {

     //получение списка тренировок по плану
     suspend fun getWorkoutTemplatesByPlanId(planId: Int): PlanWithWorkouts? {
         return workoutPlanDao.getPlanWithWorkouts(planId)
     }
    //открытие тренировки
}