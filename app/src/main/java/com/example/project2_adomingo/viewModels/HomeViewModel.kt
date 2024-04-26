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
    var user: User? = null

    // QUEUE CREATION DATA
    var workoutSchedule: List<ScheduleDate>? = null
    var workoutPlans: List<WorkoutPlan>? = null

    var startedWorkout: WorkoutHistoryPartial? = null
    var lastFinishedWorkout: WorkoutHistoryPartial? = null

    var nextWorkoutIndex: Int = 0

    // HOME UI
    var workoutQueue: List<WorkoutPlan> = emptyList()
    var workoutDates: List<LocalDate> = emptyList()

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
            if (user == null) {
                Log.d(
                    "HomeViewModel",
                    "User not logged into app, fetching data from DB..."
                )

                // Fetch data from DB
                val result: Map<String, Any>? = stronkLiftsDao.getHomeActivityData(DEFAULT_USER_ID)

                // Commit results to LiveHomeData
                liveHomeData.postValue(result)
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
        startedWorkout: WorkoutHistoryPartial?,
        lastFinishedWorkout: WorkoutHistoryPartial?,
        workoutDatesSize: Int
    ): List<WorkoutPlan> {
        if (workoutPlans.isEmpty()) {
            return emptyList()
        }

        // Workout Queue
        val workoutQueue: MutableList<WorkoutPlan>

        // Started? / Workout in progress?
        if (startedWorkout != null) {
            /* Note: At this point, started workouts will always be dated for today.
            Stale workouts are dealt by setWorkoutDates */

            // Does this workout still exist in plan?
            if (startedWorkout.workout.workoutId != null) {
                // Find where started workout is in workout plans
                val startedWorkoutIndex = workoutPlans.indexOfFirst { it.workout.workoutId == startedWorkout.workout.workoutId }
                if (startedWorkoutIndex < 0 ) {
                    Log.d(
                        "HomeViewModel",
                        "Error: Tried to find ${startedWorkout.workout.workoutId}, but it does not exist."
                    )
                    return emptyList()
                } else {
                    // Increment next index by one
                    val newNextIndex = (startedWorkoutIndex + 1) % workoutPlans.size
                    this.nextWorkoutIndex = newNextIndex
                    updateNextWorkoutIndex(newNextIndex)

                    // Start queue at started
                    workoutQueue = startWorkoutQueueAtIndex(workoutPlans, startedWorkoutIndex)
                }
            }
            // Started workout deleted from plan, need to manually add on top
            else {
                // Make dummy Workout Plan for the started workout
                val startedWorkoutPlan = createDummyWorkoutPlan(startedWorkout)

                // Start queue at next
                workoutQueue = startWorkoutQueueAtIndex(workoutPlans, nextWorkoutIndex)

                // Prepend started workout to front of queue
                workoutQueue.add(0, startedWorkoutPlan)
                workoutQueue.removeLast() // even it out
            }
        }
        // Finished?
        else if (lastFinishedWorkout != null ) {
            // Does this workout still exist in plan?
            if (lastFinishedWorkout.workout.workoutId != null) {
                // Find where last finished workout is in workout plans
                val finishedWorkoutIndex = workoutPlans.indexOfFirst { it.workout.workoutId == lastFinishedWorkout.workout.workoutId }

                // Set next index after the finished workout's index
                val newNextIndex = (finishedWorkoutIndex + 1) % workoutPlans.size
                this.nextWorkoutIndex = newNextIndex
                updateNextWorkoutIndex(newNextIndex)

                // Start queue AFTER the finished workout
                workoutQueue = startWorkoutQueueAtIndex(workoutPlans, newNextIndex)
            }
            // Finished workout doesn't exist in plan anymore and is no longer relevant tot he plan order.
            // For now, workout queue will stick to the next workout in the plan. (instead of searching for the last finished workout in history)
            // Might need to fix if reordering plan messes up the next index
            else {
                workoutQueue = startWorkoutQueueAtIndex(workoutPlans, nextWorkoutIndex)
            }
        }
        else {
            workoutQueue = startWorkoutQueueAtIndex(workoutPlans, nextWorkoutIndex)
        }

        Log.d(
            "HomeViewModel",
            "Created Workout Queue:\n$workoutQueue"
        )

        // Adjust queue size to fit the number of workout days
        return if (workoutQueue.size < workoutDatesSize) {
            fillWorkoutQueue(workoutQueue, workoutDatesSize)
        } else if (workoutQueue.size > workoutDatesSize) {
            workoutQueue.take(workoutDatesSize)
        } else {
            workoutQueue.toList()
        }

    }

    private fun createDummyWorkoutPlan(startedWorkoutHistory: WorkoutHistoryPartial): WorkoutPlan {
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
        return WorkoutPlan(
            workout = Workout(
                workoutId = -1,
                workoutName = startedWorkoutHistory.workout.workoutName,
                listOrder = -1,
            ),
            exercises = exercises
        )
    }

