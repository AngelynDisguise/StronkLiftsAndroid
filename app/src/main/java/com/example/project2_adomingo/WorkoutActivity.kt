package com.example.project2_adomingo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.listAdapters.WorkoutListAdapter
import org.json.JSONException
import org.json.JSONObject

class WorkoutActivity : AppCompatActivity() {
    //private lateinit var workoutViewModel: WorkoutViewModel
    private lateinit var workoutListAdapter: WorkoutListAdapter

    private lateinit var workoutName: String
    private lateinit var exercises: List<JSONObject>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout)

        // Get Current WorkoutPlan Data
        val wnExtra: String? = intent.getStringExtra("workoutName")
        val weExtra: ArrayList<String>? = intent.getStringArrayListExtra("workoutExercises")
        if (wnExtra != null && weExtra != null) {
            try {
                workoutName = wnExtra
                exercises = weExtra.map{ JSONObject(it) }

                exercises.forEach {
                    Log.d(
                        "WorkoutActivity",
                        "Got Exercise:\nname: ${it.getString("name")}\nsets: ${it.getInt("sets")}" +
                                "\nreps: ${it.getInt("reps")}" +
                                "\nweight: ${it.getDouble("weight")}"
                    )
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            Log.d(
                "WorkoutActivity",
                "Error: Intent extra (workoutName, workoutExercises) passed from HomeActivity is is null."
            )
        }

        // Set Workout title
        val workoutNameTextView: TextView = findViewById(R.id.workout_title)
        workoutNameTextView.text = workoutName

        // Make set buttons clickable


        // Setup Workout View Model
        //workoutViewModel = WorkoutViewModel(application)

        // Setup Recycler View and List Adapters
        workoutListAdapter = WorkoutListAdapter(exercises)
        val workoutRecyclerView: RecyclerView = findViewById(R.id.workout_recycler_view)
        workoutRecyclerView.adapter = workoutListAdapter
    }
    fun onClickHome(view: View) {
        finish() // Close this activity
    }
}