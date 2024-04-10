package com.example.project2_adomingo.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Relation
import androidx.room.TypeConverters
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration

// Only one user for right now (anonymous app)
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    var username: String = "user", // Should be a primary key?
    val nextWorkoutIndex: Int = 0
)

// 1:N, a user has many workout dates
// (in either their workout schedule or workout history)
@Entity(tableName = "schedule_dates")
data class ScheduleDate(
    @PrimaryKey(autoGenerate = true) val dateId: Long = 0,
    val weekday: DayOfWeek // Should be a primary key?
)

// 1:N, a user has many workouts (in a workout plan)
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val workoutId: Long = 0,
    var workoutName: String,
    var description: String,
    val listOrder: Int // should be a primary key?

)

// Available exercises (independent of workouts)
// 1:N, a user has many exercises to choose from
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
    primaryKeys = ["workoutExerciseId", "workoutId", "exerciseId"]
)
//@TypeConverters(Converters::class)
data class WorkoutExercise(
    val workoutExerciseId: Long,
    @ColumnInfo(index = true)
    val workoutId: Long,
    @ColumnInfo(index = true)
    val exerciseId: Long,
    @Embedded(prefix = "exercise_") // or could user exerciseId, but harder to query later
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val breakTime: Int, // ex. 30.seconds, needed TypeConverter
    val listOrder: Int
){
    constructor() : this(0, 0, 0, Exercise(0,"", Equipment.MACHINE, MuscleGroup.CHEST), 0, 0, 0.0, 0, 0)
}

// Workouts with a list of exercises and a list of dates
// Embedded for efficient querying
data class WorkoutPlan(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutId",
    )
    var exercises: List<WorkoutExercise>, // 1:N, each workout has many exercises
)

// 1:N, a user has many workout dates (instances) in their history
@Entity(tableName = "workout_history_dates")
@TypeConverters(Converters::class)
data class WorkoutHistoryDate(
    @PrimaryKey(autoGenerate = true) val workoutHistoryDateId: Long = 0,
    val date: LocalDate // Needed TypeConverter
)

// 1:N, a user has many exercises in their workout sessions in history
@Entity(tableName = "exercise_history",
    foreignKeys = [
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
    primaryKeys = ["exerciseHistoryId", "workoutHistoryDateId", "workoutExerciseId"]
)
data class ExerciseHistory(
    val exerciseHistoryId: Long = 0,
    val workoutHistoryDateId: Long,
    val workoutExerciseId: Long
    //@Embedded // easier join
    //val workoutExercise: WorkoutExercise
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
