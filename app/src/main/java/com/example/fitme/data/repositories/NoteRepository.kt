package com.example.fitme.data.repositories

import androidx.room.withTransaction
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Note
import com.example.fitme.data.entities.enums.TrainingMode

class NoteRepository(private val db: AppDatabase) {

    private val noteDao = db.noteDao()
    private val workoutSessionDao = db.workoutSessionDao()
    private val exerciseToDoDao = db.exerciseToDoDao()
    fun getNotesForExercise(exerciseId: Int) =
        noteDao.getNotesForExercise(exerciseId)

    fun getNotesForExerciseInSession(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ) = noteDao.getNotesForExerciseInSession(workoutSessionId, exerciseToDoId)

    suspend fun getNotesForExerciseInSessionOnce(
        workoutSessionId: Int,
        exerciseToDoId: Int,
    ): List<Note> =
        noteDao.getNotesForExerciseInSessionOnce(workoutSessionId, exerciseToDoId)

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
        validateNoteInput(reps = reps, weight = weight, duration = duration)
        val session = workoutSessionDao.getWorkoutSessionById(workoutSessionId)
            ?: error("Workout session not found")
        val exerciseToDo = exerciseToDoDao.getExerciseToDoById(exerciseToDoId)
            ?: error("ExerciseToDo not found")
        require(session.workoutTemplateId == exerciseToDo.workoutTemplateId) {
            "ExerciseToDo does not belong to workout session template"
        }
        val nextIndex = (noteDao.getMaxSetIndex(workoutSessionId, exerciseToDoId) ?: 0) + 1
        noteDao.insertNote(
            Note(
                exerciseToDoId = exerciseToDoId,
                exerciseId = exerciseToDo.exerciseId,
                workoutSessionId = workoutSessionId,
                setIndex = nextIndex,
                modeUsed = modeUsed,
                reps = reps,
                weight = weight,
                duration = duration,
            )
        )
    }

    private fun validateNoteInput(
        reps: Int?,
        weight: Double?,
        duration: Int?,
    ) {
        require(reps == null || reps > 0) { "Note reps must be greater than zero" }
        require(weight == null || weight >= 0.0) { "Note weight must not be negative" }
        require(duration == null || duration >= 0) { "Note duration must not be negative" }
    }
}
