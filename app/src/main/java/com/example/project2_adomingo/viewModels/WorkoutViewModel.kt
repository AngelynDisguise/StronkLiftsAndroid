package com.example.project2_adomingo.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project2_adomingo.database.ExerciseHistory
import com.example.project2_adomingo.database.ExerciseHistoryComplete
import com.example.project2_adomingo.database.ExerciseSetHistory
import com.example.project2_adomingo.database.StronkLiftsDao
import com.example.project2_adomingo.database.StronkLiftsDatabase
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.Workout
import com.example.project2_adomingo.database.WorkoutHistory
import com.example.project2_adomingo.database.WorkoutHistoryComplete
import com.example.project2_adomingo.database.WorkoutPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/* Notes to self:
* A workout is started if a WorkoutHistory with date=today exists in the WorkoutHistory table (and user's currentWorkoutHistoryId != null).
* This particular selected workout is started if the projectedDate (calculated and passed in from HomeActivity)
* matches the started WorkoutHistory, which is today; in other words, projectedDate=today.
* Therefore; we can load this workout's WorkoutHistory data if it is today and a workout has been started;
* Load workout data from DB if:
* 1) WorkoutHistory with date=today exists in DB (or user's currentWorkoutHistoryId != null)
* 2) Current workout's projectedDate = today
*
* How to start a workout that isn't today?
* - If user already started a workout (user has currentWorkoutHistoryId)
* 1) Replace user's currentHistoryId with this one's...
* Wait.. I know user started a workout if WorkoutHistory with date=today exists.
* Does user really need currentWorkoutHistoryId? (Maybe if there were multiple users...)
*
* Workout Activity Flow:
* 1) WorkoutActivity (this Activity) is started by HomeActivity intent. Intent has WorkoutPlan info and projectedDate.
* - Use WorkoutPlan and projectedDate info to create a WorkoutExerciseHistory. This is useful forL 1) potentially saving this to DB if this workout gets started 2) setting ListAdapter info
*
* - S
* */

class WorkoutViewModel(application: Application): AndroidViewModel(application) {
    // DAO
    private val stronkLiftsDao: StronkLiftsDao

    // STARTED
    var workoutLiveData: MutableLiveData<WorkoutHistoryComplete> = MutableLiveData()
    var workoutHistory: WorkoutHistoryComplete? = null
    var resumed: Boolean = false

    init {
        // Connect to database
        stronkLiftsDao = StronkLiftsDatabase.getDatabase(application).stronkLiftsDao()

        Log.d(
            "WorkoutViewModel",
            "Connected to Room DB"
        )

        // Check if workout is started (is Workout in WorkoutHistory?)
        // If Workout not in WorkoutHistory, then workout is not started
        //loadWorkoutDataIfSaved()
    }

    fun getStartedWorkoutHistory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = async { stronkLiftsDao.getWorkoutHistoryComplete(id) }
            workoutLiveData.postValue(res.await())
        }
    }

    fun createWorkoutHistoryFromPlan(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = async { stronkLiftsDao.getWorkoutPlan(id) }
            val newWorkoutHistory = createWorkoutHistoryComplete(res.await())
            workoutLiveData.postValue(newWorkoutHistory)
        }
    }


    private suspend fun createWorkoutHistoryComplete(workoutPlan: WorkoutPlan): WorkoutHistoryComplete {
        // Create WorkoutHistory
        val workout = WorkoutHistory(
            date = LocalDate.now(),
            workoutName = workoutPlan.workout.workoutName,
            workoutId = workoutPlan.workout.workoutId
        )

        // Create ExerciseHistory list
        val exercises: MutableList<ExerciseHistoryComplete> = mutableListOf()
        workoutPlan.exercises.forEachIndexed { index, it ->
            val workoutHistoryId = workout.workoutHistoryId
            val sets = it.workoutExercise.sets
            val reps = it.workoutExercise.reps

            // Create ExerciseHistoryComplete
            val exercise = ExerciseHistory(
                workoutHistoryId = workoutHistoryId,
                exerciseName = it.exercise.exerciseName,
                sets = sets,
                reps = reps,
                weight = it.workoutExercise.weight
            )
            val setsxreps: List<ExerciseSetHistory> = List(sets) {
                ExerciseSetHistory(
                    workoutHistoryId = workoutHistoryId,
                    exerciseHistoryId = exercise.exerciseHistoryId,
                    setNumber = index,
                    repsDone = -reps // negative meaning it is not started
                )
            }
            exercises.add(ExerciseHistoryComplete(exercise,setsxreps))
        }

        return WorkoutHistoryComplete(workout, exercises)
    }

    // If all exercise buttons are white, then workout is not started anymore (update user)
    fun deleteWorkoutHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutHistory?.let {
                stronkLiftsDao.deleteWorkoutHistory(it.workout) // (FK constraint) this will delete all the exercise and set histories along with it! :D
            }
            // set user's startedWorkoutHistory to null in Home
        }
    }

    fun updateWorkoutHistoryComplete(workouHistory: WorkoutHistoryComplete) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutHistory?.let {
                stronkLiftsDao.updateWorkoutHistoryComplete(workoutHistory!!)
            }
        }
    }

    fun insertWorkoutHistoryComplete(workoutHistoryComplete: WorkoutHistoryComplete) {
        viewModelScope.launch(Dispatchers.IO) {
            val workoutHistoryId = stronkLiftsDao.insertWorkoutHistory(workoutHistoryComplete.workout)
            Log.d("WorkoutActivity", "Inserting workout history... ${workoutHistoryComplete.workout}")

            workoutHistoryComplete.exercises.forEach {
                val newExercise = ExerciseHistory(
                    workoutHistoryId = workoutHistoryId,
                    exerciseName = it.exercise.exerciseName,
                    sets = it.exercise.sets,
                    reps = it.exercise.reps,
                    weight = it.exercise.weight
                )
                val exerciseHistoryId = stronkLiftsDao.insertExerciseHistory(newExercise)
                Log.d("WorkoutActivity", "Inserting exercise history... $newExercise")

                it.setsXreps.forEach { set ->
                    val newSet = ExerciseSetHistory(
                        workoutHistoryId = workoutHistoryId,
                        exerciseHistoryId = exerciseHistoryId,
                        setNumber = set.setNumber,
                        repsDone = set.repsDone
                    )
                    stronkLiftsDao.insertExerciseSetHistory(newSet)
                    Log.d("WorkoutActivity", "Inserting set history... $newSet")
                }
            }
        }
    }
}