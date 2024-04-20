package com.example.project2_adomingo.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project2_adomingo.database.DEFAULT_USER_ID
import com.example.project2_adomingo.database.Equipment
import com.example.project2_adomingo.database.Exercise
import com.example.project2_adomingo.database.MuscleGroup
import com.example.project2_adomingo.database.PPLSchedule
import com.example.project2_adomingo.database.PPLWorkoutPlans
import com.example.project2_adomingo.database.PPLWorkouts
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.StronkLiftsDao
import com.example.project2_adomingo.database.StronkLiftsDatabase
import com.example.project2_adomingo.database.User
import com.example.project2_adomingo.database.Workout
import com.example.project2_adomingo.database.WorkoutExercise
import com.example.project2_adomingo.database.WorkoutExerciseComplete
import com.example.project2_adomingo.database.WorkoutHistory
import com.example.project2_adomingo.database.WorkoutHistoryPartial
import com.example.project2_adomingo.database.WorkoutPlan
import com.example.project2_adomingo.database.defaultUser
import com.example.project2_adomingo.database.legExercises
import com.example.project2_adomingo.database.legWorkoutExercises
import com.example.project2_adomingo.database.pullExercises
import com.example.project2_adomingo.database.pullWorkoutExercises
import com.example.project2_adomingo.database.pushExercises
import com.example.project2_adomingo.database.pushWorkoutExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

/*
    A workout is started if:
    - user's startedWorkoutId != null, AND that WorkoutHistory's date is today
    - front of workoutQueue and top card is today's workout (derived from a WorkoutHistory)
    - nextWorkoutIndex is at workoutQueue[1]
    - if user chooses a different card other than today's workout, they need to cancel today's workout (remove the top card),

     */

class HomeViewModel(application: Application): AndroidViewModel(application) {

    // DAO
    private val stronkLiftsDao: StronkLiftsDao

    // USER DATA (only one user exists)
    var loggedIn: Boolean = false
    var started: Boolean = false

    // HOME UI
    lateinit var workoutQueue: List<WorkoutPlan>
    lateinit var workoutDates: List<LocalDate>

    /* Live data to fetch data needed to create workout queue */
    var liveHomeData: MutableLiveData<Map<String, Any>?> = MutableLiveData(emptyMap())

    /* Live data to update Recycler View*/
    var liveWorkoutQueue: MutableLiveData<List<Pair<WorkoutPlan, LocalDate>>> = MutableLiveData(
        emptyList())

    init {
        // Connect to database
        stronkLiftsDao = StronkLiftsDatabase.getDatabase(application).stronkLiftsDao()

        Log.d(
            "HomeViewModel",
            "Connected to Room DB"
        )

        // Load user data - if user doesn't exist, seed database before loading
        viewModelScope.launch(Dispatchers.IO) {
            initUserData()
        }

    }

    // If no user exists, load default user info, PPL data, and schedule
    // Called onCreate and on activity result callback (onResume)
    private suspend fun initUserData() {
            // Fresh start?
            if (!loggedIn) {
                Log.d(
                    "HomeViewModel",
                    "User not logged into app, fetching data from DB..."
                )

                // Fetch data from DB
                val result: Map<String, Any>? = stronkLiftsDao.getHomeActivityData(DEFAULT_USER_ID)

                // Commit results to LiveHomeData
                liveHomeData.postValue(result)
                loggedIn = true
            }

            Log.d(
                "HomeViewModel",
                "User already logged into app, fetching data from cache..."
            )
    }

    // Triggered by loadUserData()/changes to liveHomeData (observer)
    fun setWorkoutQueue(
        workoutPlans: List<WorkoutPlan>,
        nextWorkoutIndex: Int,
        startedWorkoutHistory: WorkoutHistoryPartial?,
        minQueueSize: Int
    ): List<WorkoutPlan> {
        if (workoutPlans.isEmpty()) {
            return emptyList()
        }

        // Set Workout Queue
        val workoutQueue: MutableList<WorkoutPlan> =
            startWorkoutQueueAtIndex(workoutPlans, nextWorkoutIndex)

        // Fill gaps by repeating if queue is too short
        if (workoutQueue.size < minQueueSize) {
            fillWorkoutQueue(minQueueSize)
        }

        // Workout started?
        startedWorkoutHistory?.let {
            val startedDate = startedWorkoutHistory.workout.date
            val today = LocalDate.now()

            // For today?
            if (startedDate == today) {
                /* Make dummy Workout Plan for the started workout */
                val exercises: List<WorkoutExerciseComplete> =
                    startedWorkoutHistory.exercises.map {
                        WorkoutExerciseComplete(
                            workoutExercise = WorkoutExercise(
                                workoutExerciseId = -1,
                                workoutId = -1,
                                exerciseId = -1,
                                sets = it.sets,
                                reps = it.reps,
                                weight = it.weight,
                                breakTime = -1,
                                listOrder = -1
                            ),
                            exercise = Exercise(
                                exerciseId = -1,
                                exerciseName = it.exerciseName,
                                equipment = Equipment.MACHINE,
                                muscleGroup = MuscleGroup.LEGS

                            )
                        )
                    }
                val startedWorkoutPlan = WorkoutPlan(
                    workout = Workout(
                        workoutId = -1,
                        workoutName = startedWorkoutHistory.workout.workoutName,
                        listOrder = -1,
                    ),
                    exercises = exercises
                )

                // Prepend started workout to front of queue
                workoutQueue.add(0, startedWorkoutPlan)
                workoutQueue.removeLast() // remove redundant workout

                // Update nextWorkoutIndex
                //this.nextWorkoutIndex += 1
                viewModelScope.launch(Dispatchers.IO) {
                    stronkLiftsDao.incrementNextWorkoutIndex(DEFAULT_USER_ID)
                }
            }
            /* Started workout is not today - user forgot to finish old workout */
            else {
                viewModelScope.launch(Dispatchers.IO) {
                    // WorkoutHistory should already saved, remove its status as started
                    stronkLiftsDao.updateStartedWorkoutHistoryId(null, DEFAULT_USER_ID)
                }
            }
        }

        Log.d(
            "HomeViewModel",
            "Created Workout Queue:\n$workoutQueue"
        )

        return workoutQueue.toList()
    }

