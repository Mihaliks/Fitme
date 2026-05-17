package com.example.fitme.data.seed

import com.example.fitme.data.entities.Exercise
import com.example.fitme.data.entities.enums.BodyRegion
import com.example.fitme.data.entities.enums.MuscleGroup
import com.example.fitme.data.entities.enums.TrainingMode

object DefaultSeedData {

    val exercises = listOf(
        // Chest
        Exercise(name = "Жим штанги лежа", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.MIDDLE_CHEST, isBuiltIn = true),
        Exercise(name = "Жим гантелей лежа", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.MIDDLE_CHEST, isBuiltIn = true),
        Exercise(name = "Жим гантелей на наклонной скамье", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.UPPER_CHEST, isBuiltIn = true),
        Exercise(name = "Жим штанги на наклонной скамье", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.UPPER_CHEST, isBuiltIn = true),
        Exercise(name = "Жим в Смите", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.MIDDLE_CHEST, isBuiltIn = true),
        Exercise(name = "Жим в тренажере на среднюю грудь", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.MIDDLE_CHEST, isBuiltIn = true),
        Exercise(name = "Отжимания на брусьях", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.LOWER_CHEST, isBuiltIn = true),
        Exercise(name = "Отжимания от пола", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.MIDDLE_CHEST, isBuiltIn = true),
        Exercise(name = "Сведение рук в кроссовере", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.MIDDLE_CHEST, isBuiltIn = true),
        Exercise(name = "Бабочка", bodyRegion = BodyRegion.CHEST, muscle = MuscleGroup.MIDDLE_CHEST, isBuiltIn = true),

        // Back
        Exercise(name = "Подтягивания широким хватом", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.LATS, isBuiltIn = true),
        Exercise(name = "Подтягивания обратным хватом", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.LATS, isBuiltIn = true),
        Exercise(name = "Тяга верхнего блока", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.LATS, isBuiltIn = true),
        Exercise(name = "Тяга горизонтального блока", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.MID_BACK, isBuiltIn = true),
        Exercise(name = "Тяга штанги в наклоне", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.MID_BACK, isBuiltIn = true),
        Exercise(name = "Тяга гантели одной рукой", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.LATS, isBuiltIn = true),
        Exercise(name = "Пуловер в кроссовере", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.LATS, isBuiltIn = true),
        Exercise(name = "Гиперэкстензия", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.LOWER_BACK, isBuiltIn = true),
        Exercise(name = "Шраги с гантелями", bodyRegion = BodyRegion.BACK, muscle = MuscleGroup.TRAPS, isBuiltIn = true),
        Exercise(name = "Фейс-пул", bodyRegion = BodyRegion.SHOULDERS, muscle = MuscleGroup.REAR_DELTS, isBuiltIn = true),

        // Shoulders
        Exercise(name = "Жим штанги стоя", bodyRegion = BodyRegion.SHOULDERS, muscle = MuscleGroup.FRONT_DELTS, isBuiltIn = true),
        Exercise(name = "Жим гантелей сидя", bodyRegion = BodyRegion.SHOULDERS, muscle = MuscleGroup.FRONT_DELTS, isBuiltIn = true),
        Exercise(name = "Подъем гантелей в стороны", bodyRegion = BodyRegion.SHOULDERS, muscle = MuscleGroup.SIDE_DELTS, isBuiltIn = true),
        Exercise(name = "Подъем гантелей перед собой", bodyRegion = BodyRegion.SHOULDERS, muscle = MuscleGroup.FRONT_DELTS, isBuiltIn = true),
        Exercise(name = "Разведение гантелей в наклоне", bodyRegion = BodyRegion.SHOULDERS, muscle = MuscleGroup.REAR_DELTS, isBuiltIn = true),
        Exercise(name = "Обратная бабочка", bodyRegion = BodyRegion.SHOULDERS, muscle = MuscleGroup.REAR_DELTS, isBuiltIn = true),

        // Arms
        Exercise(name = "Сгибание рук со штангой", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.BICEPS, isBuiltIn = true),
        Exercise(name = "Сгибание рук с гантелями", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.BICEPS, isBuiltIn = true),
        Exercise(name = "Сгибание рук на скамье Скотта", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.BICEPS, isBuiltIn = true),
        Exercise(name = "Молотковые сгибания", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.BRACHIALIS, isBuiltIn = true),
        Exercise(name = "Сгибание рук обратным хватом", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.BRACHIALIS, isBuiltIn = true),
        Exercise(name = "Французский жим", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.TRICEPS, isBuiltIn = true),
        Exercise(name = "Разгибание рук на блоке", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.TRICEPS, isBuiltIn = true),
        Exercise(name = "Разгибание рук с канатом из-за головы", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.TRICEPS, isBuiltIn = true),
        Exercise(name = "Жим узким хватом", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.TRICEPS, isBuiltIn = true),
        Exercise(name = "Разгибание гантели из-за головы", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.TRICEPS, isBuiltIn = true),
        Exercise(name = "Сгибание запястий", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.FOREARMS, isBuiltIn = true),
        Exercise(name = "Разгибание запястий", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.FOREARMS, isBuiltIn = true),
        Exercise(name = "Фермерская прогулка", bodyRegion = BodyRegion.ARMS, muscle = MuscleGroup.FOREARMS, isBuiltIn = true),

        // Core
        Exercise(name = "Планка", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.ABS, isBuiltIn = true),
        Exercise(name = "Боковая планка", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.OBLIQUES, isBuiltIn = true),
        Exercise(name = "Скручивания", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.ABS, isBuiltIn = true),
        Exercise(name = "Обратные скручивания", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.ABS, isBuiltIn = true),
        Exercise(name = "Подъем ног лежа", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.ABS, isBuiltIn = true),
        Exercise(name = "Подъем ног в висе", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.ABS, isBuiltIn = true),
        Exercise(name = "Русские скручивания", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.OBLIQUES, isBuiltIn = true),
        Exercise(name = "Дэд баг", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.ABS, isBuiltIn = true),
        Exercise(name = "Птица-собака", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.LOWER_BACK, isBuiltIn = true),
        Exercise(name = "Альпинист", bodyRegion = BodyRegion.CORE, muscle = MuscleGroup.ABS, isBuiltIn = true),

        // Glutes and legs
        Exercise(name = "Приседания со штангой", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.QUADS, isBuiltIn = true),
        Exercise(name = "Фронтальные приседания", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.QUADS, isBuiltIn = true),
        Exercise(name = "Жим ногами", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.QUADS, isBuiltIn = true),
        Exercise(name = "Болгарские сплит-приседания", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.QUADS, isBuiltIn = true),
        Exercise(name = "Выпады", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.QUADS, isBuiltIn = true),
        Exercise(name = "Разгибание ног в тренажере", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.QUADS, isBuiltIn = true),
        Exercise(name = "Румынская тяга", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.HAMSTRINGS, isBuiltIn = true),
        Exercise(name = "Румынская тяга с гантелями", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.HAMSTRINGS, isBuiltIn = true),
        Exercise(name = "Сгибание ног лежа", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.HAMSTRINGS, isBuiltIn = true),
        Exercise(name = "Сгибание ног сидя", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.HAMSTRINGS, isBuiltIn = true),
        Exercise(name = "Ягодичный мост", bodyRegion = BodyRegion.GLUTES, muscle = MuscleGroup.GLUTE_MAXIMUS, isBuiltIn = true),
        Exercise(name = "Хип-траст", bodyRegion = BodyRegion.GLUTES, muscle = MuscleGroup.GLUTE_MAXIMUS, isBuiltIn = true),
        Exercise(name = "Отведение ноги в кроссовере", bodyRegion = BodyRegion.GLUTES, muscle = MuscleGroup.GLUTE_MEDIUS, isBuiltIn = true),
        Exercise(name = "Разведение ног в тренажере", bodyRegion = BodyRegion.GLUTES, muscle = MuscleGroup.GLUTE_MEDIUS, isBuiltIn = true),
        Exercise(name = "Сведение ног в тренажере", bodyRegion = BodyRegion.LEGS, muscle = MuscleGroup.ADDUCTORS, isBuiltIn = true),
        Exercise(name = "Подъемы на икры стоя", bodyRegion = BodyRegion.CALVES, muscle = MuscleGroup.CALVES, isBuiltIn = true),
        Exercise(name = "Подъемы на икры сидя", bodyRegion = BodyRegion.CALVES, muscle = MuscleGroup.CALVES, isBuiltIn = true),

        // Cardio and full body
        Exercise(name = "Беговая дорожка", bodyRegion = BodyRegion.CARDIO, muscle = MuscleGroup.CARDIO, isBuiltIn = true),
        Exercise(name = "Велотренажер", bodyRegion = BodyRegion.CARDIO, muscle = MuscleGroup.CARDIO, isBuiltIn = true),
        Exercise(name = "Эллипсоид", bodyRegion = BodyRegion.CARDIO, muscle = MuscleGroup.CARDIO, isBuiltIn = true),
        Exercise(name = "Берпи", bodyRegion = BodyRegion.FULL_BODY, muscle = MuscleGroup.FULL_BODY, isBuiltIn = true),
    )

