package com.example.project2_adomingo.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import com.example.project2_adomingo.database.ScheduleDate
import com.example.project2_adomingo.database.WorkoutExercise
import com.example.project2_adomingo.database.WorkoutExerciseComplete
import com.example.project2_adomingo.database.WorkoutPlan
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class HomeWorkoutListAdapter(private var workoutList: List<WorkoutPlan>, private var scheduleDates: List<ScheduleDate>) :
    RecyclerView.Adapter<HomeWorkoutListAdapter.ViewHolder>() {
    private var onClickListener: ((Int) -> Unit)? = null
    private val schedule: List<DayOfWeek> = scheduleDates.map { it.weekday }

    // Reference the type of views I'm using
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Header row
        val homeWorkoutName: TextView = view.findViewById(R.id.home_workout_name)
        val homeWorkoutDate: TextView = view.findViewById(R.id.home_workout_date)

        private val homeExerciseList: RecyclerView = view.findViewById(R.id.exercise_recycler_view)
        fun bind(result: List<WorkoutExerciseComplete>) {
            val exerciseListAdapter = ExerciseListAdapter(result)
            homeExerciseList.adapter = exerciseListAdapter
        }
    }

    // Layout Manager creates new views and defines the UI of the list item
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.home_workout_card_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Layout manager populates the views with the dataset passed in
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Add preloaded workouts
        val currentWorkout: WorkoutPlan = workoutList[position] // must be of size 3
        val nextWorkoutDates = getNextWorkoutDates() // must be of size 3

        // Workout name
        viewHolder.homeWorkoutName.text = currentWorkout.workout.workoutName

        // Workout Date (top card is today)
        /* TODO:
            - should implement top card to be next date if today's workout is finished
            - define nextWorkoutDates as parameter, so when today's workout is done,
            nextWorkoutDates changes and syncs with workoutList
         */
        viewHolder.homeWorkoutDate.text = nextWorkoutDates[position]

        // Embedded list of exercises
        viewHolder.bind(currentWorkout.exercises)
    }

    // OnClick listeners to bind
//    fun setOnClickListener(listener: (Int) -> Unit) {
//        this.onClickListener = listenergetDateOfNextWorkout()
//    }

    // Layout manager returns the size of dataset
    override fun getItemCount() = workoutList.size

    private fun getNextWorkoutDates(): List<String> {
        val today: LocalDate = LocalDate.now() // (e.g. 2024-04-08)
        var currentDate: LocalDate = today
        var currentDayOfWeek: DayOfWeek = today.dayOfWeek // (e.g. Monday, value = 1; Sunday, value = 7)
        var remainingDates: Int = 3
        val nextWorkoutDates = mutableListOf<String>()

        while (remainingDates > 0) {
            if ((currentDayOfWeek == today.dayOfWeek) && (currentDayOfWeek in schedule) && nextWorkoutDates.isEmpty()) {
                nextWorkoutDates.add("Today")
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

                // Format date to look like "Tue, 9 Apr"
                val formatter = DateTimeFormatter.ofPattern("EEE, d MMM")
                nextWorkoutDates.add(currentDate.format(formatter))
            }
            currentDayOfWeek = currentDate.dayOfWeek
            remainingDates--
        }
        return nextWorkoutDates
    }

    // Update workout plans
    fun updateWorkoutPlans(newWorkoutPlans: List<WorkoutPlan>) {
        workoutList = newWorkoutPlans
        notifyDataSetChanged()
    }

    // Update workout schedule
    fun updateWorkoutSchedule(newWorkoutSchedule: List<ScheduleDate>) {
        scheduleDates = newWorkoutSchedule
        notifyDataSetChanged()
    }
}

