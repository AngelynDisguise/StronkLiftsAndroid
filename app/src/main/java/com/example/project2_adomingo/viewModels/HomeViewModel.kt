package com.example.project2_adomingo.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.project2_adomingo.PPLSchedule
import com.example.project2_adomingo.PPLWorkoutPlans
import com.example.project2_adomingo.PPLWorkouts
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.StronkLiftsDao
import com.example.project2_adomingo.database.StronkLiftsDatabase
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.WorkoutPlan
import com.example.project2_adomingo.defaultUser
import com.example.project2_adomingo.legExercises
import com.example.project2_adomingo.legWorkoutExercises
import com.example.project2_adomingo.pullExercises
import com.example.project2_adomingo.pullWorkoutExercises
import com.example.project2_adomingo.pushExercises
import com.example.project2_adomingo.pushWorkoutExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application): AndroidViewModel(application) {

    // DAO
    private val stronkLiftsDao: StronkLiftsDao

    // USER DATA
    var user: LiveData<User>? = null
    var workoutPlans: LiveData<List<WorkoutPlan>>? = null
    var workoutSchedule: LiveData<List<ScheduleDate>>? = null

    // HOME UI
    var workoutQueue: List<WorkoutPlan>? = null // workout cards

    init {
        // Connect to database
         stronkLiftsDao = StronkLiftsDatabase.getDatabase(application).stronkLiftsDao()

        // Load user data - if user doesn't exist, seed database before loading
        loadUserData()
    }

    // If no user exists, load default user info, PPL data, and schedule
    private fun loadUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if the database is empty
            val noDataInDatabase = stronkLiftsDao.getUserCount() == 0

            if (noDataInDatabase) {
                // Set user
                stronkLiftsDao.insertUser(defaultUser)

                // Set PPL Schedule
                stronkLiftsDao.insertSchedule(PPLSchedule)

                // Set PPL Exercises Data
                val exerciseDataSet = listOf(pushExercises, pullExercises, legExercises)
                exerciseDataSet.forEach { exerciseList ->
                    exerciseList.forEach {
                        stronkLiftsDao.insertExercise(it)
                    }
                }

                // Set PPL Workout Data
                PPLWorkouts.forEach {
                    stronkLiftsDao.insertWorkout(it)
                }

                // Set PPL Workout Exercises Data
                val workoutExerciseDataSet =
                    listOf(pushWorkoutExercises, pullWorkoutExercises, legWorkoutExercises)
                workoutExerciseDataSet.forEach { workoutExerciseList ->
                    workoutExerciseList.forEach {
                        stronkLiftsDao.insertWorkoutExercise(it)
                        //println(it)
                    }
                }
            }

            // Get user
            user = stronkLiftsDao.getUser()

            // Load workout schedule
            workoutSchedule = stronkLiftsDao.getSchedule()

            // Load workout data
            workoutPlans = stronkLiftsDao.getAllWorkoutPlans()

            // Load workout queue (get the next three workouts to do)
            workoutQueue = workoutPlans!!.value?.take(3)
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