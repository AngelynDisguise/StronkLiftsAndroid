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

    // Insert a workout plan along with all the associated entities
    @Transaction
    fun insertWorkoutPlanWithDetails(workoutPlan: WorkoutPlan) {
        val workoutId = workoutPlan.workout.workoutId

        // Insert Workout
        insertWorkout(workoutPlan.workout)

        // Insert Date (workoutId should exist)
        for (date in workoutPlan.dates) {
            insertDate(Date(date.dateId, workoutId, date.date))
        }

        // Insert WorkoutExercise (workoutId and exerciseId should exist)
        for (exercise in workoutPlan.exercises) {
            insertWorkoutExercise(exercise)
        }

        // Insert WorkoutPlan
        insertWorkoutPlan(workoutPlan.copy(workout = workoutPlan.workout.copy(workoutId = workoutId)))
    }

    // Add a workout, overwriting if the workout already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: Workout)

    // Add an exercise, only if the exercise does not already exist
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertExercise(exercise: Exercise)

    // Insert a date for a workout, overwriting if the date already exists in the workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDate(date: Date)

    // Insert an exercise for a workout, overwriting if the exercise already exists in the workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkoutExercise(workoutExercise: WorkoutExercise)

    // Insert a workout plan, and the embedded workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkoutPlan(workoutPlan: WorkoutPlan)


    /* * * * * *  READ  * * * * * */

    // Get all workout plans
    @Transaction
    @Query("SELECT * FROM workouts")
    fun getAllWorkoutPlans(): LiveData<List<WorkoutPlan>>

    // Get all exercises
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): LiveData<List<Exercise>>

    // Get a specific workout (has the exercises and dates)
    @Query("SELECT * FROM workouts WHERE workoutId = :workoutId")
    fun getWorkoutPlan(workoutId: Long): WorkoutPlan

    // Get all workout exercises from a workout
    @Transaction
    fun getAllWorkoutExercises(workoutId: Long): List<WorkoutExercise>? {
        return getWorkoutPlan(workoutId).exercises
    }

    // Get all dates from a workout
    @Transaction
    fun getAllWorkoutDates(workoutId: Long): List<Date>? {
        return getWorkoutPlan(workoutId).dates
    }

    // Get all workout exercise names from a workout plan
    fun getAllWorkoutExerciseNames(workoutPlan: WorkoutPlan): List<String> {
        return workoutPlan.exercises.map { it.exercise.exerciseName }
    }

    // Get all workout exercise names from a workout plan by ID
//    fun getAllWorkoutExerciseNamesById(workoutId: Long): List<String> {
//        val exercises: List<WorkoutExercise>? = getAllWorkoutExercises(workoutId)
//        return exercises.map { it.exerciseName }
//    }

    // Get current workout session (assuming there is only one workout session in a day)
    @Transaction
    @Query("SELECT * FROM workout_history WHERE date = :today")
    fun getCurrentWorkoutSession(today: LocalDate = LocalDate.now()): LiveData<List<ExerciseHistory>>

    /* * * * * *  UPDATE  * * * * * */

    // Update a workout
    @Update
    fun updateWorkout(workout: Workout)

    // Update an exercise
    @Update
    fun updateExercise(exercise: Exercise)

    // Update a date
    @Update
    fun updateDate(date: Date)

    // Insert or update a workout plan with details
    @Transaction
    fun insertOrUpdateWorkoutPlanWithDetails(workoutPlan: WorkoutPlan) {
        val workoutId = workoutPlan.workout.workoutId

        // Delete the original WorkoutPlan
        deleteWorkoutPlan(workoutId)

        // Insert the updated WorkoutPlan
        insertWorkoutPlan(workoutPlan)

        // Insert Date
        for (date in workoutPlan.dates) {
            insertDate(date)
        }

        // Insert WorkoutExercise
        for (exercise in workoutPlan.exercises) {
            insertWorkoutExercise(exercise)
        }
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

    // Delete all dates by workoutId (only called in deleteWorkoutPlan)
    @Query("DELETE FROM dates WHERE workoutId = :workoutId")
    fun deleteAllWorkoutDates(workoutId: Long)

//    // Delete a specific workout exercise by workoutId and exerciseId
//    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId AND exerciseId = :exerciseId")
//    fun deleteWorkoutExercise(workoutId: Long, exerciseId: Long)
//
//    // Delete a specific date by workoutId and dateId
//    @Query("DELETE FROM dates WHERE workoutId = :workoutId AND dateId = :dateId")
//    fun deleteWorkoutDate(workoutId: Long, dateId: Long)

    // Delete a workout plan
    @Transaction
    fun deleteWorkoutPlan(workoutId: Long) {
        deleteAllWorkoutExercises(workoutId)
        deleteAllWorkoutDates(workoutId)
        deleteWorkout(workoutId)
    }
}





