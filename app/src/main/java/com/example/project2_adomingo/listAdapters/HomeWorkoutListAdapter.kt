package com.example.project2_adomingo.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
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


class HomeWorkoutListAdapter(private var workoutPlansAndDates: List<Pair<WorkoutPlan, LocalDate>>) :
    RecyclerView.Adapter<HomeWorkoutListAdapter.ViewHolder>() {
    private var onClickListener: ((Int) -> Unit)? = null

    // Reference the type of views I'm using
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Header row
        val homeWorkoutName: TextView = view.findViewById(R.id.home_workout_name)
        val homeWorkoutDate: TextView = view.findViewById(R.id.home_workout_date)

        private val homeExerciseList: RecyclerView = view.findViewById(R.id.exercise_recycler_view)
        private val transparentView: View = view.findViewById(R.id.transparent_view)

        fun bind(result: List<WorkoutExerciseComplete>, listener: ((Int) -> Unit)?) {
            val exerciseListAdapter = ExerciseListAdapter(result)
            homeExerciseList.adapter = exerciseListAdapter

            // Make whole view clickable
            itemView.setOnClickListener {
                listener?.invoke(adapterPosition)
            }
            transparentView.setOnClickListener {
                listener?.invoke(adapterPosition)
            }
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
        val currentWorkout: WorkoutPlan = workoutPlansAndDates[position].first // must be of size 3

        // Display Workout name
        viewHolder.homeWorkoutName.text = currentWorkout.workout.workoutName
        
        // Format date to look like "Tue, 9 Apr"
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM")

        // Display Format Date
        val workoutDate: LocalDate = workoutPlansAndDates[position].second
        val formattedWorkoutDate: String = workoutDate.format(formatter)
        viewHolder.homeWorkoutDate.text = formattedWorkoutDate

        // Embed workout's list of exercises
        viewHolder.bind(currentWorkout.exercises, this.onClickListener)

    }

    // OnClick listeners to bind
    fun setOnClickListener(listener: (Int) -> Unit) {
        this.onClickListener = listener
    }

    // Layout manager returns the size of dataset
    override fun getItemCount() = workoutPlansAndDates.size

    // Update data
    fun updateWorkoutPlans(newWorkoutPlansAndDates: List<Pair<WorkoutPlan, LocalDate>>) {
        workoutPlansAndDates = newWorkoutPlansAndDates
        notifyDataSetChanged()
    }
}

