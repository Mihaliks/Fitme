package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitme.data.entities.enums.TrainingMode

@Entity(
    tableName = "exercises_todo",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["workout_template_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workout_template_id"), Index("exercise_id")]
)
//TODO реализовать режим суперсета (тут скорее визуальное изменение, нужно показывать не конкретное упражнение, а сразу группу
data class ExerciseToDo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "workout_template_id") val workoutTemplateId: Int,
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,
    val duration: Int? = null,
    val order: Int,
    @ColumnInfo(name = "training_mode") val trainingMode: TrainingMode,
    @ColumnInfo(name = "custom_training_mode_name") val customTrainingModeName: String? = null,

    // Настройки упражнения для включения режима периодизации
    @ColumnInfo(name = "periodization_enabled") val periodizationEnabled: Boolean = false,
    @ColumnInfo(name = "mode_a") val modeA: TrainingMode? = null,
    @ColumnInfo(name = "mode_b") val modeB: TrainingMode? = null,
    @ColumnInfo(name = "sets_a") val setsA: Int? = null,
    @ColumnInfo(name = "reps_a") val repsA: Int? = null,
    @ColumnInfo(name = "weight_a") val weightA: Double? = null,
    @ColumnInfo(name = "sets_b") val setsB: Int? = null,
    @ColumnInfo(name = "reps_b") val repsB: Int? = null,
    @ColumnInfo(name = "weight_b") val weightB: Double? = null,
)
