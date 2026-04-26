package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.relations.ExerciseWithDetails
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM exercises_todo WHERE workout_template_id = :workoutTemplateId ORDER BY `order`")
    fun getExercisesToDoForWorkout(workoutTemplateId: Int): Flow<List<ExerciseToDo>>

    @Transaction
    @Query("SELECT * FROM exercises_todo WHERE workout_template_id = :workoutTemplateId ORDER BY `order`")
    fun getExerciseDetailsForWorkout(workoutTemplateId: Int): Flow<List<ExerciseWithDetails>>
}
