package com.example.fitme.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.entities.relations.WorkoutSessionWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert
    suspend fun insertWorkoutSession(workoutSession: WorkoutSession): Long

    @Update
    suspend fun updateWorkoutSession(workoutSession: WorkoutSession)

    @Delete
    suspend fun deleteWorkoutSession(workoutSession: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllWorkoutSessions(): PagingSource<Int, WorkoutSession>

    @Query("SELECT * FROM workout_sessions WHERE workout_template_id = :workoutTemplateId ORDER BY date DESC")
    fun getWorkoutSessionsForTemplate(workoutTemplateId: Int): Flow<List<WorkoutSession>>


    // получить прошлую(последнюю) сессию этого типа
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE workout_template_id = :workoutTemplateId
        ORDER BY date DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getLastSessionForTemplate(workoutTemplateId: Int): WorkoutSession?

    // получить последнюю сессию в плане (для создания следующей сессии)
    @Query(
        """
        SELECT ws.* FROM workout_sessions ws
        JOIN workout_templates wt ON wt.id = ws.workout_template_id
        WHERE wt.plan_id = :planId
        ORDER BY ws.date DESC, ws.id DESC
        LIMIT 1
        """
    )
    suspend fun getLastSessionForPlan(planId: Int): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE id = :workoutSessionId")
    suspend fun getWorkoutSessionById(workoutSessionId: Int): WorkoutSession?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :workoutSessionId")
    suspend fun getWorkoutSessionWithExercises(workoutSessionId: Int): WorkoutSessionWithExercises?
}
