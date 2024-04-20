package com.example.project2_adomingo.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

/* TODO:
    - prepend internal methods with _
    - isolate DAO interfaces for User, Workout Plan, Schedule, and Exercise?
    - if multiple users, add userId check to all methods
 */

// NOTE: Only one user and one plan (default PPL) should exist for now.

@Dao
interface StronkLiftsDao {

    /* * * * * *  CREATE * * * * * */

    // USER

    // Add username
    // Note: only one user should exist for now, inserting will overwrite
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // WORKOUT PLAN

    // Add a workout, overwriting if the workout already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)


    // Insert an exercise for a workout, overwriting if the exercise already exists in the workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise)

    // EXERCISE
    @Insert(onConflict = OnConflictStrategy.IGNORE) // prevent overwrite
    suspend fun insertExercise(exercise: Exercise)

    // SCHEDULE

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScheduleDate(scheduleDate: ScheduleDate)

    @Transaction
    suspend fun insertSchedule(scheduleDates: List<ScheduleDate>) {
        scheduleDates.forEach() { scheduleDate -> insertScheduleDate(scheduleDate) }
    }

    // CUSTOM TRANSACTIONS

    @Transaction
    suspend fun populateDBWithDefaultData(): List<WorkoutPlan> {
        // Set default user data
        Log.d(
            "HomeViewModel",
            "Inserting user...: $defaultUser"
        )
        insertUser(defaultUser)

        // Set user's schedule with default PPL schedule
        Log.d(
            "HomeViewModel",
            "Inserting schedule...: $PPLSchedule"
        )
        insertSchedule(PPLSchedule)

        // Populate Exercises table with default PPL exercises
        val exerciseDataSet = listOf(pushExercises, pullExercises, legExercises)
        exerciseDataSet.forEach { exerciseList ->
            exerciseList.forEach {
                Log.d(
                    "HomeViewModel",
                    "Inserting exercise...: $it"
                )
                insertExercise(it)
            }
        }

        // Populate Workouts table with default PPL workouts
        PPLWorkouts.forEach {
            Log.d(
                "HomeViewModel",
                "Inserting workout...: $it"
            )
            insertWorkout(it)
        }

        // Populate WorkoutExercises table with default PPL workout exercises
        val workoutExerciseDataSet =
            listOf(pushWorkoutExercises, pullWorkoutExercises, legWorkoutExercises)
        workoutExerciseDataSet.forEach { workoutExerciseList ->
            workoutExerciseList.forEach {
                Log.d(
                    "HomeViewModel",
                    "Inserting workout exercise... $it"
                )
                insertWorkoutExercise(it)
            }
        }

        // No started workout by default
        // nextWorkoutId is 0 by default

        Log.d(
            "StronkLiftsDao",
            "Seeding DB done"
        )

        return getAllWorkoutPlans()
    }


    /* * * * * *  READ  * * * * * */

    /* GET USER INFO */

    // Get user info (only one anon user exists)
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUser(userId: Long): User?

    // Get user count - should be 1
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    /* GET WORKOUT PLAN */

    // Get all workout plans (sorted)
    @Suppress("FunctionName") // ignore the underscore
    @Transaction
    @Query("SELECT * FROM workouts Order By listOrder")
    fun _getAllWorkoutPlans(): List<WorkoutPlan>

    // Get all workouts in plan (sorted, with internal exercise list sorted)
    fun getAllWorkoutPlans(): List<WorkoutPlan> {
        val workoutPlans: List<WorkoutPlan> = _getAllWorkoutPlans()
        workoutPlans.forEach { workoutPlan ->
            workoutPlan.exercises = workoutPlan.exercises.sortedBy { it.workoutExercise.listOrder }
        }
        return workoutPlans // LiveData is abstract???
    }

    // Get a specific workout in a plan (has the exercises and dates)
    @Transaction
    @Query("SELECT * FROM workouts WHERE workoutId = :workoutId")
    suspend fun getWorkoutPlan(workoutId: Long): WorkoutPlan

    //Get all workout exercises in a workout plan
    @Suppress("FunctionName") // ignore the underscore
    @Transaction
    suspend fun _getAllSortedWorkoutExercises(workoutId: Long): List<WorkoutExerciseComplete> {
        return getWorkoutPlan(workoutId).exercises.sortedBy { it.workoutExercise.listOrder }
    }

    // Get all workout exercise names from a workout plan
    fun getAllWorkoutExerciseNames(workoutPlan: WorkoutPlan): List<String> {
        return workoutPlan.exercises.map { it.exercise.exerciseName }
    }

    @Query("SELECT COUNT(*) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun getWorkoutExerciseCount(workoutId: Long): Int

    @Query("SELECT COUNT(*) FROM workouts WHERE workoutId = :workoutId")
    suspend fun getWorkoutCount(workoutId: Long): Int

    @Query("SELECT * FROM workouts WHERE listOrder = :listOrder")
    suspend fun getWorkoutByListOrder(listOrder: Int): Workout

    // Get Last Workout by order
    @Query("SELECT * FROM workouts ORDER BY listOrder DESC LIMIT 1")
    suspend fun getLastWorkout(): Workout

    /* GET EXERCISES */

    // Get all available exercises
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): LiveData<List<Exercise>>

    // Get number of all available exercises
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    /* GET WORKOUT HISTORY */
    // Assumption: User does one workout a day (each workout has only one date)

    @Transaction
    @Query("SELECT * FROM workout_history WHERE workoutHistoryId = :workoutHistoryId")
    suspend fun getWorkoutHistory(workoutHistoryId: Long): WorkoutHistory

    @Transaction
    @Query("SELECT * FROM workout_history WHERE workoutHistoryId = :workoutHistoryId")
    suspend fun getWorkoutHistoryPartial(workoutHistoryId: Long): WorkoutHistoryPartial

