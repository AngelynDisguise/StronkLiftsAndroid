package com.example.project2_adomingo.database

import java.time.DayOfWeek

const val DEFAULT_USER_ID: Long = 0

val defaultUser = User(
    userId = DEFAULT_USER_ID,
    username = "bruh"
)

val PPLSchedule: List<ScheduleDate> = listOf(
    ScheduleDate(
        weekday = DayOfWeek.MONDAY
    ),
    ScheduleDate(
        weekday = DayOfWeek.TUESDAY
    ),
    ScheduleDate(
        weekday = DayOfWeek.WEDNESDAY
    ),
    ScheduleDate(
        weekday = DayOfWeek.THURSDAY
    ),
    ScheduleDate(
        weekday = DayOfWeek.FRIDAY
    ),
    ScheduleDate(
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
        //description = "Chest, Shoulder and Triceps",
        listOrder = 0
    ),
    Workout(
        workoutId = 1,
        workoutName = "Pull",
        //description = "Back, Biceps, and Abs",
        listOrder = 1
    ),
    Workout(
        workoutId = 2,
        workoutName = "Leg",
        //description = "Quads, Hams, Glutes, and Calves",
        listOrder = 2
    ),
)

val pushWorkoutExercises: List<WorkoutExercise> = listOf(
    WorkoutExercise(
        workoutExerciseId = 0,
        workoutId = 0,
        exerciseId = pushExercises[0].exerciseId,
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
        sets = 3,
        reps = 10, // 8-10
        weight = 135.0,
        breakTime = 90,
        listOrder = 2
    )
)

val pushWorkoutExercisesComplete: List<WorkoutExerciseComplete> = listOf(
    WorkoutExerciseComplete(
        workoutExercise = pushWorkoutExercises[0],
        exercise = pushExercises[0]
    ),
    WorkoutExerciseComplete(
        workoutExercise = pushWorkoutExercises[1],
        exercise = pushExercises[1]
    ),
    WorkoutExerciseComplete(
        workoutExercise = pushWorkoutExercises[2],
        exercise = pushExercises[2]
    )
)

val pullWorkoutExercisesComplete: List<WorkoutExerciseComplete> = listOf(
    WorkoutExerciseComplete(
        workoutExercise = pullWorkoutExercises[0],
        exercise = pullExercises[0]
    ),
    WorkoutExerciseComplete(
        workoutExercise = pullWorkoutExercises[1],
        exercise = pullExercises[1]
    ),
    WorkoutExerciseComplete(
        workoutExercise = pullWorkoutExercises[2],
        exercise = pullExercises[2]
    )
)

val legWorkoutExercisesComplete: List<WorkoutExerciseComplete> = listOf(
    WorkoutExerciseComplete(
        workoutExercise = legWorkoutExercises[0],
        exercise = legExercises[0]
    ),
    WorkoutExerciseComplete(
        workoutExercise = legWorkoutExercises[1],
        exercise = legExercises[1]
    ),
    WorkoutExerciseComplete(
        workoutExercise = pushWorkoutExercises[2],
        exercise = legExercises[2]
    )
)

val PPLWorkoutPlans: List<WorkoutPlan> = listOf(
    WorkoutPlan(
        workout = PPLWorkouts[0],
        exercises = pushWorkoutExercisesComplete
    ),
    WorkoutPlan(
        workout = PPLWorkouts[1],
        exercises = pullWorkoutExercisesComplete
    ),
    WorkoutPlan(
        workout = PPLWorkouts[2],
        exercises = legWorkoutExercisesComplete
    )
)
