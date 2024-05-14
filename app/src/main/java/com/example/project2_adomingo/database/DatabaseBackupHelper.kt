package com.example.project2_adomingo.database

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

const val APP_DATA_FOLDER: String = "stronkLiftsAppData"
const val DATABASE_FILENAME: String = "stronklifts_database"
const val APPLICATION_NAME: String = "StronkLiftsAndroid"

/** Helper class to manage Google Drive operations, mainly uploading and downloading.
 * @param context the application using this.
 * @param credential Google Account Credentials (obtained from sign-in).
 * @constructor Creates a drive service connected to a Google Account.
*/
class DatabaseBackupHelper(private val context: Context, private val credential: GoogleAccountCredential) {

    /* Set up Drive Service with credentials */
    private val driveService: Drive = Drive
        .Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName(APPLICATION_NAME)
        .build()

    /**
     * Gets the id of the app data folder in Google Drive.
     * @return the folder id of a folder name in Drive.
     * @return null if it doesn't exist.
    */
    private fun getAppDataFolder(): String? {
        // Check for app data folder in Drive
        val appDataFolder: FileList = driveService.files().list()
            .setQ("name = '$APP_DATA_FOLDER' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
            .setSpaces("drive")
            .setFields("nextPageToken, files(id, name)")
            .execute()

        // App data folder exist?
        return if (appDataFolder.files.isEmpty()) {
            val folderId = appDataFolder.files[0].id
            Log.d("DatabaseBackupHelper", "Google Drive SUCCESS: Got folder: $folderId.")
            folderId
        } else {
            Log.d("DatabaseBackupHelper", "Google Drive ERROR: $APP_DATA_FOLDER app data folder not found.")
            null
        }
    }

    /**
     * Creates an new empty appData folder in root directory of Google Drive.
     * @return the folder id of created folder.
     * @return null if creation unsuccessful.
     */
    private fun createAppDataFolder(): String? {
        val metadata = com.google.api.services.drive.model.File()
            .setParents(listOf("root"))
            .setMimeType("application/vnd.google-apps.folder")
            .setName(APP_DATA_FOLDER)

        val folder = driveService.files()
            .create(metadata)
            .setFields("id")
            .execute()

        folder?.let {
            Log.d("DatabaseBackupHelper", "Google Drive SUCCESS: Created folder ${folder.id}.")
            return folder.id
        } ?: let {
            Log.d("DatabaseBackupHelper", "Google Drive ERROR: Failed to create $APP_DATA_FOLDER app data folder.")
            return null
        }
    }


    /**
     * Upload current database file to Google Drive.
     */
    suspend fun uploadDatabaseFile() {
        withContext(Dispatchers.IO) {
            // Get appData folder (create if it doesn't exist)
            val folderId = getAppDataFolder() ?: createAppDataFolder()

                // Success?
                folderId?.let {
                    try {
                        // Retrieve existing database backup files
                        val existingFiles = driveService.files().list()
                            .setQ("name = '$DATABASE_FILENAME' and parents in '$folderId'")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name)")
                            .execute()

                        // Create File
                        val path = context.getDatabasePath(DATABASE_FILENAME).absolutePath
                        val file = File(path)
                        val mediaContent = FileContent("", file)

                        // Success?
                        existingFiles?.let {
                            Log.d("DatabaseBackupHelper", "Google Drive: Uploading current database file...")


                            /* No backup file exists */
                            if (existingFiles.files.isEmpty()) {
                                Log.d("DatabaseBackupHelper", "Google Drive: No existing file found, creating file...")

                                // Create Drive file
                                val driveFile = com.google.api.services.drive.model.File().apply {
                                    parents = listOf(folderId)
                                    name = DATABASE_FILENAME
                                }

                                val result = driveService.files()
                                    .create(driveFile, mediaContent)
                                    .execute()

                                result?.let {
                                    Log.e("DatabaseBackupHelper", "UPLOAD SUCCESS: Backup database file created and uploaded successfully!")
                                } ?: let {
                                    throw Exception("Create failed (unexpected null result)")
                                }
                            }
                            /* Backup exists - update backup file */
                            else {
                                Log.d("DatabaseBackupHelper", "Google Drive: Existing database file found, updating file...")

                                val result = driveService.files()
                                    .update(existingFiles.files[0].id, null, mediaContent)
                                    .execute()

                                result?.let {
                                    Log.e("DatabaseBackupHelper", "UPLOAD SUCCESS: Backup database file updated and uploaded successfully!")
                                } ?: let {
                                    throw Exception("Update failed (unexpected null result)")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("DatabaseBackupHelper", "UPLOAD FAIL: Google Drive could not upload current database file.")
                    }
                } ?:  Log.d("DatabaseBackupHelper", "UPLOAD FAIL: Google Drive failed to get or create $APP_DATA_FOLDER folder.")
        }
    }

    // Download Database file from Google Drive
    /**
     * Download backup database file (located in appData folder) from Google Drive.
     * @param backupDirectory where to put the backup database file in local storage
     * @throws java.io.IOException if downloading to backupDirectory fails
     */
    suspend fun downloadDatabaseFile(backupDirectory: File) {
        withContext(Dispatchers.IO) {
            // Check for app data folder in Drive
            val folderId = getAppDataFolder()
            // App data folder exist in Drive?
            folderId?. let {
                Log.d("DatabaseBackupHelper", "Google Drive: Downloading database file...")

                try {
                    // Retrieve database file
                    val fileResult = driveService.files().list()
                        .setQ("name='$DATABASE_FILENAME' and trashed=false and '$folderId' in parents")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .execute()

                    // Database file exist?
                    if (fileResult.files.isNotEmpty()) {
                        val fileId = fileResult.files[0].id
                        val fileMetadata: com.google.api.services.drive.model.File =
                            driveService.files().get(fileId).execute()
                        val fileName = fileMetadata.name
                        Log.d("DatabaseBackupHelper", "Google Drive SUCCESS: Got file: $fileName.")

                        // Clean up backup directory (create if none exists)
                        if (backupDirectory.exists()) {
                            backupDirectory.deleteRecursively()
                        }
                        backupDirectory.mkdirs()

                        // Download database file to backup directory
                        driveService.files() // not checking result here, return is a Unit/unknown.
                            .get(fileId)
                            .executeMediaAndDownloadTo(FileOutputStream(File(backupDirectory, DATABASE_FILENAME))) // throws exception

                        Log.e("DatabaseBackupHelper", "DOWNLOAD SUCCESS: Backup database file downloaded to Drive successfully!")
                    } else {
                        Log.e("DatabaseBackupHelper", "DOWNLOAD FAIL: $DATABASE_FILENAME database file not found in $APP_DATA_FOLDER in Drive.")
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                    Log.e("DatabaseBackupHelper", "DOWNLOAD FAIL: Google Drive could not download backup database to $APP_DATA_FOLDER.")
                }
            } ?: Log.e("DatabaseBackupHelper", "DOWNLOAD FAIL: $APP_DATA_FOLDER folder doesn't exist in Drive.")
        }
    }
}