    // Get a workout from workout history
    //
//    @Transaction
//    @Query("SELECT * FROM workout_history WHERE workoutHistoryId = :workoutHistoryId")
//    suspend fun getWorkoutHistoryComplete(workoutHistoryId: Long): WorkoutHistoryComplete


    /* GET SCHEDULE DATES */

    @Query("SELECT * FROM schedule_dates")
    fun getSchedule(): List<ScheduleDate>

    /* CUSTOM TRANSACTION */

    @Transaction
    suspend fun getHomeActivityData(userId: Long): Map<String, Any> {
        val u = getUser(userId)
        val user = "user" to (u ?: defaultUser)

        val s = u?.startedWorkoutHistoryId?.let { getWorkoutHistoryPartial(it) } // null if no started workout
        val startedWorkoutHistory = s?. let{ "startedWorkoutHistory" to s }

        val workoutSchedule = "workoutSchedule" to (u?.let{ getSchedule() } ?: PPLSchedule)

        /* If no user (data), populate DB with starting PPL data, get resulting Workout Plans */
        // Note: could fix this... originally wanted populateDBWithDefaultData to return nothing
        val workoutPlans = "workoutPlans" to (u?. let{ getAllWorkoutPlans() } ?: populateDBWithDefaultData())

        Log.d(
            "StrongLiftsDao",
            "Fetching from DB done"
        )

//        return if (startedWorkoutHistory == null) {
//            mapOf(user, workoutSchedule, workoutPlans)
//        } else {
//            mapOf(user, startedWorkoutHistory, workoutSchedule, workoutPlans)
//        }

        return startedWorkoutHistory?.let {
            mapOf(user, it, workoutSchedule, workoutPlans)
        } ?: mapOf(user, workoutSchedule, workoutPlans)
}


    /* * * * * *  UPDATE  * * * * * */

    /* UPDATE USER INFO */

    // Update User
    @Update
    suspend fun updateUser(user: User)

    // Update username
    @Query("UPDATE users SET username = :username WHERE userId = :userId")
    suspend fun updateUsername(username: String, userId: Long)

    @Query("UPDATE users SET nextWorkoutIndex = nextWorkoutIndex + 1 WHERE userId = :userId")
    suspend fun incrementNextWorkoutIndex(userId: Long)

    @Query("UPDATE users SET nextWorkoutIndex = nextWorkoutIndex - 1 WHERE userId = :userId")
    suspend fun decrementNextWorkoutIndex(userId: Long)

    @Query("UPDATE users SET startedWorkoutHistoryId = :id WHERE userId = :userId")
    suspend fun updateStartedWorkoutHistoryId(id: Long?, userId: Long)


    /* UPDATE WORKOUT PLAN */

    @Update
    suspend fun updateWorkout(workout: Workout)

    /* UPDATE WORKOUT HISTORY */

    //@Update
    //suspend fun updateWorkoutHistoryDate(workoutHistoryDate: WorkoutHistoryDate)

//    @Update
//    suspend fun updateExerciseHistory(exerciseHistory: ExerciseHistory)

    //@Transaction
//    suspend fun updateWorkoutHistory(workoutHistory: WorkoutHistory) {
//        updateWorkoutHistoryDate(workoutHistory.workout)
//        workoutHistory.exercises.forEach {
//            updateExerciseHistory(it)
//        }
//    }

    /* UPDATE WORKOUT SCHEDULE */

    // Update an exercise
    @Update
    suspend fun updateExercise(exercise: Exercise)

    /* UPDATE SCHEDULE */

    // Should just add and delete instead
    @Transaction
    suspend fun updateSchedule(scheduleDates: List<ScheduleDate>) {
        deleteAllScheduleDates()
        scheduleDates.forEach{ scheduleDate -> insertScheduleDate(scheduleDate) }
    }

    /* * * * * *  DELETE  * * * * * */

    // Delete an exercise
    @Query("DELETE FROM exercises WHERE exerciseId = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: Long)
    // need to also refresh workout plans, maybe call getAllWorkoutPlans() again?

    // Delete a workout
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // Delete all workout exercises by workoutId (only called in deleteWorkoutPlan)
    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteAllWorkoutExercises(workoutId: Long)

    // Delete a specific workout exercise by workoutId and exerciseId
    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId AND exerciseId = :exerciseId")
    suspend fun deleteWorkoutExercise(workoutId: Long, exerciseId: Long)

    // Delete a workout plan
//    @Transaction
//    suspend fun deleteWorkoutPlan(workoutId: Long) {
//        deleteAllWorkoutExercises(workoutId)
//        deleteWorkout(workoutId)
//    }

    // WORKOUT HISTORY

//    @Delete
//    suspend fun deleteWorkoutHistoryDate(workoutHistoryDate: WorkoutHistoryDate)

    @Delete
    suspend fun deleteExerciseHistory(exerciseHistory: ExerciseHistory)

//    @Transaction
//    suspend fun deleteWorkoutHistory(workoutHistory: WorkoutHistory) {
//        deleteWorkoutHistoryDate(workoutHistory.workout)
//        workoutHistory.exercises.forEach {
//            deleteExerciseHistory(it)
//        }
//    }

    // EXERCISE

    // Not safe, must have at least one exercise
    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Transaction
    suspend fun deleteExerciseSafely(exercise: Exercise) {
        val exerciseCount = getExerciseCount()
        if (exerciseCount > 1) {
            deleteExercise(exercise)
        }
    }

    // SCHEDULE
    @Delete
    suspend fun deleteScheduleDate(scheduleDate: ScheduleDate)

    // Not safe, must have a non-empty schedule
    @Query("DELETE FROM schedule_dates")
    suspend fun deleteAllScheduleDates()
}





