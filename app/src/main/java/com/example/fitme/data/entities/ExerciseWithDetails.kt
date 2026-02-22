import androidx.room.Embedded
import androidx.room.Relation
import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.ExerciseToDo

data class ExerciseWithDetails(
    @Embedded val exerciseToDo: ExerciseToDo,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "id"
    )
    val exercise: Exercise
)