package com.example.project2_adomingo.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.project2_adomingo.database.ExerciseHistory
import com.example.project2_adomingo.database.StronkLiftsDao
import com.example.project2_adomingo.database.StronkLiftsDatabase
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.WorkoutHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    // USER DATA
    var user: User? = null
    var workoutHistory: LiveData<WorkoutHistory>? = null
    var exerciseHistory: LiveData<MutableList<ExerciseHistory>>? = null

    // WORKOUT UI
    val workoutStarted: Boolean = false

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

//    private fun loadWorkoutDataIfSaved() {
//        viewModelScope.launch(Dispatchers.IO) {
//            if (user == null && workoutHistory == null) {
//                // Fresh load, check data base for workoutHistory
//                try {
//                    stronkLiftsDao.get
//                }
//
//
//            }
//        }
//    }

    fun updateWorkoutExerciseReps() {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    // If an exercise button is clicked and is red, then workout is started
    // If workout is started, create WorkoutHistory
    fun createWorkoutHistory() {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    // If all exercise buttons are white, then workout is not started anymore (update user)
    fun deleteWorkoutHistory() {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }
}