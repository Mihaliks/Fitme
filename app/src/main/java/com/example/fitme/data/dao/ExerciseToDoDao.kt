package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.relations.ExerciseWithDetails

@Dao
interface ExerciseToDoDao {

    @Insert
    suspend fun insertExerciseToDo(exerciseToDo: ExerciseToDo): Long

    @Update
    suspend fun updateExerciseToDo(exerciseToDo: ExerciseToDo)

    @Delete
    suspend fun deleteExerciseToDo(exerciseToDo: ExerciseToDo)

    @Query("SELECT * FROM exercises_todo WHERE id = :exerciseToDoId")
    suspend fun getExerciseToDoById(exerciseToDoId: Int): ExerciseToDo?

    @Query("SELECT MAX(`order`) FROM exercises_todo WHERE workout_template_id = :workoutTemplateId")
    suspend fun getMaxOrderForWorkoutTemplate(workoutTemplateId: Int): Int?

    @Query("SELECT id FROM exercises_todo WHERE workout_template_id = :workoutTemplateId ORDER BY `order`")
    suspend fun getExerciseToDoIdsForWorkoutTemplate(workoutTemplateId: Int): List<Int>

    @Query("UPDATE exercises_todo SET `order` = :newOrder WHERE id = :id")
    suspend fun setExerciseToDoOrder(id: Int, newOrder: Int)

    @Transaction
    @Query("""
      SELECT * FROM exercises_todo
      WHERE workout_template_id = :workoutTemplateId
      ORDER BY `order`
  """)
    suspend fun getExerciseDetailsForWorkoutOnce(
        workoutTemplateId: Int
    ): List<ExerciseWithDetails>
}
