package com.example.project2_adomingo.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Relation
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    var userName: String,
    var description: String
)

// 1:N, a user has many workouts (in a workout plan)
@Entity(tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["workoutId", "userId"]
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val workoutId: Long = 0,
    val userId: Long,
    var workoutName: String,
    var description: String
)

// 1:N, a user has many workout dates 
// (in either their workout schedule or workout history)
@Entity(tableName = "schedule_dates",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["dateId", "userId", "weekday"]
)
data class ScheduleDate(
    @PrimaryKey(autoGenerate = true) val dateId: Long = 0,
    val userId: Long,
    val weekday: DayOfWeek
)

// Users workout schedule (e.g. Monday, Wednesday, Friday)
// Determines order of workouts
// Embedded for efficient querying
//data class WorkoutSchedule(
//    @Embedded val workout: User, // Embedded for efficient querying
//    @Relation(
//        parentColumn = "userId",
//        entityColumn = "userId",
//    )
//    val exercises: List<ScheduleDate>, // 1:N, each user as many workout dates
//)

// Available exercises (independent of workouts)
// 1:N, a user has many exercises to choose from
@Entity(tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["exerciseId", "userId"]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val exerciseId: Long = 0,
    val userId: Long,
    val exerciseName: String,
    val equipment: Equipment,
    val muscleGroup: MuscleGroup
)

// 1:N Workout-Exercise junction table: Workouts have many exercises
@Entity(tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["userId", "workoutId", "exerciseId"]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val workoutExerciseId: Long = 0,
    val workoutId: Long,
    //val exerciseId: Long,
    @Embedded // or could user exerciseId, but harder to query later
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
)

// 1:N, a user has many workout dates (instances) in their history
@Entity(tableName = "workout_history_dates",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["workoutHistoryDateId, userId"]
)
data class WorkoutHistoryDate(
    @PrimaryKey(autoGenerate = true) val workoutHistoryDateId: Long = 0,
    val userId: Long,
    val date: LocalDate
)

// 1:N, a user has many exercises in their workout sessions in history
@Entity(tableName = "exercise_history",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutHistoryDate::class,
            parentColumns = ["workoutHistoryDateId"],
            childColumns = ["workoutHistoryDateId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["workoutExerciseId"],
            childColumns = ["workoutExerciseId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["exerciseHistoryId", "userId,", "workoutHistoryDateId", "workoutExerciseId"]
)
data class ExerciseHistory(
    @PrimaryKey(autoGenerate = true) val exerciseHistoryId: Long = 0,
    val userId: Long,
    val workoutHistoryDateId: Long,
    @Embedded // easier join
    val workoutExercise: WorkoutExercise
)

// Workout History with workout date and exercise history
// Embedded for efficient querying
data class WorkoutHistory(
    @Embedded val workout: WorkoutHistoryDate,
    @Relation(
        parentColumn = "workoutHistoryDateId",
        entityColumn = "workoutHistoryDateId",
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