    private fun periodizedExercise(
        exerciseName: String,
        sets: Int = 3,
        reps: Int = 10,
        lowReps: Int = 8,
        highReps: Int = 15,
    ) = SeedExerciseToDo(
        exerciseName = exerciseName,
        sets = sets,
        reps = reps,
        trainingMode = TrainingMode.HYPERTROPHY,
        periodizationEnabled = true,
        modeA = TrainingMode.HYPERTROPHY,
        modeB = TrainingMode.ENDURANCE,
        setsA = sets,
        repsA = lowReps,
        setsB = sets,
        repsB = highReps,
    )

    private val fullBody = SeedWorkoutTemplate(
        name = "Фулбади для новичка",
        exercises = listOf(
            periodizedExercise("Жим ногами", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Сгибание ног лежа", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Жим гантелей на наклонной скамье", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Тяга верхнего блока", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Подъем гантелей в стороны", sets = 2, reps = 15, lowReps = 12, highReps = 20),
            SeedExerciseToDo("Планка", sets = 3, reps = 1, duration = 30, trainingMode = TrainingMode.NONE),
        ),
    )

    private val upperA = SeedWorkoutTemplate(
        name = "Верх A - грудь и руки",
        exercises = listOf(
            periodizedExercise("Жим гантелей на наклонной скамье", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Пуловер в кроссовере", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Бабочка", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Сгибание рук с гантелями", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Разгибание рук на блоке", reps = 12, lowReps = 10, highReps = 18),
        ),
    )

