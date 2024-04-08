package com.example.project2_adomingo.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.Relation
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val workoutId: Long = 0,
    //val userId: Long,
    var workoutName: String,
    var description: String
)

// 1:N Workout-Date junction table: Workouts have many dates
@Entity(tableName = "dates",
    foreignKeys = [ForeignKey(
        entity = Workout::class,
        parentColumns = ["workoutId"],
        childColumns = ["workoutId"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )]
)
data class Date(
    @PrimaryKey(autoGenerate = true) val dateId: Long = 0,
    val workoutId: Long,
    val date: DayOfWeek
)

// Exercises independent of Workouts
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val exerciseId: Long = 0,
    val exerciseName: String,
    val equipment: Equipment,
    val muscleGroup: MuscleGroup
)

// 1:N Workout-Exercise junction table: Workouts have many exercises
@Entity(tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["workoutId", "exerciseId"]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val workoutExerciseId: Long = 0,
    val workoutId: Long,
    //val exerciseId: Long,
    @Embedded
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val breakTime: Duration // ex. 30.seconds
)

// Workouts with a list of exercises and a list of dates
// Embedded for efficient querying
data class WorkoutPlan(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutId",
    )
    val exercises: List<WorkoutExercise>, // 1:N, each workout has many exercises
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutId" // 1:N, each workout has many dates
    )
    val dates: List<Date>
)

//
@Entity(tableName = "workout_history",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["workoutId"]
)
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val workoutHistoryId: Long = 0,
    val workoutId: Long,
    val date: LocalDate
)

//
@Entity(tableName = "exercise_history",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutHistory::class,
            parentColumns = ["workoutHistoryId"],
            childColumns = ["workoutHistoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["workoutExerciseId"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["workoutHistoryId", "workoutExerciseId"]
)
data class ExerciseHistory(
    @PrimaryKey(autoGenerate = true) val exerciseHistoryId: Long = 0,
    val workoutHistoryId: Long,
    @Embedded
    val workoutExercise: WorkoutExercise
)

data class WorkoutSession(
    @Embedded val workout: WorkoutHistory,
    @Relation(
        parentColumn = "workoutHistoryId",
        entityColumn = "workoutHistoryId",
    )
    val exercises: List<ExerciseHistory>, // 1:N, each workout history had many exercise histories
)

enum class Equipment(val type: String) {
    BARBELL("Barbell"),
    DUMBELL("Dumbell"),
    BODYWEIGHT("Body Weight"),
    MACHINE("Machine"),
    OTHER("OTHER")
}

enum class MuscleGroup(val muscle: String) {
    ARMS("Arms"),
    BACK("Back"),
    CHEST("Chest"),
    LEGS("Legs"),
    SHOULDERS("Shoulders"),
    ABS("Abs")
}
