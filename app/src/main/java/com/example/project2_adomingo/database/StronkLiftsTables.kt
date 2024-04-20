package com.example.project2_adomingo.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation
import androidx.room.TypeConverters
import java.time.DayOfWeek
import java.time.LocalDate

// Only one user for right now (anonymous user)
// May or may not have a current workout going on for them
@Entity(tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutHistory::class,
            parentColumns = ["workoutHistoryId"],
            childColumns = ["startedWorkoutHistoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class User(
    @PrimaryKey val userId: Long = 0,
    var username: String = "user",
    val nextWorkoutIndex: Int = 0,
    @ColumnInfo(index = true)
    val startedWorkoutHistoryId: Long? = null // must persist even if workout plan is deleted
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
    @PrimaryKey val workoutId: Long,
    var workoutName: String,
    //var description: String,
    val listOrder: Int // should be a primary key?

)

// Available exercises (independent of workouts)
// 1:N, a user has many exercises to choose from
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val exerciseId: Long,
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
    ]
)
//@TypeConverters(Converters::class)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val workoutExerciseId: Long,
    @ColumnInfo(index = true)
    val workoutId: Long,
    @ColumnInfo(index = true)
    val exerciseId: Long,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val breakTime: Int, // ex. 30.seconds, needed TypeConverter
    val listOrder: Int
)

// Workout with the Workout Exercise
data class WorkoutExercisePartial(
    @Embedded val workoutExercise: Workout,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExercise>
)

// Workout Exercise with the Exercise
data class WorkoutExerciseComplete(
    @Embedded val workoutExercise: WorkoutExercise,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val exercise: Exercise
)

// Workouts with the workout exercises with its exercises
// Joined Workout and WorkoutExercise (joined with Exercise)
data class WorkoutPlan(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutId",
        entity = WorkoutExercise::class
    )
    var exercises: List<WorkoutExerciseComplete>, // 1:N, each workout has many exercises
)

// 1:N, a user has many workout dates (instances) in their history
@Entity(tableName = "workout_history")
@TypeConverters(Converters::class)
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val workoutHistoryId: Long = 0,
    val date: LocalDate, // Needed TypeConverter
    val workoutName: String

)

// 1:N, a user has many exercises in their workout sessions in history
@Entity(tableName = "exercise_history",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutHistory::class,
            parentColumns = ["workoutHistoryId"],
            childColumns = ["workoutHistoryId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    //primaryKeys = ["exerciseHistoryId", "workoutHistoryId"]
)
data class ExerciseHistory(
    @PrimaryKey(autoGenerate = true) val exerciseHistoryId: Long = 0,
    @ColumnInfo(index = true)
    val workoutHistoryId: Long,
    // Copy of Workout Exercise (Plan) info
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weight: Double
)

// 1:N, an exercise has a number of sets, each set a record number of reps
@Entity(tableName = "exercise_set_history",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutHistory::class,
            parentColumns = ["workoutHistoryId"],
            childColumns = ["workoutHistoryId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseHistory::class,
            parentColumns = ["exerciseHistoryId"],
            childColumns = ["exerciseHistoryId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exerciseSetHistoryId", "setNumber"], // set order must be unique to set history
        unique = true)],
    //primaryKeys = ["exerciseSetHistoryId", "workoutHistoryId", "exerciseHistoryId"]
)
data class ExerciseSetHistory(
    @PrimaryKey(autoGenerate = true)
    val exerciseSetHistoryId: Long = 0,
    @ColumnInfo(index = true)
    val workoutHistoryId: Long,
    @ColumnInfo(index = true)
    val exerciseHistoryId: Long,
    val setNumber: Int, // order by this
    val repsDone: Int,
)

// Exercise History with a history of reps for each exercise set
// Joined ExerciseHistory with ExerciseSetHistory
// TODO: Fix this. (error: "class must be either @Entity or @DatabaseView.")
//data class ExerciseHistoryComplete(
//    @Embedded val exercise: ExerciseHistory,
//    @Relation(
//        parentColumn = "exerciseHistoryId",
//        entityColumn = "exerciseHistoryId"
//    )
//    val setsXreps: List<ExerciseSetHistory> // size of sets, need to order by set number
//)

// Workout History with a history of exercises (no history of reps)
// Joined WorkoutHistory with ExerciseHistory
// Used for getting started Workout info for Home Activity
data class WorkoutHistoryPartial(
    @Embedded val workout: WorkoutHistory,
    @Relation(
        parentColumn = "workoutHistoryId",
        entityColumn = "workoutHistoryId",
    )
    val exercises: List<ExerciseHistory>, // 1:N, each workout history has many exercise histories
)

// Workout History with a history of exercises, with a history of reps in each set
// Joined WorkoutHistory with ExerciseHistory (joined with ExerciseSetHistory)
// Used for reloading workout history info for started workout in Workout Activity
// TODO: Fix this. See ExerciseHistoryComplete.
//data class WorkoutHistoryComplete(
//    @Embedded val workout: WorkoutHistory,
//    @Relation(
//        parentColumn = "workoutHistoryId",
//        entityColumn = "workoutHistoryId",
//    )
//    val exercises: List<ExerciseHistoryComplete>, // 1:N, each workout history had many exercise histories (with many rep histories)
//)

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
