package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.enums.TrainingMode
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {

    @Insert
    abstract suspend fun insertNote(note: Note): Long

    @Update
    abstract suspend fun updateNote(note: Note)

    @Delete
    abstract suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    abstract suspend fun getNoteById(noteId: Int): Note?

    // Все Note конкретного типа упражнения каталога для построения статистики
    @Query(
        """
        SELECT n.* FROM notes n
        JOIN exercises_todo etd ON etd.id = n.exercise_to_do_id
        JOIN workout_sessions ws ON ws.id = n.workout_session_id
        WHERE etd.exercise_id = :exerciseId
        ORDER BY ws.date DESC, n.set_index ASC
        """
    )
    abstract fun getNotesForExercise(exerciseId: Int): Flow<List<Note>>

    // Сеты конкретного упражнения в конкретной сессии
    @Query(
        """
        SELECT * FROM notes
        WHERE workout_session_id = :workoutSessionId AND exercise_to_do_id = :exerciseToDoId
        ORDER BY set_index ASC
        """
    )
    abstract fun getNotesForExerciseInSession(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ): Flow<List<Note>>

    // Найти ID последней сессии, в которой данный ExerciseToDo выполнялся в указанном режиме.
    @Query(
        """
        SELECT ws.id FROM workout_sessions ws
        JOIN notes n ON n.workout_session_id = ws.id
        WHERE n.exercise_to_do_id = :exerciseToDoId AND n.mode_used = :mode
        ORDER BY ws.date DESC, ws.id DESC
        LIMIT 1
        """
    )
    protected abstract suspend fun findLastSessionIdWithMode(
        exerciseToDoId: Int,
        mode: TrainingMode,
    ): Int?

    // Все сеты упражнения в этой найденной сессии — это и есть «как было в прошлый раз».
    @Query(
        """
        SELECT * FROM notes
        WHERE workout_session_id = :workoutSessionId AND exercise_to_do_id = :exerciseToDoId
        ORDER BY set_index ASC
        """
    )
    protected abstract suspend fun getNotesForExerciseInSessionOnce(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ): List<Note>

    @Transaction
    open suspend fun getLastNotesByMode(exerciseToDoId: Int, mode: TrainingMode): List<Note> {
        val sessionId = findLastSessionIdWithMode(exerciseToDoId, mode) ?: return emptyList()
        return getNotesForExerciseInSessionOnce(sessionId, exerciseToDoId)
    }

    // Какой режим использовался в последний раз для этого слота — независимо от того, какой именно.
    // Нужно для чередования периодизации.
    @Query(
        """
        SELECT n.mode_used FROM notes n
        JOIN workout_sessions ws ON ws.id = n.workout_session_id
        WHERE n.exercise_to_do_id = :exerciseToDoId
        ORDER BY ws.date DESC, ws.id DESC, n.set_index DESC
        LIMIT 1
        """
    )
    abstract suspend fun getLastModeFor(exerciseToDoId: Int): TrainingMode?

    @Query(
        """
        SELECT MAX(set_index) FROM notes
        WHERE workout_session_id = :workoutSessionId AND exercise_to_do_id = :exerciseToDoId
        """
    )
    protected abstract suspend fun getMaxSetIndex(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ): Int?

    // Сохранить ещё один сет: setIndex считается автоматически как MAX+1 по паре (session, todo).
    @Transaction
    open suspend fun appendNote(
        workoutSessionId: Int,
        exerciseToDoId: Int,
        modeUsed: TrainingMode,
        reps: Int? = null,
        weight: Double? = null,
        duration: Int? = null,
    ): Long {
        val nextIndex = (getMaxSetIndex(workoutSessionId, exerciseToDoId) ?: 0) + 1
        return insertNote(
            Note(
                exerciseToDoId = exerciseToDoId,
                workoutSessionId = workoutSessionId,
                setIndex = nextIndex,
                modeUsed = modeUsed,
                reps = reps,
                weight = weight,
                duration = duration,
            )
        )
    }
}
