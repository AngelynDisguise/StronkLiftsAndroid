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
import com.example.project2_adomingo.database.ExerciseHistory
import com.example.project2_adomingo.database.WorkoutHistory
import com.example.project2_adomingo.listAdapters.WorkoutListAdapter
import com.example.project2_adomingo.viewModels.WorkoutViewModel
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

class WorkoutActivity : AppCompatActivity() {
    private lateinit var workoutViewModel: WorkoutViewModel
    private lateinit var workoutListAdapter: WorkoutListAdapter

    private var workoutId: Long = -1
    private lateinit var workoutName: String
    private lateinit var workoutHistoryDate: WorkoutHistory
    private var exerciseHistory: MutableList<ExerciseHistory> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout)

        // Get Current WorkoutPlan Data
        val widExtra: Long = intent.getLongExtra("workoutId", -1)
        val wnExtra: String? = intent.getStringExtra("workoutName")
        val weExtra: ArrayList<String>? = intent.getStringArrayListExtra("workoutExercises")
        if (wnExtra != null && weExtra != null && widExtra.toInt() != -1) {
            try {
                workoutId = widExtra
                workoutName = wnExtra

                Log.d(
                    "WorkoutActivity",
                    "Got Workout: $workoutName (id=$workoutId)"
                )

                // Create WorkoutHistory
                workoutHistoryDate = WorkoutHistory(
                    workoutName = workoutName,
                    date = LocalDate.now()
                )

                val exercisesJSON: List<JSONObject> = weExtra.map{ JSONObject(it) }
                exercisesJSON.forEach {
                    val name: String = it.getString("name")
                    val sets: Int = it.getInt("sets")
                    val reps: Int = it.getInt("reps")
                    val weight: Double = it.getDouble("weight")
                    Log.d(
                        "WorkoutActivity",
                        "Got Exercise:\nname: $name\nsets: $sets" +
                                "\nreps: $reps" +
                                "\nweight: $weight"
                    )
                    // Create WorkoutHistory
                    exerciseHistory.add(ExerciseHistory(
                        workoutHistoryId = workoutHistoryDate.workoutHistoryId,
                        exerciseName = name,
                        sets = sets,
                        reps = reps,
                        weight = weight
                    ))
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

        // Setup Workout View Model
        workoutViewModel = WorkoutViewModel(application)

        // Setup Recycler View and List Adapters
        workoutListAdapter = WorkoutListAdapter(exerciseHistory)
        val workoutRecyclerView: RecyclerView = findViewById(R.id.workout_recycler_view)
        workoutRecyclerView.adapter = workoutListAdapter

//        // Overwrite current history from DB if history exists
//        workoutViewModel.workoutHistory?.observe(this) {
//            exerciseHistory = it.exercises.toMutableList()
//            workoutListAdapter.notifyDataSetChanged() // hope this works!
//
//            Log.d(
//                "WorkoutActivity",
//                "Loaded Exercise History Data from DB: $it\n"
//            )
//        }
    }

    override fun onPause() {
        super.onPause()
        // Save current data to DB

    }

    fun onClickHome(view: View) {
        finish() // Close this activity
    }
}