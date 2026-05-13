package com.example.fitme.data.repositories

import com.example.fitme.data.dao.ExerciseDao
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {

    fun getAllActiveExercises() = exerciseDao.getActiveExercises()
    fun getAllInactiveExercises() = exerciseDao.getInactiveExercises()
    fun getAllExercisesByMuscleGroup(muscleGroup: MuscleGroup) =
        exerciseDao.getExercisesByMuscleGroup(muscleGroup)

    fun getAllExercisesByBodyRegion(bodyRegion: BodyRegion) =
        exerciseDao.getExercisesByBodyRegion(bodyRegion)

    fun getAllExercisesByBodyRegionAndMuscleGroup(
        bodyRegion: BodyRegion, muscleGroup: MuscleGroup
    ) = exerciseDao.getExercisesByBodyRegionAndMuscleGroup(bodyRegion, muscleGroup)


    // поиск который необходимо правильно обработать на уровне UI - с задержкой
    fun searchActiveExercises(query: String): Flow<List<Exercise>> {
        val normalized = query.trim()
        if (normalized.isBlank()) {
            return exerciseDao.getActiveExercises()
        }
        return exerciseDao.searchActiveExercises(normalized)
    }

    suspend fun createCustomExercise(exercise: Exercise): Long {
        return exerciseDao.insertExercise(
            exercise.copy(id = 0, isActive = true, isBuiltIn = false)
        )
    }
    // через это менять статус активности
    suspend fun updateCustomExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise)
    }

    suspend fun archiveCustomExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise.copy(isActive = false))
    }

    suspend fun getExerciseById(exerciseId: Int): Exercise? =
        exerciseDao.getExerciseById(exerciseId)
}
