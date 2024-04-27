package com.example.project2_adomingo.listAdapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import com.example.project2_adomingo.database.ExerciseHistory
import com.example.project2_adomingo.database.ExerciseHistoryComplete
//import com.example.project2_adomingo.database.ExerciseHistoryComplete
import org.json.JSONObject


class WorkoutListAdapter(private var exercises: MutableList<ExerciseHistoryComplete>, private var setsXreps: MutableList<MutableList<Int>>) :
    RecyclerView.Adapter<WorkoutListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Header row
        val exerciseName: TextView = view.findViewById(R.id.exercise_row_name)
        val setsXRepsWeight: Button = view.findViewById(R.id.workout_setsXReps_weight)

        private val setsXRepsList: RecyclerView = view.findViewById(R.id.workout_exercise_recycler_view)
        fun bind(setsXreps: MutableList<Int>, originalReps: Int) {
            val setsXrepsListAdapter = SetsXRepsListAdapter(setsXreps, originalReps)
            setsXRepsList.adapter = setsXrepsListAdapter
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.workout_exercise_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val exercise = exercises[position].exercise
        val name = exercise.exerciseName
        val sets = exercise.sets
        val reps = exercise.reps
        val weight = exercise.weight.toInt()

        Log.d(
            "WorkoutListAdapter",
            "Inflated view with:\nexercise: $exercise"
        )

        viewHolder.exerciseName.text = name
        val text = "${sets}x${reps} ${weight}lb  >>"
        viewHolder.setsXRepsWeight.text = text

        // Embedded list of sets/reps
        //viewHolder.bind(exercises[position].setsXreps.map { it.repsDone }.toMutableList(), reps)
        viewHolder.bind(setsXreps[position], reps)
    }

    override fun getItemCount() = exercises.size

    fun updateWorkoutListAdapter(exercises: MutableList<ExerciseHistoryComplete>) {
        this.exercises = exercises
        notifyDataSetChanged()
    }


}