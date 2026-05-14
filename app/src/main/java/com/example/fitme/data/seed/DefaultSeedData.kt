package com.example.fitme.data.seed

import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import com.example.fitme.data.entities.enums.TrainingMode

object DefaultSeedData {

    val exercises = listOf(
        Exercise(
            name = "Жим лежа",
            bodyRegion = BodyRegion.CHEST,
            muscle = MuscleGroup.MIDDLE_CHEST,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Жим гантелей на наклонной скамье",
            bodyRegion = BodyRegion.CHEST,
            muscle = MuscleGroup.UPPER_CHEST,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Подтягивания",
            bodyRegion = BodyRegion.BACK,
            muscle = MuscleGroup.LATS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Тяга штанги в наклоне",
            bodyRegion = BodyRegion.BACK,
            muscle = MuscleGroup.LATS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Жим стоя",
            bodyRegion = BodyRegion.SHOULDERS,
            muscle = MuscleGroup.FRONT_DELTS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Подъем гантелей в стороны",
            bodyRegion = BodyRegion.SHOULDERS,
            muscle = MuscleGroup.SIDE_DELTS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Приседания со штангой",
            bodyRegion = BodyRegion.LEGS,
            muscle = MuscleGroup.QUADS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Румынская тяга",
            bodyRegion = BodyRegion.LEGS,
            muscle = MuscleGroup.HAMSTRINGS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Выпады",
            bodyRegion = BodyRegion.LEGS,
            muscle = MuscleGroup.QUADS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Подъемы на икры стоя",
            bodyRegion = BodyRegion.CALVES,
            muscle = MuscleGroup.CALVES,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Сгибание рук со штангой",
            bodyRegion = BodyRegion.ARMS,
            muscle = MuscleGroup.BICEPS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Французский жим",
            bodyRegion = BodyRegion.ARMS,
            muscle = MuscleGroup.TRICEPS,
            isBuiltIn = true,
        ),
        Exercise(
            name = "Планка",
            bodyRegion = BodyRegion.CORE,
            muscle = MuscleGroup.ABS,
            isBuiltIn = true,
        ),
    )

    private val fullBodyA = SeedWorkoutTemplate(
        name = "Фулбади A",
        exercises = listOf(
            SeedExerciseToDo("Приседания со штангой", sets = 3, reps = 8),
            SeedExerciseToDo("Жим лежа", sets = 3, reps = 8),
            SeedExerciseToDo("Тяга штанги в наклоне", sets = 3, reps = 10),
            SeedExerciseToDo("Планка", sets = 3, reps = 1, duration = 60, trainingMode = TrainingMode.NONE),
        ),
    )

    private val fullBodyB = SeedWorkoutTemplate(
        name = "Фулбади B",
        exercises = listOf(
            SeedExerciseToDo("Румынская тяга", sets = 3, reps = 8),
            SeedExerciseToDo("Жим стоя", sets = 3, reps = 8),
            SeedExerciseToDo("Подтягивания", sets = 3, reps = 6),
            SeedExerciseToDo("Выпады", sets = 3, reps = 10),
        ),
    )

    private val upper = SeedWorkoutTemplate(
        name = "Верх",
        exercises = listOf(
            SeedExerciseToDo("Жим лежа", sets = 4, reps = 6, trainingMode = TrainingMode.STRENGTH),
            SeedExerciseToDo("Подтягивания", sets = 4, reps = 6, trainingMode = TrainingMode.STRENGTH),
            SeedExerciseToDo("Жим гантелей на наклонной скамье", sets = 3, reps = 10),
            SeedExerciseToDo("Подъем гантелей в стороны", sets = 3, reps = 12),
            SeedExerciseToDo("Сгибание рук со штангой", sets = 3, reps = 10),
            SeedExerciseToDo("Французский жим", sets = 3, reps = 10),
        ),
    )

    private val lower = SeedWorkoutTemplate(
        name = "Низ",
        exercises = listOf(
            SeedExerciseToDo("Приседания со штангой", sets = 4, reps = 6, trainingMode = TrainingMode.STRENGTH),
            SeedExerciseToDo("Румынская тяга", sets = 4, reps = 8),
            SeedExerciseToDo("Выпады", sets = 3, reps = 10),
            SeedExerciseToDo("Подъемы на икры стоя", sets = 4, reps = 12),
            SeedExerciseToDo("Планка", sets = 3, reps = 1, duration = 60, trainingMode = TrainingMode.NONE),
        ),
    )

    val workoutTemplates = listOf(
        fullBodyA,
        fullBodyB,
        upper,
        lower,
    )

    val plans = listOf(
        SeedPlan(
            name = "Фулбади для новичка",
            templates = listOf(
                fullBodyA,
                fullBodyB,
            ),
        ),
        SeedPlan(
            name = "Верх / низ",
            templates = listOf(
                upper,
                lower,
            ),
        ),
    )
}

data class SeedPlan(
    val name: String,
    val templates: List<SeedWorkoutTemplate>,
)

data class SeedWorkoutTemplate(
    val name: String,
    val exercises: List<SeedExerciseToDo>,
)

data class SeedExerciseToDo(
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,
    val duration: Int? = null,
    val trainingMode: TrainingMode = TrainingMode.HYPERTROPHY,
)
