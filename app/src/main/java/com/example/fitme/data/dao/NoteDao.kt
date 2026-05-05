package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.enums.TrainingMode
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    @Query(
        """
        SELECT n.* FROM notes n
        JOIN exercises_todo etd ON etd.id = n.exercise_to_do_id
        JOIN workout_sessions ws ON ws.id = n.workout_session_id
        WHERE etd.exercise_id = :exerciseId
        ORDER BY ws.date DESC, n.set_index ASC
        """
    )
    fun getNotesForExercise(exerciseId: Int): Flow<List<Note>>

    @Query(
        """
        SELECT * FROM notes
        WHERE workout_session_id = :workoutSessionId AND exercise_to_do_id = :exerciseToDoId
        ORDER BY set_index ASC
        """
    )
    fun getNotesForExerciseInSession(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ): Flow<List<Note>>

    @Query(
        """
        SELECT ws.id FROM workout_sessions ws
        JOIN notes n ON n.workout_session_id = ws.id
        WHERE n.exercise_to_do_id = :exerciseToDoId AND n.mode_used = :mode
        ORDER BY ws.date DESC, ws.id DESC
        LIMIT 1
        """
    )
    suspend fun findLastSessionIdWithMode(
        exerciseToDoId: Int,
        mode: TrainingMode,
    ): Int?

    @Query(
        """
        SELECT * FROM notes
        WHERE workout_session_id = :workoutSessionId AND exercise_to_do_id = :exerciseToDoId
        ORDER BY set_index ASC
        """
    )
    suspend fun getNotesForExerciseInSessionOnce(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ): List<Note>

    @Query(
        """
        SELECT n.mode_used FROM notes n
        JOIN workout_sessions ws ON ws.id = n.workout_session_id
        WHERE n.exercise_to_do_id = :exerciseToDoId
        ORDER BY ws.date DESC, ws.id DESC, n.set_index DESC
        LIMIT 1
        """
    )
    suspend fun getLastModeFor(exerciseToDoId: Int): TrainingMode?

    @Query(
        """
        SELECT MAX(set_index) FROM notes
        WHERE workout_session_id = :workoutSessionId AND exercise_to_do_id = :exerciseToDoId
        """
    )
    suspend fun getMaxSetIndex(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ): Int?
}
