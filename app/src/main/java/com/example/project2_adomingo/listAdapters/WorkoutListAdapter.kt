package com.example.project2_adomingo.listAdapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import org.json.JSONObject

class WorkoutListAdapter(private var exercises: List<JSONObject>) :
    RecyclerView.Adapter<WorkoutListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Header row
        val exerciseName: TextView = view.findViewById(R.id.exercise_row_name)
        val setsXRepsWeight: Button = view.findViewById(R.id.workout_setsXReps_weight)

        private val setsXRepsList: RecyclerView = view.findViewById(R.id.workout_exercise_recycler_view)
        fun bind(setsXreps: MutableList<Int>) {
            val setsXrepsListAdapter = SetsXRepsListAdapter(setsXreps)
            setsXRepsList.adapter = setsXrepsListAdapter
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.workout_exercise_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        val sets = exercise.getInt("sets")
        val reps = exercise.getInt("reps")
        val weight = exercise.getInt("weight") // originally Double

        Log.d(
            "WorkoutListAdapter",
            "Inflated view with:\nexercise: $exercise"
        )

        viewHolder.exerciseName.text = exercise.getString("name")
        val text = "${sets}x${reps} ${weight}lb  >>"
        viewHolder.setsXRepsWeight.text = text

        // Embedded list of sets/reps
        val setsXreps = MutableList(sets) { reps }
        viewHolder.bind(setsXreps)
    }

    override fun getItemCount() = exercises.size
}