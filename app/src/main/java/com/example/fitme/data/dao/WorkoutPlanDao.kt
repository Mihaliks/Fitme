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
abstract class WorkoutPlanDao {

    @Insert
    abstract suspend fun insertPlan(plan: Plan): Long

    @Update
    abstract suspend fun updatePlan(plan: Plan)

    @Delete
    abstract suspend fun deletePlan(plan: Plan)

    @Query("SELECT * FROM plans ORDER BY id")
    abstract fun getAllPlans(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :planId")
    abstract suspend fun getPlanById(planId: Int): Plan?

    @Transaction
    @Query("SELECT * FROM plans ORDER BY id")
    abstract fun getAllPlansWithWorkouts(): Flow<List<PlanWithWorkouts>>

    @Transaction
    @Query("SELECT * FROM plans WHERE id = :planId")
    abstract suspend fun getPlanWithWorkouts(planId: Int): PlanWithWorkouts?

    @Insert
    abstract suspend fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long

    @Update
    abstract suspend fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate)

    @Delete
    abstract suspend fun deleteWorkoutTemplate(workoutTemplate: WorkoutTemplate)

    @Query("SELECT * FROM workout_templates WHERE id = :workoutTemplateId")
    abstract suspend fun getWorkoutTemplateById(workoutTemplateId: Int): WorkoutTemplate?

    @Query("SELECT * FROM workout_templates WHERE plan_id = :planId ORDER BY `order`")
    abstract fun getWorkoutTemplatesForPlan(planId: Int): Flow<List<WorkoutTemplate>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :workoutTemplateId")
    abstract suspend fun getWorkoutWithExercises(workoutTemplateId: Int): WorkoutWithExercises?

    @Query("SELECT MAX(`order`) FROM workout_templates WHERE plan_id = :planId")
    protected abstract suspend fun getMaxOrderForPlan(planId: Int): Int?
    @Transaction
    open suspend fun appendWorkoutTemplate(name: String, planId: Int): Long {
        val nextOrder = (getMaxOrderForPlan(planId) ?: 0) + 1
        return insertWorkoutTemplate(
            WorkoutTemplate(name = name, planId = planId, order = nextOrder)
        )
    }

    @Query("UPDATE workout_templates SET `order` = :newOrder WHERE id = :id")
    abstract suspend fun setWorkoutTemplateOrder(id: Int, newOrder: Int)

    @Query(
        """
        SELECT * FROM workout_templates
        WHERE plan_id = :planId AND `order` > :afterOrder
        ORDER BY `order` ASC
        LIMIT 1
        """
    )
    abstract suspend fun getNextWorkoutTemplateAfter(planId: Int, afterOrder: Int): WorkoutTemplate?

    @Query(
        """
        SELECT * FROM workout_templates
        WHERE plan_id = :planId
        ORDER BY `order` ASC
        LIMIT 1
        """
    )
    abstract suspend fun getFirstWorkoutTemplate(planId: Int): WorkoutTemplate?

}
