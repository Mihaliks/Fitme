package com.example.fitme.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.fitme.data.dao.WorkoutSessionDao
import com.example.fitme.data.entities.WorkoutSession
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val workoutSessionDao: WorkoutSessionDao) {

    //загрузить последние 10 тренировок в истории, догружается постранично.
    fun getWorkoutSessions(): Flow<PagingData<WorkoutSession>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                prefetchDistance = 3,
                enablePlaceholders = false
            ),

            pagingSourceFactory = {
                workoutSessionDao.getAllWorkoutSessions()
            }
        ).flow

    }
    //открыть информацию по прошедшей тренировке: список выполненных упражнений - List<ExerciseWithDetails> - упражнения и данные по конкретной тренировке
    suspend fun getWorkoutSessionWithExercises(workoutSessionId: Int) =
        workoutSessionDao.getWorkoutSessionWithExercises(workoutSessionId)
}