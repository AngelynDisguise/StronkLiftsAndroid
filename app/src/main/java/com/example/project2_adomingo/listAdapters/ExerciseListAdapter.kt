package com.example.project2_adomingo.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import com.example.project2_adomingo.database.Exercise
import com.example.project2_adomingo.database.WorkoutExercise

class ExerciseListAdapter(private val exercises: List<WorkoutExercise>) :
    RecyclerView.Adapter<ExerciseListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseName: TextView = view.findViewById(R.id.exercise_name)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.exercise_row_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentExercise: Exercise = exercises[position].exercise
        viewHolder.exerciseName.text = currentExercise.exerciseName
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}
