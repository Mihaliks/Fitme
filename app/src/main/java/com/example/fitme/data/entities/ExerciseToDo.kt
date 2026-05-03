package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitme.data.entities.enums.TrainingMode
import java.time.LocalDate

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

//TODO реализовать создание тренировки,  добавить режим "периодизации"
//TODO реализовать режим суперсета (тут скорее визуальное изменение, нужно показывать не конкретное упражнение, а сразу группу
/*тут важно придумать смысл автогенерации бесконечных тренировок, сначала создается программа тренировок
она заполняется списком ExerciseToDo выбранным, и в дальнейшем
 */
data class ExerciseToDo(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "workout_template_id") val workoutTemplateId: Int,
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,
    val duration: Int? = null,
    val order: Int,
    val trainingMode: TrainingMode,
    val customTrainingModeName: String? = null
)