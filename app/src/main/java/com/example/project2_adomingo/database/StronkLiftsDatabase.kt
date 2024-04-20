package com.example.project2_adomingo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Room Database class
@Database(entities = [
    User::class,
    ScheduleDate::class,
    Workout::class,
    Exercise::class,
    WorkoutExercise::class,
    WorkoutHistory::class,
    ExerciseHistory::class,
    ExerciseSetHistory::class],
    //exportSchema = false,
    version = 14)
@TypeConverters(Converters::class)
abstract class StronkLiftsDatabase : RoomDatabase() {
    abstract fun stronkLiftsDao(): StronkLiftsDao

    companion object {
        @Volatile
        private var INSTANCE: StronkLiftsDatabase? = null
        fun getDatabase(context: Context): StronkLiftsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StronkLiftsDatabase::class.java,
                    "stronklifts_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}