package com.example.project2_adomingo.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate

@Dao
interface StronkLiftsDao {

    /* * * * * *  CREATE * * * * * */
    // IMPORTANT NOTE: only one user (userId = 0) should exist for now
    // Change queries and methods later to have userId parameter if multiple users are an option

    // USER

    // Add user
    // Note: only one user should exist for now, inserting will overwrite
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    // WORKOUT PLAN

    // Add a workout, overwriting if the workout already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: Workout)


    // Insert an exercise for a workout, overwriting if the exercise already exists in the workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkoutExercise(workoutExercise: WorkoutExercise)

    // Insert a workout plan, and the embedded workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkoutPlan(workoutPlan: WorkoutPlan)

    // Add a workout plan along with all the associated entities
    @Transaction
    fun insertWorkoutPlanWithDetails(workoutPlan: WorkoutPlan) {
        val workoutId = workoutPlan.workout.workoutId

        // Insert Workout
        insertWorkout(workoutPlan.workout)

        // Insert WorkoutExercise (workoutId and exerciseId should exist)
        for (exercise in workoutPlan.exercises) {
            insertWorkoutExercise(exercise)
        }

        // Insert WorkoutPlan
        insertWorkoutPlan(workoutPlan.copy(workout = workoutPlan.workout.copy(workoutId = workoutId)))
    }

    // EXERCISE
    @Insert(onConflict = OnConflictStrategy.IGNORE) // prevent overwrite
    fun insertExercise(exercise: Exercise)

    // SCHEDULE

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertScheduleDate(scheduleDate: ScheduleDate)

    @Transaction
    fun insertSchedule(scheduleDates: List<ScheduleDate>) {
        scheduleDates.forEach() { scheduleDate -> insertScheduleDate(scheduleDate) }
    }


    /* * * * * *  READ  * * * * * */
    // IMPORTANT NOTE: only one user (userId = 0) should exist for now

    // USER INFO

    // Get a specific user
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUser(userId: Long): User

    // WORKOUT PLAN

    // Get all workouts in plan (only one plan exists for now)
    @Transaction
    @Query("SELECT * FROM workouts WHERE userId = :userId")
    fun getAllWorkoutPlans(userId: Long): LiveData<List<WorkoutPlan>>

    // Get a specific workout in a plan (has the exercises and dates)
    @Query("SELECT * FROM workouts WHERE workoutId = :workoutId AND userId = :userId")
    fun getWorkoutPlan(userId: Long, workoutId: Long): WorkoutPlan

    // Get all workout exercises in a workout plan
    @Transaction
    fun getAllWorkoutExercises(userId: Long, workoutId: Long): List<WorkoutExercise>? {
        return getWorkoutPlan(userId, workoutId).exercises
    }

    // Get all workout exercise names from a workout plan
    fun getAllWorkoutExerciseNames(workoutPlan: WorkoutPlan): List<String> {
        return workoutPlan.exercises.map { it.exercise.exerciseName }
    }

    // WORKOUT HISTORY

    // Get workout history (has workout date and exercises)
    @Transaction
    @Query("SELECT * FROM workout_history_dates WHERE userId = :userId")
    fun getWorkoutHistory(userId: Long): LiveData<List<WorkoutHistory>>

    // Get a workout from workout history
    // Assumption: User does one workout a day (each workout has only one date)
    // Note: Useful for getting today's workout session
    @Transaction
    @Query("SELECT * FROM workout_history_dates WHERE date = :date AND userId = :userId")
    fun getWorkoutFromHistory(userId: Long, date: LocalDate): WorkoutHistory

    @Transaction
    @Query("SELECT * FROM workout_history_dates WHERE date = :today AND userId = :userId")
    fun getTodaysWorkout(userId: Long, today: LocalDate = LocalDate.now()): WorkoutHistory

    // EXERCISES

    // Get all available exercises
    @Query("SELECT * FROM exercises WHERE userId = 0")
    fun getAllExercises(): LiveData<List<Exercise>>

    // Get number of all available exercises
    @Query("SELECT COUNT(*) FROM exercises WHERE userId = 0")
    fun getExerciseCount(): Int

    // SCHEDULE

    @Query("SELECT * FROM workouts WHERE userId = 0")
    fun getUserSchedule() :LiveData<List<ScheduleDate>>

    /* * * * * *  UPDATE  * * * * * */
    // IMPORTANT NOTE: only one user (userId = 0) should exist for now
    // Change queries and methods later to have userId parameter if multiple users are an option

    // USER INFO

    // Update a user
    @Update
    fun updateWorkout(user: User)

    // WORKOUT PLAN

    // Update a workout
    @Update
    fun updateWorkout(workout: Workout)

    // Insert or update a workout plan with details
    @Transaction
    fun insertOrUpdateWorkoutPlanWithDetails(workoutPlan: WorkoutPlan) {
        val workoutId = workoutPlan.workout.workoutId

        // Delete the original WorkoutPlan
        deleteWorkoutPlan(workoutId)

        // Insert the updated WorkoutPlan
        insertWorkoutPlan(workoutPlan)

        // Insert WorkoutExercise
        for (exercise in workoutPlan.exercises) {
            insertWorkoutExercise(exercise)
        }
    }

    // EXERCISE

    // Update an exercise
    @Update
    fun updateExercise(exercise: Exercise)

    // SCHEDULE

    // Should just add and delete instead
    @Transaction
    fun updateSchedule(scheduleDates: List<ScheduleDate>) {
        deleteAllScheduleDates()
        scheduleDates.forEach{ scheduleDate -> insertScheduleDate(scheduleDate) }
    }

    /* * * * * *  DELETE  * * * * * */

    // Delete an exercise
    @Delete
    fun deleteExerciseById(exerciseId: Long)
    // need to also refresh workout plans, maybe call getAllWorkoutPlans() again?

    // Delete a workout (only called in deleteWorkoutPlan)
    @Delete
    fun deleteWorkout(workoutId: Long)

    // Delete all workout exercises by workoutId (only called in deleteWorkoutPlan)
    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    fun deleteAllWorkoutExercises(workoutId: Long)

    // Delete a specific workout exercise by workoutId and exerciseId
    @Query("DELETE FROM workout_exercises WHERE userId = :userId AND workoutId = :workoutId AND exerciseId = :exerciseId")
    fun deleteWorkoutExercise(userId: Long, workoutId: Long, exerciseId: Long)

    // Delete a workout plan
    @Transaction
    fun deleteWorkoutPlan(workoutId: Long) {
        deleteAllWorkoutExercises(workoutId)
        deleteWorkout(workoutId)
    }

    // EXERCISE

    // Not safe, must have at least one exercise
    @Delete
    fun deleteExercise(exercise: Exercise)

    @Transaction
    fun deleteExerciseSafely(exercise: Exercise) {
        val exerciseCount = getExerciseCount()
        if (exerciseCount > 1) {
            deleteExercise(exercise)
        }
    }

    // SCHEDULE
    @Delete
    fun deleteScheduleDate(scheduleDate: ScheduleDate)

    // Not safe, must have a non-empty schedule
    @Query("DELETE FROM schedule_dates")
    fun deleteAllScheduleDates()
}





