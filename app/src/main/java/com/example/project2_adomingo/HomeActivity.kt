package com.example.project2_adomingo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.WorkoutHistoryPartial
import com.example.project2_adomingo.database.WorkoutPlan
import com.example.project2_adomingo.listAdapters.HomeWorkoutListAdapter
import com.example.project2_adomingo.viewModels.HomeViewModel
import java.time.LocalDate

private const val MIN_QUEUE_SIZE = 3

class HomeActivity : AppCompatActivity() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeWorkoutListAdapter: HomeWorkoutListAdapter

    private var workoutQueue: List<WorkoutPlan> = emptyList()
    private var workoutDates: List<LocalDate> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        homeViewModel = HomeViewModel(application)

        // Update UI if app reconfigured (or recreated by lifecycle from low system resources)
        if (homeViewModel.loggedIn) {
            workoutQueue = homeViewModel.workoutQueue
            workoutDates = homeViewModel.workoutDates
        }

        // Setup RecyclerView and List Adapters
        homeWorkoutListAdapter = HomeWorkoutListAdapter(emptyList())

        val homeRecyclerView: RecyclerView = findViewById(R.id.home_recycler_view)
        homeRecyclerView.adapter = homeWorkoutListAdapter

        Log.d(
            "HomeActivity",
            "Home List Adapters Set"
        )

        // Observe changes to workout data
        homeViewModel.liveHomeData.observe(this) { map ->

            Log.d(
                "HomeActivity",
                "LiveHomeData received:\n$map"
            )

            try {
                val user = map["user"] as? User ?: return@observe

                val startedWorkoutHistory = map["startedWorkoutHistory"] as? WorkoutHistoryPartial?
                val workoutSchedule = map["workoutSchedule"] as? List<ScheduleDate> // List<ScheduleDates>
                val workoutPlans = map["workoutPlans"] as? List<WorkoutPlan> // List<WorkoutPlan>

                // Display username
                val usernameTextView: TextView = findViewById(R.id.home_affirmation)
                val affirmationText = "Get ripped, ${user.username}"
                usernameTextView.text = affirmationText

                val nextWorkoutIndex = user.nextWorkoutIndex

                Log.d(
                    "HomeActivity",
                    "Extracted Data:\nuser = ${user}\nworkoutSchedule = ${workoutSchedule}\nworkoutPlans = ${workoutPlans}\nnextWorkoutIndex = ${nextWorkoutIndex}\nstartedWorkoutHistory? = $startedWorkoutHistory"
                )

                // Create Workout Queue
                if (workoutPlans != null) {
                    workoutQueue = homeViewModel.setWorkoutQueue(workoutPlans, nextWorkoutIndex, startedWorkoutHistory,
                        MIN_QUEUE_SIZE)
                }

                // Create schedule dates:
                if (workoutSchedule != null) {
                    workoutDates =
                        homeViewModel.getNextWorkoutDates(workoutSchedule, workoutQueue.size)
                }

                // Commit to LiveData
                homeViewModel.liveWorkoutQueue.postValue((workoutQueue zip workoutDates).toList())

                // Save to Home ViewModel
                homeViewModel.workoutQueue = workoutQueue
                homeViewModel.workoutDates = workoutDates


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Observe changes to Workout Queue
        // Must be observable because this can change during onResume or activity result callback
        homeViewModel.liveWorkoutQueue.observe(this) {
            homeWorkoutListAdapter.updateWorkoutPlans(it)

            Log.d(
                "HomeActivity",
                "LiveWorkoutQueue data received:\nWorkout Queue: ${it.map { pair ->  pair.first}}" +
                        "\nWorkout Dates: ${it.map { pair -> pair.second }}"
            )
        }

        if (homeViewModel.started) {
            val startWorkoutButton: Button = findViewById(R.id.action_button)
            val text = "Continue Workout"
            startWorkoutButton.text = text
        }

        homeWorkoutListAdapter.setOnClickListener { position ->
            val projectedWorkoutDate: LocalDate = workoutDates[position]
            val today: LocalDate = LocalDate.now()

            if (homeViewModel.started && projectedWorkoutDate != today) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Workout In Progress")
                    .setMessage("Starting a new workout will delete your workout in progress.")

                builder.setPositiveButton("Start New Workout") { _, _ ->
                    startNewWorkout(position)
                }
                builder.setNeutralButton("Resume Workout in Progress") { _, _ ->
                    resumeWorkoutInProgress()
                }
                builder.setNegativeButton("Cancel") { _, _ ->
                    // Do nothing
                }
            } else {
                resumeWorkoutInProgress()
            }

        }
    }

    private fun startNewWorkout(position: Int) {

//        // Get WorkoutPlan clicked and its projected date
//        val workout: WorkoutPlan = workoutQueue[position]
//        val projectedWorkoutDate: LocalDate = workoutDates[position]
//
//        // Extract info from WorkoutPlan needed for WorkoutHistory
//        val workoutId: Long = workout.workout.workoutId
//        val workoutName: String = workout.workout.workoutName
//        val workoutExercises: List<WorkoutExerciseComplete> = workout.exercises
//
//        val extraWorkoutExercises = ArrayList<String>()
//        workoutExercises.forEach { it ->
//            val extraExercise = JSONObject()
//            extraExercise.put("name", it.exercise.exerciseName)
//            extraExercise.put("sets", it.workoutExercise.sets)
//            extraExercise.put("reps", it.workoutExercise.reps)
//            extraExercise.put("weight", it.workoutExercise.weight)
//            // other potential info: body weight, notes
//            extraWorkoutExercises.add(extraExercise.toString())
//        }
//
//        val intent = Intent(this, WorkoutActivity::class.java)
//        intent.putExtra("workoutId", workoutId)
//        intent.putExtra("workoutName", workoutName)
//        intent.putStringArrayListExtra("workoutExercises", extraWorkoutExercises)
//        startActivity(intent)

        // TODO: Register activity for result callback, so that HomeActivity knows if the workout was actually started
        TODO()
    }

    private fun resumeWorkoutInProgress() {
        TODO()
    }


}