package com.example.project2_adomingo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.database.ExerciseHistory
import com.example.project2_adomingo.database.ExerciseHistoryComplete
import com.example.project2_adomingo.database.ExerciseSetHistory
import com.example.project2_adomingo.database.WorkoutHistory
import com.example.project2_adomingo.database.WorkoutHistoryComplete
import com.example.project2_adomingo.listAdapters.WorkoutListAdapter
import com.example.project2_adomingo.viewModels.WorkoutViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

class WorkoutActivity : AppCompatActivity() {
    private lateinit var workoutViewModel: WorkoutViewModel
    private lateinit var workoutListAdapter: WorkoutListAdapter

    // For Recycler View
    // Started if any positive values in setsXreps
    private var exercises: MutableList<ExerciseHistoryComplete> = mutableListOf()
    private var setsXreps: MutableList<MutableList<Int>> = mutableListOf()

    // Workout History to persist in DB
    private lateinit var workoutHistory: WorkoutHistoryComplete

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
            workoutHistory = workoutViewModel.workoutHistory!!
            exercises = workoutHistory.exercises.toMutableList() // goes to recycler view
            setsXreps = exercises.map{ it.setsXreps.toMutableList().map { it.repsDone }.toMutableList() }.toMutableList()
            setWorkoutTitle(workoutViewModel.workoutHistory!!.workout.workoutName)
        }
        // Resume work in progress?
        else if (startedWHIDExtra > -1) {
            // Trigger live data
            workoutViewModel.getStartedWorkoutHistory(startedWHIDExtra)
            workoutViewModel.resumed = true
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

            Log.d(
                "WorkoutActivity",
                "Loaded Workout History Data from DB: $it\n"
            )
            // save to home and view model
            workoutViewModel.workoutHistory = it
            workoutHistory = it
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
        //Log.d("WorkoutActivity", "Finishing workout activity: $workoutHistory\nWith sets: $setsXreps\nResumed?: ${workoutViewModel.resumed}\nWorkoutInProgress?: ${workoutInProgress(setsXreps)}")

        // Cancelled
        if (workoutViewModel.resumed && !workoutInProgress(setsXreps)) {
            Log.d("WorkoutActivity", "Resumed workout cancelled: started workout history exists and no positive reps were found.\nDeleting started workout history from DB...")
            // Delete WorkoutHistory from DB
            workoutViewModel.deleteWorkoutHistory()
            setResult(RESULT_CANCEL_START)
        }
        // Resumed
        else if (workoutViewModel.resumed && workoutInProgress(setsXreps)) {
            Log.d("WorkoutActivity", "Resumed workout continued: started workout history exists and positive rep values were found.\nSaving started workout history to DB...")
            // Update WorkoutHistory in DB
            workoutViewModel.updateWorkoutHistoryComplete(workoutHistory)

            // Send back over the resumed workoutHistoryId in intent
            val resumedIntent = Intent(this, HomeActivity::class.java)
            resumedIntent.putExtra("resumedStartedWHID", workoutHistory.workout.workoutId)
            setResult(RESULT_RESUMED, resumedIntent)

        // New Start
        } else if(workoutInProgress(setsXreps)) {
            Log.d("WorkoutActivity", "Started new workout: A new workout history was created and positive rep values were found.\nSaving new started workout history to DB...")
            // Insert WorkoutHistory into DB and get workoutId (async-await)
            val workoutHistoryId = runBlocking(Dispatchers.IO) {
                workoutViewModel.insertWorkoutHistory(workoutHistory.workout)
            }
            // Insert the rest of the exercise history (async)
            workoutViewModel.insertExerciseHistoryComplete(workoutHistoryId, getNewExercises(exercises, setsXreps))

            // Send over the new workoutHistoryId in intent
            val newStartIntent = Intent(this, HomeActivity::class.java)
            newStartIntent.putExtra("newStartedWHID", workoutHistoryId)
            setResult(RESULT_NEW_START, newStartIntent)
        } else {
            setResult(RESULT_CANCEL_START)
        }

        finish() // Close this activity
    }

    // Workout is considered "started" or "in progress" once any of the rep values are non-negative
    private fun workoutInProgress(setsXreps: MutableList<MutableList<Int>>): Boolean {
        return setsXreps.any { set -> set.any { reps -> reps > 0 }}
    }

    private fun getNewExercises(exercises: List<ExerciseHistoryComplete>, setsXreps: MutableList<MutableList<Int>>): List<ExerciseHistoryComplete> {
        return (exercises.mapIndexed { index, exercise ->
            ExerciseHistoryComplete(
                exercise = exercise.exercise,
                setsXreps = setsXreps[index].map {
                    ExerciseSetHistory(
                        workoutHistoryId = 0,
                        exerciseHistoryId = 0,
                        setNumber = index,
                        repsDone = it
                    )
                }
            )
        })
    }
}