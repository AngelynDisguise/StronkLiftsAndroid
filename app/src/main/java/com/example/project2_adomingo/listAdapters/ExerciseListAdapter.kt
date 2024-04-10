package com.example.project2_adomingo.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import com.example.project2_adomingo.database.Exercise
import com.example.project2_adomingo.database.WorkoutExercise
import com.example.project2_adomingo.database.WorkoutExerciseComplete

class ExerciseListAdapter(private val exercises: List<WorkoutExerciseComplete>) :
    RecyclerView.Adapter<ExerciseListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseName: TextView = view.findViewById(R.id.exercise_name)
        val exerciseSetsXReps: TextView = view.findViewById(R.id.exercise_setxXreps)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.exercise_row_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentWorkoutExercise: WorkoutExercise = exercises[position].workoutExercise
        val currentExercise: Exercise = exercises[position].exercise

        viewHolder.exerciseName.text = currentExercise.exerciseName

        val sets = currentWorkoutExercise.sets
        val reps = currentWorkoutExercise.reps
        val weight = currentWorkoutExercise.weight.toInt()
        val setsXRepsText = "${sets}x${reps} ${weight}lb"
        viewHolder.exerciseSetsXReps.text = setsXRepsText
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}
