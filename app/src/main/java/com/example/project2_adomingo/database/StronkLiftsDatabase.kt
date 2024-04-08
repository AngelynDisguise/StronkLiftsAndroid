package com.example.project2_adomingo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase

// Room Database class
@Database(entities = [Exercise::class, Workout::class], version = 14)
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