package com.example.project2_adomingo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.database.ExerciseHistory
import com.example.project2_adomingo.database.ExerciseHistoryComplete
import com.example.project2_adomingo.database.WorkoutHistory
import com.example.project2_adomingo.database.WorkoutHistoryComplete
import com.example.project2_adomingo.listAdapters.WorkoutListAdapter
import com.example.project2_adomingo.viewModels.WorkoutViewModel
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

class WorkoutActivity : AppCompatActivity() {
    private lateinit var workoutViewModel: WorkoutViewModel
    private lateinit var workoutListAdapter: WorkoutListAdapter

    // For Recycler View
    // Started if any of the ExerciseSetHistory lists are not empty
    private var exercises: MutableList<ExerciseHistoryComplete> = mutableListOf()
    private var setsXreps: MutableList<MutableList<Int>> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout)

        /* Unpack intent */

        // New workout intent
        val widExtra: Long = intent.getLongExtra("workoutId", -1)

        // Started workout intent
        val startedWHIDExtra: Long = intent.getLongExtra("startedWHID", -1)

        /* * */

        // Setup Workout View Model
        workoutViewModel = WorkoutViewModel(application)

        // Recover from reconfiguration
        if (workoutViewModel.workoutHistory != null) {
            exercises = workoutViewModel.workoutHistory!!.exercises.toMutableList() // goes to recycler view
            setsXreps = exercises.map{ it.setsXreps.toMutableList().map { it.repsDone }.toMutableList() }.toMutableList()
            setWorkoutTitle(workoutViewModel.workoutHistory!!.workout.workoutName)
        }
        // Resume work in progress?
        else if (startedWHIDExtra > -1) {
            // Trigger live data
            workoutViewModel.getStartedWorkoutHistory(startedWHIDExtra)
            Log.d(
                "WorkoutActivity",
                "Got started workout intent. Resuming workout in progress..."
            )
        }
        // Start new workout
        else if (widExtra > -1) {
            workoutViewModel.createWorkoutHistoryFromPlan(widExtra)
        } else {
            Log.d(
                "WorkoutActivity",
                "Error: No workoutId or startedWorkoutId found in intent passed from Home Activity."
            )
        }

        // Setup Recycler View and List Adapters
        workoutListAdapter = WorkoutListAdapter(exercises, setsXreps)
        val workoutRecyclerView: RecyclerView = findViewById(R.id.workout_recycler_view)
        workoutRecyclerView.adapter = workoutListAdapter

        // Overwrite current history from DB if history exists
        workoutViewModel.workoutLiveData.observe(this) {
            exercises.addAll(it.exercises)
            setsXreps.addAll(exercises.map{ it.setsXreps.toMutableList().map { it.repsDone }.toMutableList() }.toMutableList())

            workoutListAdapter.notifyDataSetChanged() // hope this works!
            //workoutListAdapter.updateWorkoutListAdapter(it.exercises.toMutableList())

            Log.d(
                "WorkoutActivity",
                "Loaded Workout History Data from DB: $it\n"
            )
            // save to home and view model
            workoutViewModel.workoutHistory = it
        }
    }

    private fun setWorkoutTitle(workoutName: String) {
        // Set Workout title
        val workoutNameTextView: TextView = findViewById(R.id.workout_title)
        workoutNameTextView.text = workoutName
    }

    override fun onPause() {
        super.onPause()
        // Save current data to DB

    }

    fun onClickHome(view: View) {
        //val finalSetsXReps = exercises.map{ it.setsXreps.map { it.repsDone } }
        val finalSetsXReps = setsXreps
        Log.d("WorkoutActivity", "Finishing workout activity with history: ${workoutViewModel.workoutHistory}\n$finalSetsXReps")
        finish() // Close this activity
    }
}