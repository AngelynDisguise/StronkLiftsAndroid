package com.example.project2_adomingo.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate

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

    // Insert a workout plan, and the embedded workout
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlan)

    // Add a workout plan along with all the associated entities
    // Added to end of list
//    @Transaction
//    suspend fun insertWorkoutPlanWithDetails(workoutPlan: WorkoutPlan) {
//        val workoutId = workoutPlan.workout.workoutId
//
//        // TODO: Ensure list order is at the end
//
//        // Insert Workout
//        insertWorkout(workoutPlan.workout)
//
//        // Insert WorkoutExercise (workoutId and exerciseId should exist)
//        for (exercise in workoutPlan.exercises) {
//            insertWorkoutExercise(exercise)
//        }
//
//        // Insert WorkoutPlan
//        insertWorkoutPlan(workoutPlan.copy(workout = workoutPlan.workout.copy(workoutId = workoutId)))
//    }

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


    /* * * * * *  READ  * * * * * */

    // USER INFO

    // Get user info (only one anon user exists)
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): LiveData<User>

    // Get user count - should be 1
    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Int

    // WORKOUT PLAN

    // Get all workout plans (sorted)
    @Suppress("FunctionName") // ignore the underscore
    @Transaction
    @Query("SELECT * FROM workouts Order By listOrder")
    fun _getAllWorkoutPlans(): List<WorkoutPlan>

    // Get all workouts in plan (sorted, with internal exercise list sorted)
    fun getAllWorkoutPlans(): LiveData<List<WorkoutPlan>> {
        val workoutPlans: List<WorkoutPlan> = _getAllWorkoutPlans()
        workoutPlans.forEach { workoutPlan ->
            workoutPlan.exercises = workoutPlan.exercises.sortedBy { it.listOrder }
        }
        return MutableLiveData(workoutPlans) // LiveData is abstract???
    }

    // Get a specific workout in a plan (has the exercises and dates)
    @Query("SELECT * FROM workouts WHERE workoutId = :workoutId")
    suspend fun getWorkoutPlan(workoutId: Long): WorkoutPlan

    //Get all workout exercises in a workout plan
    @Suppress("FunctionName") // ignore the underscore
    @Transaction
    suspend fun _getAllSortedWorkoutExercises(workoutId: Long): List<WorkoutExercise> {
        return getWorkoutPlan(workoutId).exercises.sortedBy { it.listOrder }
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

//    // WORKOUT HISTORY
//
//    // Get workout history (has workout date and exercises)
//    @Transaction
//    @Query("SELECT * FROM workout_history_dates")
//    fun getWorkoutHistory(): LiveData<List<WorkoutHistory>>
//
//    // Get a workout from workout history
//    // Assumption: User does one workout a day (each workout has only one date)
//    // Note: Useful for getting today's workout session
//    @Transaction
//    @Query("SELECT * FROM workout_history_dates WHERE date = :date")
//    suspend fun getWorkoutFromHistory(date: LocalDate): WorkoutHistory
//
//    @Transaction
//    @Query("SELECT * FROM workout_history_dates WHERE date = :today")
//    suspend fun getTodaysWorkout(today: LocalDate = LocalDate.now()): WorkoutHistory

    // EXERCISES

    // Get all available exercises
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): LiveData<List<Exercise>>

    // Get number of all available exercises
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    // SCHEDULE

    @Query("SELECT * FROM schedule_dates")
    fun getSchedule() :LiveData<List<ScheduleDate>>

    /* * * * * *  UPDATE  * * * * * */

    // USER

    // Update username
    @Update
    suspend fun updateUser(user: User)

    // WORKOUT PLAN

    // Update a workout
    @Update
    suspend fun updateWorkout(workout: Workout)

    // Insert or update a workout plan with details
//    @Transaction
//    suspend fun insertOrUpdateWorkoutPlanWithDetails(workoutPlan: WorkoutPlan) {
//        val workoutId = workoutPlan.workout.workoutId
//
//        // Delete the original WorkoutPlan
//        deleteWorkoutPlan(workoutId)
//
//        // Insert the updated WorkoutPlan
//        insertWorkoutPlan(workoutPlan)
//
//        // Insert WorkoutExercise
//        for (exercise in workoutPlan.exercises) {
//            insertWorkoutExercise(exercise)
//        }
//    }

    // EXERCISE

    // Update an exercise
    @Update
    suspend fun updateExercise(exercise: Exercise)

    // SCHEDULE

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





