package com.example.project2_adomingo.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.project2_adomingo.database.PPLSchedule
import com.example.project2_adomingo.database.PPLWorkouts
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.StronkLiftsDao
import com.example.project2_adomingo.database.StronkLiftsDatabase
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.WorkoutPlan
import com.example.project2_adomingo.database.defaultUser
import com.example.project2_adomingo.database.legExercises
import com.example.project2_adomingo.database.legWorkoutExercises
import com.example.project2_adomingo.database.pullExercises
import com.example.project2_adomingo.database.pullWorkoutExercises
import com.example.project2_adomingo.database.pushExercises
import com.example.project2_adomingo.database.pushWorkoutExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application): AndroidViewModel(application) {

    // DAO
    private val stronkLiftsDao: StronkLiftsDao

    // USER DATA
    var user: User? = null
    var workoutPlans: LiveData<List<WorkoutPlan>>? = null
    var workoutSchedule: LiveData<List<ScheduleDate>>? = null

    // HOME UI
    var workoutQueue: List<WorkoutPlan>? = null // workout cards

    /*
    startedWorkout will tell if a workout has started.
    Meaning
    - user's startedWorkoutId != null, AND that WorkoutHistory's date is today
    - front of workoutQueue and top card is today's workout (derived from a WorkoutHistory)
    - nextWorkoutIndex is at workoutQueue[1]
    - if user chooses a different card other than today's workout, they need to cancel today's workout (remove the top card),

     */
    var startedWorkout: Boolean = false

    init {
        // Connect to database
         stronkLiftsDao = StronkLiftsDatabase.getDatabase(application).stronkLiftsDao()

        Log.d(
            "HomeViewModel",
            "Connected to Room DB"
        )

        // Load user data - if user doesn't exist, seed database before loading
        loadUserData()
    }

    // If no user exists, load default user info, PPL data, and schedule
    private fun loadUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if the database is empty
            val noDataInDatabase = stronkLiftsDao.getUserCount() == 0

            if (noDataInDatabase) {
                Log.d(
                    "HomeViewModel",
                    "No Users found, seeding DB..."
                )

                // Set user
                stronkLiftsDao.insertUser(defaultUser)

                // Set PPL Schedule
                stronkLiftsDao.insertSchedule(PPLSchedule)

                // Set PPL Workout Data
                PPLWorkouts.forEach {
                    stronkLiftsDao.insertWorkout(it)
                }

                // Set PPL Exercises Data
                val exerciseDataSet = listOf(pushExercises, pullExercises, legExercises)
                exerciseDataSet.forEach { exerciseList ->
                    exerciseList.forEach {
                        stronkLiftsDao.insertExercise(it)
                    }
                }

                // Set PPL Workout Exercises Data
                val workoutExerciseDataSet =
                    listOf(pushWorkoutExercises, pullWorkoutExercises, legWorkoutExercises)
                workoutExerciseDataSet.forEach { workoutExerciseList ->
                    workoutExerciseList.forEach {
                        stronkLiftsDao.insertWorkoutExercise(it)
                    }
                }

                Log.d(
                    "HomeViewModel",
                    "Seeding DB done"
                )
            }

            // Get user
            user = stronkLiftsDao.getUser()

            // Load workout schedule
            workoutSchedule = stronkLiftsDao.getSchedule()

            // Load workout data
            workoutPlans = stronkLiftsDao.getAllWorkoutPlans()

            // Load workout queue (get the next three workouts to do)
            // Get current workout if one was started (goes in front of queue)
//            if (user!!.currentWorkoutHistoryId != null) {
//                // Get current workout
//                val currentWorkout: WorkoutHistoryPartial = getWorkoutHistoryPartial(currentWorkoutId)
//                val currentWorkoutPlan: WorkoutPlan = WorkoutPlan(
//                    Workout(
//
//                    )
//                )
//            }
            workoutQueue = workoutPlans!!.value?.take(3)

            Log.d(
                "HomeViewModel",
                "Got Data from DB:\nuser = ${user}\nworkoutSchedule = ${workoutSchedule!!.value}\nworkoutPlans = ${workoutPlans!!.value}s\nworkoutQueue = ${workoutQueue}"
            )
        }
    }

//    private fun loadUserData() {
//        viewModelScope.launch(Dispatchers.IO){
//            // Get user
//            user = stronkLiftsDao.getUser()
//
//            // Load workout schedule
//            workoutSchedule = stronkLiftsDao.getSchedule()
//
//            // Load workout data
//            workoutPlans = stronkLiftsDao.getAllWorkoutPlans()
//
//            // Load workout queue (get the next three workouts to do)
//            workoutQueue = workoutPlans.value?.take(3)
//        }
//    }
//

}