//    private inline fun <reified T> startListAtTarget(list: List<T>, target: T): List<T> {
//        return list.dropWhile { it != target } + list.takeWhile { it != target }
//    }

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

    private fun fillWorkoutQueue(workoutQueue: List<WorkoutPlan>, size: Int): List<WorkoutPlan> {
        return (0 until size).map { workoutQueue[it % workoutQueue.size] }
    }

    // Triggered by loadUserData()/changes to liveHomeData (observer)
    fun setWorkoutDates(
        workoutSchedule: List<ScheduleDate>,
        startedWorkout: WorkoutHistoryPartial?,
        lastFinishedWorkout: WorkoutHistoryPartial?
    ): List<LocalDate> {
        val startedWorkoutDate = startedWorkout?.workout?.date
        val finishedWorkoutDate = lastFinishedWorkout?.workout?.date

        // Convert ScheduleData to DayOfWeek
        val weekdays: List<DayOfWeek> = workoutSchedule.map { it.weekday }
        val startDate: LocalDate
        val today: LocalDate = LocalDate.now() // (e.g. 2024-04-08)

        val workoutDates: MutableList<LocalDate>

        // Started? / Workout in progress?
        if (startedWorkoutDate != null) {
            startDate = startedWorkoutDate

            // Stale started workout?
            /* Note: still need to deal with stale next.
            Both started and next workout have been from last year. */
            if (startedWorkoutDate != today) {
                workoutDates = getNextWorkoutDates(weekdays, startDate, workoutSchedule.size+1)

                // Save as finished
                updateLastFinishedWHID(startedWorkout.workout.workoutHistoryId)
                this.lastFinishedWorkout = startedWorkout

                // Remove started
                updateStartedWHID(null)
                this.startedWorkout = null

                // Dequeue stale workout
                workoutDates.removeAt(0)

            } else {
                workoutDates = getNextWorkoutDates(weekdays, startDate, workoutSchedule.size)
            }
        }
        // Finished?
        else if (finishedWorkoutDate != null) {
            /* Note: I can't just start out at next because if a user cancels a started workout,
            I need to start with respect to the last finished workout,
            and next may be different if the started workout wasn't the next workout */
            startDate = finishedWorkoutDate
            workoutDates = getNextWorkoutDates(weekdays, startDate, workoutSchedule.size+1)
            // Dequeue finished workout
            workoutDates.removeAt(0)
        }
        // Nothing started or finished (fresh start)
        else {
            workoutDates = getNextWorkoutDates(weekdays, today, workoutSchedule.size)
        }

        return workoutDates.toList()

    }

    // Get the next number of dates from a starting date
    private fun getNextWorkoutDates(schedule: List<DayOfWeek>, startDate: LocalDate, numDays: Int): MutableList<LocalDate> {
        val today: LocalDate = LocalDate.now() // (e.g. 2024-04-08)

        var currentDate: LocalDate = startDate
        var currentDayOfWeek: DayOfWeek = currentDate.dayOfWeek // (e.g. Monday, value = 1; Sunday, value = 7)
        var remainingDates: Int = numDays

        val nextWorkoutDates = mutableListOf<LocalDate>()

        while (remainingDates > 0) {
            // If today is in the schedule, add it as the first item in the list
            if ((currentDayOfWeek in schedule) && nextWorkoutDates.isEmpty()) {
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
            }

            currentDayOfWeek = currentDate.dayOfWeek
            remainingDates--
        }

        Log.d(
            "HomeViewModel",
            "Got Next Workout Dates:\n$nextWorkoutDates"
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

    // DAO METHODS

    private fun updateNextWorkoutIndex(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            stronkLiftsDao.updateNextWorkoutIndex(index, DEFAULT_USER_ID)
        }
    }

    private fun updateLastFinishedWHID(lastFinishedWHID: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            stronkLiftsDao.updateLastFinishedWHID(lastFinishedWHID, DEFAULT_USER_ID)
        }
    }

    fun updateStartedWHID(startedWHID: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            stronkLiftsDao.updateStartedWHID(startedWHID, DEFAULT_USER_ID)
        }
    }

    fun deleteWorkoutHistory(workoutHistory: WorkoutHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            stronkLiftsDao.deleteWorkoutHistory(workoutHistory)
        }
    }

    fun setNewStartedWorkout(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get new started workout
            val res = async { stronkLiftsDao.getWorkoutHistoryPartial(id) }

            // Save new started workout to view model
            val newStartedWorkout =  res.await()
            startedWorkout = newStartedWorkout

            // Recreate workout dates
            workoutSchedule?.let {
                workoutDates = setWorkoutDates(workoutSchedule!!, newStartedWorkout, lastFinishedWorkout)
            }

            // Recreate workout queue
            workoutPlans?.let {
                workoutQueue = setWorkoutQueue(workoutPlans!!, nextWorkoutIndex, newStartedWorkout, lastFinishedWorkout, workoutDates.size)
            }

            // Commit to LiveData
            liveWorkoutQueue.postValue((workoutQueue zip workoutDates).toList())
        }
    }

    fun cancelStartedWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            // Set started workout to null
            stronkLiftsDao.updateStartedWHID(null, DEFAULT_USER_ID)
            startedWorkout = null

            // Recreate workout dates
            workoutSchedule?.let {
                workoutDates = setWorkoutDates(workoutSchedule!!, null, lastFinishedWorkout)
            }

            // Recreate workout queue
            workoutPlans?.let {
                workoutQueue = setWorkoutQueue(workoutPlans!!, nextWorkoutIndex, null, lastFinishedWorkout, workoutDates.size)
            }

            // Commit to LiveData
            liveWorkoutQueue.postValue((workoutQueue zip workoutDates).toList())
        }
    }

    fun setNewFinishedWorkout(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get new started workout
            val res = async { stronkLiftsDao.getWorkoutHistoryPartial(id) }

            // Save new finished workout to view model
            val newFinishedWorkout =  res.await()
            startedWorkout = newFinishedWorkout

            // Set started workout to null
            stronkLiftsDao.updateStartedWHID(null, DEFAULT_USER_ID)
            startedWorkout = null

            // Recreate workout dates
            workoutSchedule?.let {
                workoutDates = setWorkoutDates(workoutSchedule!!, newFinishedWorkout, lastFinishedWorkout)
            }

            // Recreate workout queue
            workoutPlans?.let {
                workoutQueue = setWorkoutQueue(workoutPlans!!, nextWorkoutIndex, newFinishedWorkout, lastFinishedWorkout, workoutDates.size)
            }

            // Commit to LiveData
            liveWorkoutQueue.postValue((workoutQueue zip workoutDates).toList())
        }
    }
}