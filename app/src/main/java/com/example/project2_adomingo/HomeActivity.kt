package com.example.project2_adomingo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.WorkoutHistoryPartial
import com.example.project2_adomingo.database.WorkoutPlan
import com.example.project2_adomingo.listAdapters.HomeWorkoutListAdapter
import com.example.project2_adomingo.viewModels.HomeViewModel
import org.json.JSONObject
import java.time.LocalDate

const val RESULT_NEW_START = 1
const val RESULT_RESUMED = 2
const val RESULT_CANCEL_START = -1
const val RESULT_FINISHED = 0

class HomeActivity : AppCompatActivity() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeWorkoutListAdapter: HomeWorkoutListAdapter

    private var workoutQueue: List<WorkoutPlan> = emptyList()
    private var workoutDates: List<LocalDate> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup HomeViewModel and connect to Room DB
        homeViewModel = HomeViewModel(application)

        // Update UI if app reconfigured (or recreated by lifecycle from low system resources)
        if (homeViewModel.user != null) {
            workoutQueue = homeViewModel.workoutQueue
            workoutDates = homeViewModel.workoutDates

            setUsername(homeViewModel.user!!.username)
            setActionButton()
        }

        // Setup RecyclerView and List Adapters
        homeWorkoutListAdapter = HomeWorkoutListAdapter(emptyList(), false)

        val homeRecyclerView: RecyclerView = findViewById(R.id.home_recycler_view)
        homeRecyclerView.adapter = homeWorkoutListAdapter

        // Observe changes to workout data
        homeViewModel.liveHomeData.observe(this) { data ->
            data?.let { map ->
                Log.d(
                    "HomeActivity",
                    "LiveHomeData received:\n$map"
                )

                try {
                    val user = map["user"] as? User ?: return@observe // shouldn't return, data exists if user exists (DAO)

                    val workoutSchedule =
                        map["workoutSchedule"] as? List<ScheduleDate> // List<ScheduleDates>
                    val workoutPlans =
                        map["workoutPlans"] as? List<WorkoutPlan> // List<WorkoutPlan>

                    // Dependent variables for schedule and workout
                    val startedWorkout =
                        map["startedWorkoutHistory"] as? WorkoutHistoryPartial? // partial means it includes some of the exercise data
                    val lastFinishedWorkout =
                        map["lastFinishedWorkout"] as? WorkoutHistoryPartial?
                    val nextWorkoutIndex = user.nextWorkoutIndex

                    // Save and display username
                    setUsername(user.username)

                    homeViewModel.workoutSchedule = workoutSchedule
                    homeViewModel.workoutPlans = workoutPlans

                    // Actual things needed to be saved to view model
                    homeViewModel.user = user
                    homeViewModel.startedWorkout = startedWorkout
                    homeViewModel.lastFinishedWorkout = lastFinishedWorkout
                    homeViewModel.nextWorkoutIndex = nextWorkoutIndex

                    Log.d(
                        "HomeActivity",
                        "Successfully extracted Data:\nuser = $user\nworkoutSchedule = $workoutSchedule\nworkoutPlans = $workoutPlans\nnextWorkoutIndex = $nextWorkoutIndex\nstartedWorkoutHistory? = $startedWorkout\nlastFinishedWorkoutHistory? = $lastFinishedWorkout"
                    )

                    // Create workout dates
                    workoutSchedule?.let {
                        workoutDates = homeViewModel.setWorkoutDates(workoutSchedule, startedWorkout, lastFinishedWorkout)
                    }

                    // Create workout queue
                    workoutPlans?.let {
                        workoutQueue = homeViewModel.setWorkoutQueue(workoutPlans, nextWorkoutIndex, startedWorkout, lastFinishedWorkout, workoutDates.size)
                    }

                    // Commit to LiveData
                    homeViewModel.liveWorkoutQueue.postValue((workoutQueue zip workoutDates).toList())

                    // Save to Home ViewModel
                    homeViewModel.workoutQueue = workoutQueue
                    homeViewModel.workoutDates = workoutDates
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } ?: let {
                Log.d(
                    "HomeActivity",
                    "No user data found in DB, populating DB with default data..."
                )
                homeViewModel.populateDB()
            }
        }

        // Observe changes to Workout Queue
        // Must be observable because this can change during onResume or activity result callback
        homeViewModel.liveWorkoutQueue.observe(this) {
            homeWorkoutListAdapter.updateWorkoutPlans(it, homeViewModel.startedWorkout != null)

            Log.d(
                "HomeActivity",
                "LIVE WORKOUT QUEUE DATA RECEIVED:\nWorkout Queue: ${it.map { pair ->  pair.first}}" +
                        "\nWorkout Dates: ${it.map { pair -> pair.second }}"
            )

            Log.d(
                "HomeActivity",
                "HOME VIEW MODEL REPORT:\n" +
                        "user: ${homeViewModel.user}\n" +
                        "startedWorkout: ${homeViewModel.startedWorkout}\n" +
                        "lastFinishedWorkout: ${homeViewModel.lastFinishedWorkout}\n" +
                        "nextWorkoutIndex: ${homeViewModel.nextWorkoutIndex}"
            )

            setActionButton()
        }

        // Update StartedButton UI if workout started
        if (homeViewModel.startedWorkout != null) {
            val startWorkoutButton: Button = findViewById(R.id.action_button)
            val text = "Continue Workout"
            startWorkoutButton.text = text
        }

        // Clicking on card brings user to Workout Activity
        homeWorkoutListAdapter.setOnClickListener { position ->
            val projectedWorkoutDate: LocalDate = workoutDates[position]
            val today: LocalDate = LocalDate.now()

            // Alert user if workout already started and this workout isn't for today
            if ((homeViewModel.startedWorkout != null) && projectedWorkoutDate != today) {
                Log.d("HomeActivity", "User chose another workout other than the one in progress. Sending Alert Dialogue.")
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
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            // Clicked the started workout
            else if (homeViewModel.startedWorkout != null && projectedWorkoutDate == today){
                Log.d("HomeActivity", "User clicked the started workout.")
                resumeWorkoutInProgress()
            }
            // Clicked a workout and nothing started yet (!homeViewModel.started)
            else {
                Log.d("HomeActivity", "User clicked an unstarted workout.")
                startNewWorkout(position)
            }

        }
    }

    // Pass WorkoutPlan info for WorkoutHistory
    private fun startNewWorkout(position: Int) {
        val intent = Intent(this, WorkoutActivity::class.java)

        // If any, delete and cancel already started workout
        val startedWorkout = homeViewModel.startedWorkout?.workout
        startedWorkout?.let {
            homeViewModel.deleteWorkoutHistory(startedWorkout)
            homeViewModel.startedWorkout = null
            //homeViewModel.cancelStartedWorkout()
            //homeViewModel.updateStartedWHID(null) // unnecessary? foreign key constraint sets this to null when deleting workout history
        }

        /* Pass WorkoutPlan id
        * - Workout activity will query to get info (unless workoutId is cached)
        * - WorkoutPlan should exist since you can't click on an unstarted workout plan that doesn't exist
        */
        val workoutId: Long = workoutQueue[position].workout.workoutId
        intent.putExtra("workoutId", workoutId)

        /* Pass today flag and position offset, and queue size
        * - If is this workout is started:
        *  1) WorkoutHistory dated today's date is created and persisted when WorkoutActivity exists/pauses.
        *  2) user's nextWorkoutIndex is incremented by position+1 (Corrected if index out of bounds)
        *       - If top card clicked (position 0), offset = 1
        *       - If other card clicked (0 <= position < queue.size), offset = position + 1
         */
        //val isToday: Boolean = (workoutDates[position] == LocalDate.now())
        val offset: Int = position+1
        val queueSize: Int = workoutQueue.size

        //intent.putExtra("isToday", isToday) // started workout will become today
        intent.putExtra("offset", offset)
        intent.putExtra("queueSize", queueSize)

        Log.d("HomeActivity", "Sending intent to Workout: $intent extras: ${intent.extras}")
        startForWorkoutActivityResult.launch(intent)

    }

    // Pass the startedWorkoutHistoryId
    private fun resumeWorkoutInProgress() {
        val intent = Intent(this, WorkoutActivity::class.java)
        val startedWHID = homeViewModel.startedWorkout?.workout?.workoutHistoryId
        startedWHID?.let {
            intent.putExtra("startedWHID", startedWHID)
        }
        Log.d("HomeActivity", "Sending intent to Workout: $intent extras: ${intent.extras}")
        startForWorkoutActivityResult.launch(intent)
    }

    // Callback for Workout Activity result
    private var startForWorkoutActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_RESUMED -> {
                val resumedStartedWHID: Long? = result.data?.getLongExtra("resumedStartedWHID", -1)
                if (resumedStartedWHID != null && resumedStartedWHID > -1) {
                    Log.d("HomeActivity", "RESULT_RESUMED: Workout id $resumedStartedWHID still in progress.")
                }
            }
            RESULT_NEW_START -> {
                val newStartedWHID: Long? = result.data?.getLongExtra("newStartedWHID", -1)
                if (newStartedWHID != null && newStartedWHID > -1) {
                    homeViewModel.setNewStartedWorkout(newStartedWHID)
                    Log.d("HomeActivity", "RESULT_NEW_START: Workout id $newStartedWHID started!")

                }
            }
            RESULT_CANCEL_START -> {
                homeViewModel.cancelStartedWorkout()
            }
            RESULT_FINISHED -> {
                val newFinishedWHID: Long? = result.data?.getLongExtra("newFinishedWHID", -1)
                if (newFinishedWHID != null && newFinishedWHID > -1) {
                    homeViewModel.setNewFinishedWorkout(newFinishedWHID)
                    Log.d("HomeActivity", "RESULT_FINISHED: Workout id $newFinishedWHID finished!")

                }
            }
            else -> {
                Log.d("HomeActivity", "Error: unknown result code (${result.resultCode}) returned by Workout Activity")
            }
        }
    }

    private fun setUsername(username: String) {
        val usernameTextView: TextView = findViewById(R.id.home_affirmation)
        val affirmationText = "Get ripped, $username"
        usernameTextView.text = affirmationText
    }

    private fun setActionButton() {
        val startButton: Button = findViewById(R.id.action_button)
        if (homeViewModel.startedWorkout != null) {
            val text = "Resume Workout"
            startButton.text = text
            startButton.setOnClickListener {
                resumeWorkoutInProgress()
            }
        } else {
            val text = "Start Workout"
            startButton.text = text
            startButton.setOnClickListener {
                startNewWorkout(0)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        Log.d(
            "HomeActivity",
            "HOME VIEW MODEL REPORT:\n" +
                    "user: ${homeViewModel.user}\n" +
                    "startedWorkout: ${homeViewModel.startedWorkout}\n" +
                    "lastFinishedWorkout: ${homeViewModel.lastFinishedWorkout}\n" +
                    "nextWorkoutIndex: ${homeViewModel.nextWorkoutIndex}"
        )
    }

}