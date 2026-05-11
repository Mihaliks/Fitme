package com.example.fitme.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.WorkoutSession
import com.example.fitme.data.models.WorkoutSessionExerciseHistory
import com.example.fitme.data.models.WorkoutSessionHistory
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val db: AppDatabase) {

    private val workoutSessionDao = db.workoutSessionDao()
    private val workoutPlanDao = db.workoutPlanDao()
    private val exerciseToDoDao = db.exerciseToDoDao()
    private val noteRepository = NoteRepository(db)

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

    suspend fun getWorkoutSessionHistory(workoutSessionId: Int): WorkoutSessionHistory? {
        val session = workoutSessionDao.getWorkoutSessionById(workoutSessionId)
            ?: return null
        val template = workoutPlanDao.getWorkoutTemplateById(session.workoutTemplateId)
            ?: return null
        val exercises = exerciseToDoDao.getExerciseDetailsForWorkoutOnce(template.id)
            .map { details ->
                WorkoutSessionExerciseHistory(
                    exerciseToDo = details.exerciseToDo,
                    exercise = details.exercise,
                    notes = noteRepository.getNotesForExerciseInSessionOnce(
                        workoutSessionId = workoutSessionId,
                        exerciseToDoId = details.exerciseToDo.id,
                    ),
                )
            }

        return WorkoutSessionHistory(
            workoutSession = session,
            workoutTemplate = template,
            exercises = exercises,
        )
    }
}