    private val lowerA = SeedWorkoutTemplate(
        name = "Низ A - ноги и плечи",
        exercises = listOf(
            periodizedExercise("Жим ногами", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Разгибание ног в тренажере", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Сгибание ног лежа", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Сгибание ног сидя", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Подъем гантелей в стороны", reps = 15, lowReps = 12, highReps = 20),
            periodizedExercise("Подъем гантелей перед собой", sets = 2, reps = 15, lowReps = 12, highReps = 20),
        ),
    )

    private val upperB = SeedWorkoutTemplate(
        name = "Верх B - спина и руки",
        exercises = listOf(
            periodizedExercise("Тяга верхнего блока", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Тяга горизонтального блока", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Жим в тренажере на среднюю грудь", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Сгибание рук на скамье Скотта", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Разгибание рук с канатом из-за головы", reps = 12, lowReps = 10, highReps = 18),
        ),
    )

    private val lowerB = SeedWorkoutTemplate(
        name = "Низ B - ноги и задние дельты",
        exercises = listOf(
            periodizedExercise("Жим ногами", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Разгибание ног в тренажере", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Сгибание ног лежа", reps = 12, lowReps = 10, highReps = 18),
            periodizedExercise("Румынская тяга с гантелями", reps = 10, lowReps = 8, highReps = 15),
            periodizedExercise("Подъем гантелей в стороны", reps = 15, lowReps = 12, highReps = 20),
            periodizedExercise("Обратная бабочка", reps = 15, lowReps = 12, highReps = 20),
        ),
    )

    private val coreA = SeedWorkoutTemplate(
        name = "Пресс 30 дней A - база",
        exercises = listOf(
            SeedExerciseToDo("Беговая дорожка", sets = 1, reps = 1, duration = 600, trainingMode = TrainingMode.NONE),
            SeedExerciseToDo("Планка", sets = 3, reps = 1, duration = 30, trainingMode = TrainingMode.NONE),
            SeedExerciseToDo("Скручивания", sets = 3, reps = 15),
            SeedExerciseToDo("Дэд баг", sets = 3, reps = 10),
            SeedExerciseToDo("Альпинист", sets = 3, reps = 20),
        ),
    )

    private val coreB = SeedWorkoutTemplate(
        name = "Пресс 30 дней B - косые",
        exercises = listOf(
            SeedExerciseToDo("Велотренажер", sets = 1, reps = 1, duration = 720, trainingMode = TrainingMode.NONE),
            SeedExerciseToDo("Боковая планка", sets = 3, reps = 1, duration = 25, trainingMode = TrainingMode.NONE),
            SeedExerciseToDo("Русские скручивания", sets = 3, reps = 16),
            SeedExerciseToDo("Обратные скручивания", sets = 3, reps = 12),
            SeedExerciseToDo("Птица-собака", sets = 3, reps = 10),
        ),
    )

    private val coreC = SeedWorkoutTemplate(
        name = "Пресс 30 дней C - кардио",
        exercises = listOf(
            SeedExerciseToDo("Эллипсоид", sets = 1, reps = 1, duration = 900, trainingMode = TrainingMode.NONE),
            SeedExerciseToDo("Берпи", sets = 4, reps = 8),
            SeedExerciseToDo("Подъем ног лежа", sets = 3, reps = 12),
            SeedExerciseToDo("Планка", sets = 3, reps = 1, duration = 45, trainingMode = TrainingMode.NONE),
            SeedExerciseToDo("Альпинист", sets = 3, reps = 24),
        ),
    )

    val workoutTemplates = listOf(
        fullBody,
        upperA,
        lowerA,
        upperB,
        lowerB,
        coreA,
        coreB,
        coreC,
    )

    val plans = listOf(
        SeedPlan(
            name = "Фулбади для новичка",
            templates = listOf(fullBody),
        ),
        SeedPlan(
            name = "Верх / низ",
            templates = listOf(upperA, lowerA, upperB, lowerB),
        ),
        SeedPlan(
            name = "Красивый пресс за 30 дней",
            templates = listOf(coreA, coreB, coreC),
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
    val periodizationEnabled: Boolean = false,
    val modeA: TrainingMode? = null,
    val modeB: TrainingMode? = null,
    val setsA: Int? = null,
    val repsA: Int? = null,
    val weightA: Double? = null,
    val setsB: Int? = null,
    val repsB: Int? = null,
    val weightB: Double? = null,
)
