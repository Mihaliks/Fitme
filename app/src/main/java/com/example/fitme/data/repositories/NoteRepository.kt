package com.example.fitme.data.repositories

import androidx.room.withTransaction
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.enums.TrainingMode

class NoteRepository(private val db: AppDatabase) {

    private val noteDao = db.noteDao()

    fun getNotesForExercise(exerciseId: Int) =
        noteDao.getNotesForExercise(exerciseId)

    fun getNotesForExerciseInSession(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ) = noteDao.getNotesForExerciseInSession(workoutSessionId, exerciseToDoId)

    suspend fun getNoteById(noteId: Int): Note? =
        noteDao.getNoteById(noteId)

    suspend fun updateNote(note: Note) =
        noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(note)

    suspend fun getLastNotesByMode(
        exerciseToDoId: Int,
        mode: TrainingMode,
    ): List<Note> = db.withTransaction {
        val sessionId = noteDao.findLastSessionIdWithMode(exerciseToDoId, mode)
            ?: return@withTransaction emptyList()
        noteDao.getNotesForExerciseInSessionOnce(sessionId, exerciseToDoId)
    }

    suspend fun getLastModeFor(exerciseToDoId: Int): TrainingMode? =
        noteDao.getLastModeFor(exerciseToDoId)

    suspend fun appendNote(
        workoutSessionId: Int,
        exerciseToDoId: Int,
        modeUsed: TrainingMode,
        reps: Int? = null,
        weight: Double? = null,
        duration: Int? = null,
    ): Long = db.withTransaction {
        val nextIndex = (noteDao.getMaxSetIndex(workoutSessionId, exerciseToDoId) ?: 0) + 1
        noteDao.insertNote(
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
