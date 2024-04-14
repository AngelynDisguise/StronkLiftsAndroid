package com.example.project2_adomingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.database.PPLSchedule
import com.example.project2_adomingo.database.PPLWorkoutPlans
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.WorkoutExerciseComplete
import com.example.project2_adomingo.database.WorkoutPlan
import com.example.project2_adomingo.listAdapters.HomeWorkoutListAdapter
import com.example.project2_adomingo.viewModels.HomeViewModel
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeWorkoutListAdapter: HomeWorkoutListAdapter

    private var workoutPlans: List<WorkoutPlan> = PPLWorkoutPlans
    private var workoutSchedule: List<ScheduleDate> = PPLSchedule
    //private var workoutQueue: Pair<WorkoutPlan, LocalDate>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        homeViewModel = HomeViewModel(application)

        if(homeViewModel.workoutPlans != null || homeViewModel.workoutSchedule != null) {
            workoutPlans = homeViewModel.workoutPlans?.value!!
            workoutSchedule = homeViewModel.workoutSchedule?.value!!

            //workoutQueue = workoutPlans.take(3)

            Log.d(
                "HomeActivity",
                "Loaded Workout Plans Data HomeViewModel: \n$workoutPlans\n$workoutSchedule"
            )
        }

        // Setup RecyclerView and List Adapters
        //homeWorkoutListAdapter = HomeWorkoutListAdapter(PPLWorkoutPlans, PPLSchedule)
        homeWorkoutListAdapter = HomeWorkoutListAdapter(workoutPlans, workoutSchedule)
        //homeWorkoutListAdapter = HomeWorkoutListAdapter(workoutQueue, workoutSchedule)

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

        if (homeViewModel.startedWorkout) {
            val startWorkoutButton: Button = findViewById(R.id.action_button)
            val text = "Continue Workout"
            startWorkoutButton.text = text
        }

        homeWorkoutListAdapter.setOnClickListener { position ->
            // Get Workout at position
            //val workout: WorkoutPlan = workoutQueue[position]
            val workout: WorkoutPlan = workoutPlans[position]
            val workoutId: Long = workout.workout.workoutId
            val workoutName: String = workout.workout.workoutName
            val workoutExercises: List<WorkoutExerciseComplete> = workout.exercises

            //val projectedWorkoutDate

            val extraWorkoutExercises = ArrayList<String>()
            workoutExercises.forEach { it ->
                val extraExercise = JSONObject()
                extraExercise.put("name", it.exercise.exerciseName)
                extraExercise.put("sets", it.workoutExercise.sets)
                extraExercise.put("reps", it.workoutExercise.reps)
                extraExercise.put("weight", it.workoutExercise.weight)
                // other potential info: body weight, notes
                extraWorkoutExercises.add(extraExercise.toString())
            }

            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("workoutId", workoutId)
            intent.putExtra("workoutName", workoutName)
            intent.putStringArrayListExtra("workoutExercises", extraWorkoutExercises)
            startActivity(intent)

            // TODO: Register activity for result callback, so that HomeActivity knows if the workout was actually started
        }
    }


}