package com.example.project2_adomingo

import com.example.project2_adomingo.database.Equipment
import com.example.project2_adomingo.database.Exercise
import com.example.project2_adomingo.database.MuscleGroup
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.Workout
import com.example.project2_adomingo.database.WorkoutExercise
import com.example.project2_adomingo.database.WorkoutPlan
import java.time.DayOfWeek
import kotlin.time.Duration.Companion.seconds

val defaultUser = User(
    userId = 0,
    username = "user"
)

val PPLSchedule: List<ScheduleDate> = listOf(
    ScheduleDate(
        dateId = 0,
        weekday = DayOfWeek.MONDAY
    ),
    ScheduleDate(
        dateId = 1,
        weekday = DayOfWeek.TUESDAY
    ),
    ScheduleDate(
        dateId = 2,
        weekday = DayOfWeek.WEDNESDAY
    ),
    ScheduleDate(
        dateId = 3,
        weekday = DayOfWeek.THURSDAY
    ),
    ScheduleDate(
        dateId = 4,
        weekday = DayOfWeek.FRIDAY
    ),
    ScheduleDate(
        dateId = 5,
        weekday = DayOfWeek.SATURDAY
    )
)

val pushExercises: List<Exercise> = listOf(
    Exercise(
        exerciseId = 0,
        exerciseName = "Bench Press",
        equipment = Equipment.BARBELL,
        muscleGroup = MuscleGroup.CHEST
    ),
    Exercise(
        exerciseId = 1,
        exerciseName = "Shoulder Press",
        equipment = Equipment.MACHINE,
        muscleGroup = MuscleGroup.SHOULDERS
    ),
    Exercise(
        exerciseId = 2,
        exerciseName = "Dumbbell Flyes",
        equipment = Equipment.DUMBELL,
        muscleGroup = MuscleGroup.CHEST
    )
)

val pullExercises: List<Exercise> = listOf(
    Exercise(
        exerciseId = 3,
        exerciseName = "Barbell Row",
        equipment = Equipment.BARBELL,
        muscleGroup = MuscleGroup.BACK
    ),
    Exercise(
        exerciseId = 4,
        exerciseName = "Lat Pull down",
        equipment = Equipment.MACHINE,
        muscleGroup = MuscleGroup.BACK
    ),
    Exercise(
        exerciseId = 5,
        exerciseName = "Dumbbell Shrug",
        equipment = Equipment.DUMBELL,
        muscleGroup = MuscleGroup.SHOULDERS
    )
)

val legExercises: List<Exercise> = listOf(
    Exercise(
        exerciseId = 6,
        exerciseName = "Barbell Squat",
        equipment = Equipment.BARBELL,
        muscleGroup = MuscleGroup.LEGS
    ),
    Exercise(
        exerciseId = 7,
        exerciseName = "Romanian Deadlift",
        equipment = Equipment.BARBELL,
        muscleGroup = MuscleGroup.LEGS
    ),
    Exercise(
        exerciseId = 8,
        exerciseName = "Leg Press",
        equipment = Equipment.MACHINE,
        muscleGroup = MuscleGroup.LEGS
    )
)

val PPLWorkouts: List<Workout> = listOf(
    Workout(
        workoutId = 0,
        workoutName = "Push",
        description = "Chest, Shoulder and Triceps",
        listOrder = 0
    ),
    Workout(
        workoutId = 1,
        workoutName = "Pull",
        description = "Back, Biceps, and Abs",
        listOrder = 1
    ),
    Workout(
        workoutId = 2,
        workoutName = "Leg",
        description = "Quads, Hams, Glutes, and Calves",
        listOrder = 2
    ),
)

val pushWorkoutExercises: List<WorkoutExercise> = listOf(
    WorkoutExercise(
        workoutExerciseId = 0,
        workoutId = 0,
        exerciseId = pushExercises[0].exerciseId,
        exercise = pushExercises[0],
        sets = 4,
        reps = 10, // 6-10
        weight = 135.0,
        breakTime = 90,
        listOrder = 0
    ),
    WorkoutExercise(
        workoutExerciseId = 1,
        workoutId = 0,
        exerciseId = pushExercises[1].exerciseId,
        exercise = pushExercises[1],
        sets = 3,
        reps = 12, // 10-12
        weight = 50.0,
        breakTime = 90,
        listOrder = 1
    ),
    WorkoutExercise(
        workoutExerciseId = 2,
        workoutId = 0,
        exerciseId = pushExercises[2].exerciseId,
        exercise = pushExercises[2],
        sets = 3,
        reps = 15, // 12-15
        weight = 100.0,
        breakTime = 90,
        listOrder = 2
    )
)

val pullWorkoutExercises: List<WorkoutExercise> = listOf(
    WorkoutExercise(
        workoutExerciseId = 3,
        workoutId = 1,
        exerciseId = pullExercises[0].exerciseId,
        exercise = pullExercises[0],
        sets = 3,
        reps = 8, // 6-8
        weight = 135.0,
        breakTime = 90,
        listOrder = 0
    ),
    WorkoutExercise(
        workoutExerciseId = 4,
        workoutId = 1,
        exerciseId = pullExercises[1].exerciseId,
        exercise = pullExercises[1],
        sets = 3,
        reps = 10, // 8-10
        weight = 120.0,
        breakTime = 90,
        listOrder = 1
    ),
    WorkoutExercise(
        workoutExerciseId = 5,
        workoutId = 1,
        exerciseId = pullExercises[2].exerciseId,
        exercise = pullExercises[2],
        sets = 2,
        reps = 15, // 12-15
        weight = 75.0,
        breakTime = 90,
        listOrder = 2
    )
)

val legWorkoutExercises: List<WorkoutExercise> = listOf(
    WorkoutExercise(
        workoutExerciseId = 6,
        workoutId = 2,
        exerciseId = legExercises[0].exerciseId,
        exercise = legExercises[0],
        sets = 3,
        reps = 8, // 6-8
        weight = 135.0,
        breakTime = 90,
        listOrder = 0
    ),
    WorkoutExercise(
        workoutExerciseId = 7,
        workoutId = 2,
        exerciseId = legExercises[1].exerciseId,
        exercise = legExercises[1],
        sets = 3,
        reps = 12, // 10-12
        weight = 135.0,
        breakTime = 90,
        listOrder = 1
    ),
    WorkoutExercise(
        workoutExerciseId = 8,
        workoutId = 2,
        exerciseId = legExercises[2].exerciseId,
        exercise = legExercises[2],
        sets = 3,
        reps = 10, // 8-10
        weight = 135.0,
        breakTime = 90,
        listOrder = 2
    )
)

val PPLWorkoutPlans: List<WorkoutPlan> = listOf(
    WorkoutPlan(
        workout = PPLWorkouts[0],
        exercises = pushWorkoutExercises
    ),
    WorkoutPlan(
        workout = PPLWorkouts[1],
        exercises = pullWorkoutExercises
    ),
    WorkoutPlan(
        workout = PPLWorkouts[2],
        exercises = legWorkoutExercises
    )
)
