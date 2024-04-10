package com.example.project2_adomingo

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.database.Equipment
import com.example.project2_adomingo.database.Exercise
import com.example.project2_adomingo.database.MuscleGroup
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.Workout
import com.example.project2_adomingo.database.WorkoutExercise
import com.example.project2_adomingo.database.WorkoutPlan
import com.example.project2_adomingo.listAdapters.HomeWorkoutListAdapter
import com.example.project2_adomingo.viewModels.HomeViewModel
import kotlin.time.Duration.Companion.seconds

class HomeActivity : AppCompatActivity() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeWorkoutListAdapter: HomeWorkoutListAdapter

    private var workoutPlans: List<WorkoutPlan> = PPLWorkoutPlans
    private var workoutSchedule: List<ScheduleDate> = PPLSchedule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        homeViewModel = HomeViewModel(application)

        if(homeViewModel.workoutPlans != null || homeViewModel.workoutSchedule != null) {
            workoutPlans = homeViewModel.workoutPlans?.value!!
            workoutSchedule = homeViewModel.workoutSchedule?.value!!

            Log.d(
                "HomeActivity",
                "Loaded Workout Plans Data HomeViewModel: \n$workoutPlans\n$workoutSchedule"
            )
        }

        // Setup RecyclerView and List Adapters
        //homeWorkoutListAdapter = HomeWorkoutListAdapter(PPLWorkoutPlans, PPLSchedule)
        homeWorkoutListAdapter = HomeWorkoutListAdapter(workoutPlans, workoutSchedule)

        val homeRecyclerView: RecyclerView = findViewById(R.id.home_recycler_view)
        homeRecyclerView.adapter = homeWorkoutListAdapter

        Log.d(
            "HomeActivity",
            "Home List Adapters Set"
        )

        // Observe Workout LiveData from HomeViewModel


        homeViewModel.workoutPlans?.observe(this) { newWorkoutPlans ->
            homeWorkoutListAdapter.updateWorkoutPlans(newWorkoutPlans)
            Log.d(
                "HomeActivity",
                "Loaded Workout Plans Data from DB: $newWorkoutPlans\n"
            )
        }

        homeViewModel.workoutSchedule?.observe(this) { newWorkoutSchedule ->
            homeWorkoutListAdapter.updateWorkoutSchedule(newWorkoutSchedule)
            Log.d(
                "HomeActivity",
                "Loaded Workout Plans Data from DB: $newWorkoutSchedule\n"
            )
        }
    }
}