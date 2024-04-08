package com.example.project2_adomingo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.project2_adomingo.database.Exercise
import com.example.project2_adomingo.database.StronkLiftsDatabase
import com.example.project2_adomingo.database.Workout
import com.example.project2_adomingo.database.WorkoutSession

class UserViewModel(application: Application): AndroidViewModel(application) {
    var selectedWorkoutIndex: Int? = null

    // Retrieve app data from database
    private val stronkLiftsDao = StronkLiftsDatabase.getDatabase(application).stronkLiftsDao()
    var workouts: LiveData<MutableList<Workout>> = stronkLiftsDao.getAllWorkouts()
    var exercises: LiveData<MutableList<Exercise>> = stronkLiftsDao.getAllExercises()

    // Current Workout Session
    var currentWorkoutSession: WorkoutSession =

}