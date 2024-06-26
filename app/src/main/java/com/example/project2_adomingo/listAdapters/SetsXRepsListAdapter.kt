package com.example.project2_adomingo.listAdapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.project2_adomingo.R
import kotlin.math.absoluteValue

class SetsXRepsListAdapter(private val setsXReps: MutableList<Int>, private val originalReps: Int) :
    RecyclerView.Adapter<SetsXRepsListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.workout_setsXReps_button)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.workout_exercise_button, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentReps: Int = setsXReps[position]
        viewHolder.button.text = (currentReps.absoluteValue).toString()

        if (currentReps >= 0) {
            viewHolder.button.setBackgroundResource(R.drawable.workout_exercise_button_clicked_background)
        } else {
            viewHolder.button.setBackgroundResource(R.drawable.workout_exercise_button_background)
        }

        // Set onClickListener for the button
        viewHolder.button.setOnClickListener {
            // Decrement the number by one and update the button text
            if (currentReps > 0) {
                setsXReps[position] = currentReps - 1
                viewHolder.button.text = (currentReps-1).toString()
                viewHolder.button.setBackgroundResource(R.drawable.workout_exercise_button_clicked_background)
            }
            else if (currentReps < 0) {
                setsXReps[position] = originalReps
                viewHolder.button.text = originalReps.toString()
                viewHolder.button.setBackgroundResource(R.drawable.workout_exercise_button_clicked_background)
            }
            else { // currentReps == 0
                setsXReps[position] = -originalReps
                viewHolder.button.text = originalReps.toString()
                viewHolder.button.setBackgroundResource(R.drawable.workout_exercise_button_background)
            }
            notifyDataSetChanged()

            Log.d(
                "WorkoutListAdapter",
                "New reps value: ${setsXReps[position]}"
            )
        }
    }

    override fun getItemCount(): Int {
        return setsXReps.size
    }

}