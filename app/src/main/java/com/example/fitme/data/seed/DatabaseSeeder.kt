package com.example.fitme.data.seed

import androidx.room.withTransaction
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.ExerciseToDo
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.entities.WorkoutTemplate

class DatabaseSeeder(
    private val db: AppDatabase,
    private val seedData: DefaultSeedData = DefaultSeedData,
) {

    suspend fun seedIfNeeded() = db.withTransaction {
        val exerciseIdsByName = seedExercises()
        seedWorkoutTemplates(exerciseIdsByName)
        if (db.workoutPlanDao().getPlanCount() == 0) {
            seedPlans(exerciseIdsByName)
        }
    }

    private suspend fun seedExercises(): Map<String, Int> {
        val idsByName = mutableMapOf<String, Int>()
        seedData.exercises.forEach { exercise ->
            val existing = db.exerciseDao().getExerciseByName(exercise.name)
            val exerciseId = existing?.id ?: db.exerciseDao().insertExercise(
                exercise.copy(id = 0, isActive = true, isBuiltIn = true)
            ).toInt()
            idsByName[exercise.name] = exerciseId
        }
        return idsByName
    }

    private suspend fun seedWorkoutTemplates(exerciseIdsByName: Map<String, Int>) {
        if (db.workoutPlanDao().getBuiltInWorkoutTemplateCount() > 0) return
        seedData.workoutTemplates.forEachIndexed { templateIndex, seedTemplate ->
            val templateId = db.workoutPlanDao().insertWorkoutTemplate(
                WorkoutTemplate(
                    name = seedTemplate.name,
                    order = templateIndex + 1,
                    planId = null,
                    isBuiltIn = true,
                )
            ).toInt()
            seedExercisesToDo(
                templateId = templateId,
                exercises = seedTemplate.exercises,
                exerciseIdsByName = exerciseIdsByName,
            )
        }
    }

    private suspend fun seedPlans(exerciseIdsByName: Map<String, Int>) {
        seedData.plans.forEach { seedPlan ->
            val planId = db.workoutPlanDao().insertPlan(
                Plan(name = seedPlan.name, isActive = true)
            ).toInt()

            seedPlan.templates.forEachIndexed { templateIndex, seedTemplate ->
                val templateId = db.workoutPlanDao().insertWorkoutTemplate(
                    WorkoutTemplate(
                        name = seedTemplate.name,
                        order = templateIndex + 1,
                        planId = planId,
                    )
                ).toInt()

                seedExercisesToDo(
                    templateId = templateId,
                    exercises = seedTemplate.exercises,
                    exerciseIdsByName = exerciseIdsByName,
                )
            }
        }
    }

    private suspend fun seedExercisesToDo(
        templateId: Int,
        exercises: List<SeedExerciseToDo>,
        exerciseIdsByName: Map<String, Int>,
    ) {
        exercises.forEachIndexed { exerciseIndex, seedExercise ->
            val exerciseId = requireNotNull(exerciseIdsByName[seedExercise.exerciseName]) {
                "Seed exercise not found: ${seedExercise.exerciseName}"
            }
            db.exerciseToDoDao().insertExerciseToDo(
                ExerciseToDo(
                    exerciseId = exerciseId,
                    workoutTemplateId = templateId,
                    sets = seedExercise.sets,
                    reps = seedExercise.reps,
                    weight = seedExercise.weight,
                    duration = seedExercise.duration,
                    order = exerciseIndex + 1,
                    trainingMode = seedExercise.trainingMode,
                )
            )
        }
    }
}
