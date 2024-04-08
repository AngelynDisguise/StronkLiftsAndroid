package com.example.project2_adomingo.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import com.example.project2_adomingo.database.Workout
import com.example.project2_adomingo.database.WorkoutPlan

class HomeWorkoutListAdapter(private var workoutList: List<WorkoutPlan>) :
    RecyclerView.Adapter<WorkoutListAdapter.ViewHolder>() {
    private var onClickListener: ((Int) -> Unit)? = null

    // Reference the type of views I'm using
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // List of workout cards
        val homeWorkoutCard: Button = view.findViewById(R.id.home_workout_card)
        val homeWorkoutName: TextView = view.findViewById(R.id.home_workout_name)

        // Embedded list of exercises
        val homeExerciseList: RecyclerView = view.findViewById(R.id.workout_card_exercise_list)
        lateinit var exerciseListAdapter: ExerciseAdapter
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
        val currentWorkout: WorkoutPlan = workoutList[position]

        viewHolder.homeWorkoutName.text = currentWorkout.workout.workoutName
        viewHolder.exerciseListAdapter= ExerciseListAdapter(currentWorkout.exercises)
        viewHolder.homeExerciseList.apply {
            //layoutManager = LinearLayoutManager(context)
            adapter = viewHolder.exerciseListAdapter
        }
    }

    // OnClick listeners to bind
    fun setOnClickListener(listener: (Int) -> Unit) {
        this.onClickListener = listener
    }

    // Layout manager returns the size of dataset
    override fun getItemCount() = workoutList.size
}