    private fun startWorkoutQueueAtIndex(
        workoutPlans: List<WorkoutPlan>,
        nextWorkoutIndex: Int
    ): MutableList<WorkoutPlan> {
        if (workoutPlans.isEmpty() || nextWorkoutIndex < 0) {
            return mutableListOf()
        }

        val nextWorkout = workoutPlans[nextWorkoutIndex]
        val workoutQueue = workoutPlans.dropWhile { it != nextWorkout } +
                workoutPlans.takeWhile { it != nextWorkout }

        return workoutQueue.toMutableList()
    }

    private fun fillWorkoutQueue(size: Int) {
        workoutQueue = ((0 until size).map { workoutQueue[it % workoutQueue.size] }).toMutableList()
    }

    // Triggered by loadUserData()/changes to liveHomeData (observer)
    fun getNextWorkoutDates(
        workoutSchedule: List<ScheduleDate>,
        numDays: Int
    ): List<LocalDate> {
        // Convert ScheduleData to DayOfWeek
        val schedule: List<DayOfWeek> = workoutSchedule.map { it.weekday }

//        Log.d(
//            "HomeViewModel",
//            "(getNextWorkoutDates parameters) workoutSchedule: $workoutSchedule, numDays: $numDays\nschedule weekdays: $schedule"
//        )

        val today: LocalDate = LocalDate.now() // (e.g. 2024-04-08)
        var currentDate: LocalDate = today
        var currentDayOfWeek: DayOfWeek =
            today.dayOfWeek // (e.g. Monday, value = 1; Sunday, value = 7)
        var remainingDates: Int = numDays
        val nextWorkoutDates = mutableListOf<LocalDate>()

        while (remainingDates > 0) {
            // If today is in the schedule, add it as the first item in the list
            if ((currentDate == today) && (currentDayOfWeek in schedule) && nextWorkoutDates.isEmpty()) {
                nextWorkoutDates.add(currentDate)
            } else {
                // Get the next day in the schedule from today
                val nextScheduledDay: DayOfWeek =
                    schedule.firstOrNull { it.value > currentDayOfWeek.value }
                        ?: schedule.first() // If no day comes after the current day, then the next day would be the first day in the schedule

                // Calculate the number of days until the next scheduled day
                val daysUntilNextScheduledDay: Int =
                    (nextScheduledDay.value - currentDayOfWeek.value + 7) % 7

                // Add the number of days to the current date to get the date of the next scheduled day
                currentDate = currentDate.plusDays(daysUntilNextScheduledDay.toLong())

                nextWorkoutDates.add(currentDate)

//                Log.d(
//                    "HomeViewModel",
//                    "$nextScheduledDay, $daysUntilNextScheduledDay"
//                )
            }
//            Log.d(
//                "HomeViewModel",
//                "$nextWorkoutDates"
//            )

            currentDayOfWeek = currentDate.dayOfWeek
            remainingDates--
        }

        Log.d(
            "HomeViewModel",
            "Created Workout Dates:\n$nextWorkoutDates"
        )
        return nextWorkoutDates
    }

    fun populateDB() {
        viewModelScope.launch(Dispatchers.IO) {
            stronkLiftsDao.populateDBWithDefaultData()
        }

        val defaultData: Map<String, Any> = mapOf(
            "user" to defaultUser,
            //"startedWorkoutHistory" to null,
            "workoutSchedule" to PPLSchedule,
            "workoutPlans" to PPLWorkoutPlans,
            "nextWorkoutId" to 0
        )

        liveHomeData.postValue(defaultData)
    }

}