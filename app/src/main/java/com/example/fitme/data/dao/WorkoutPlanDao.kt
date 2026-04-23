package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate
import com.example.fitme.data.entities.relations.PlanWithWorkouts
import com.example.fitme.data.entities.relations.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {

    @Insert
    suspend fun insertPlan(plan: Plan): Long

    @Update
    suspend fun updatePlan(plan: Plan)

    @Delete
    suspend fun deletePlan(plan: Plan)

    @Query("SELECT * FROM plans ORDER BY id")
    fun getAllPlans(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getPlanById(planId: Int): Plan?

    @Transaction
    @Query("SELECT * FROM plans ORDER BY id")
    fun getAllPlansWithWorkouts(): Flow<List<PlanWithWorkouts>>

    @Transaction
    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getPlanWithWorkouts(planId: Int): PlanWithWorkouts?

    @Insert
    suspend fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long

    @Update
    suspend fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate)

    @Delete
    suspend fun deleteWorkoutTemplate(workoutTemplate: WorkoutTemplate)

    @Query("SELECT * FROM workout_templates WHERE id = :workoutTemplateId")
    suspend fun getWorkoutTemplateById(workoutTemplateId: Int): WorkoutTemplate?

    @Query("SELECT * FROM workout_templates WHERE plan_id = :planId ORDER BY id")
    fun getWorkoutTemplatesForPlan(planId: Int): Flow<List<WorkoutTemplate>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :workoutTemplateId")
    suspend fun getWorkoutWithExercises(workoutTemplateId: Int): WorkoutWithExercises?
}
