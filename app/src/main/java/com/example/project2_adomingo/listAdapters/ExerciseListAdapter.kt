package com.example.project2_adomingo.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import com.example.project2_adomingo.database.Exercise
import com.example.project2_adomingo.database.WorkoutExercise

class ExerciseAdapter(private val exercises: List<WorkoutExercise>) :
    RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = view.findViewById(R.id.exercise_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_row_item, parent, false)
        return ExerciseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentExercise: Exercise = exercises[position].exercise
        holder.exerciseName.text = currentExercise.exerciseName
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}
