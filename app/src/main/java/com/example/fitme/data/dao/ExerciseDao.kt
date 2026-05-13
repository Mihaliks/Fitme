package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {


    @Insert
    suspend fun insertExercise(exercise: Exercise): Long

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY name")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE is_active = 1 ORDER BY name")
    fun getActiveExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE is_active = 0 ORDER BY name")
    fun getInactiveExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE is_active = 1 AND muscle = :muscleGroup ORDER BY name")
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE is_active = 1 AND body_region = :bodyRegion ORDER BY name")
    fun getExercisesByBodyRegion(bodyRegion: BodyRegion): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE is_active = 1 AND body_region = :bodyRegion AND muscle = :muscleGroup ORDER BY name")
    fun getExercisesByBodyRegionAndMuscleGroup(bodyRegion: BodyRegion, muscleGroup: MuscleGroup): Flow<List<Exercise>>

    @Query("""
      SELECT * FROM exercises
      WHERE is_active = 1
        AND name LIKE '%' || :query || '%'
      ORDER BY name
      LIMIT :limit
  """)
    fun searchActiveExercises(
        query: String,
        limit: Int = 20
    ): Flow<List<Exercise>>
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Int): Exercise?
}
