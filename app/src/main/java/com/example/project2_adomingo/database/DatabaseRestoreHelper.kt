package com.example.project2_adomingo.database

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import androidx.room.Room

/** Helper class to manage backing up and restoring the database.
 * @param context the application using this.
 * @constructor Access the database instance using given context.
 */
class DatabaseRestoreHelper(private val context: Context) {
    private var database: StronkLiftsDatabase = StronkLiftsDatabase.getDatabase(context)

    /**
     * Replaces database with the backup database file.
     * (Assuming Room takes care of the existing WAL and SHM files.)
     * @return true if successful
     * @return false if unsuccessful
     */
    suspend fun restoreFromBackup(backupDirectory: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Get backup database file
                val downloadedDatabaseFile = File(backupDirectory, "stronklifts_database")

                // Close current database instance
                database.close()

                // Replace database with backup
                database = Room.databaseBuilder(context, StronkLiftsDatabase::class.java, "stronklifts_database")
                    .createFromFile(downloadedDatabaseFile)
                    .fallbackToDestructiveMigration() // in case of failure
                    .build()

                true
            } catch(e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
