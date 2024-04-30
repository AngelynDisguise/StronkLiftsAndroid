package com.example.project2_adomingo

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Room
import com.example.project2_adomingo.database.StronkLiftsDatabase

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections

const val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 0

class SettingsActivity : AppCompatActivity() {

    private val dbPath = "/data/data/com.example.project2_adomingo/databases/stronklifts_database"
    private val dbPathShm = "/data/data/com.example.project2_adomingo/databases/stronklifts_database-shm"
    private val dbPathWal = "/data/data/com.example.project2_adomingo/databases/stronklifts_database-wal"

    private lateinit var drive: Drive

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

//        val lastSyncedText: TextView = findViewById(R.id.last_backed_up_text)
//        lastSyncedText.text =
        Log.d("SettingsActivity", "DB Path: $dbPath")
    }

    fun onBackUp(view: View) {
        startForBackUpResult.launch(getGoogleSignInClient(this).signInIntent)
    }

    private fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }

    private val startForBackUpResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("SettingsActivity", "Result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (result.data != null) {
                val task: Task<GoogleSignInAccount>? =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)

                Log.d("SettingsActivity", "Got intent data: $result.data\ntask: $task")

                task?.addOnSuccessListener { googleAccount ->
                    val credential = GoogleAccountCredential.usingOAuth2(
                        this, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
                    )
                    credential.selectedAccount = googleAccount.account

                    Log.d("SettingsActivity", "Account logged in successfully: ${googleAccount.account}")

                    // get Drive Instance
                    drive = Drive
                        .Builder(
                            AndroidHttp.newCompatibleTransport(),
                            JacksonFactory.getDefaultInstance(),
                            credential
                        )
                        .setApplicationName(this.getString(R.string.app_name))
                        .build()

                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        try {
                            Log.d("SettingsActivity", "Attempting to upload to drive...")
                            val result = upload(drive)
                            Log.d("SettingsActivity", "$result")

                            // Get current time
                            val now = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            val formattedTime = now.format(formatter)

                            // Update text view
                            val lastBackedUp: TextView = findViewById(R.id.last_backed_up_text)
                            val text = "Last backed up: $formattedTime"
                            lastBackedUp.text = text

                            Toast.makeText(applicationContext, "Database files backed up to Google Drive!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("SettingsActivity", "Error uploading files", e)
                        }
                    }


                }
            } else {
                Toast.makeText(this, "Google Login Error!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun upload(drive: Drive) = withContext(Dispatchers.IO) {
        try {
            val files = drive.files().list()
                .setQ("name='stronkLiftsAppDataFolder' and mimeType='application/vnd.google-apps.folder'")
                .setSpaces("drive")
                .execute()

            if (files.files.isEmpty()) {
                createFolder(drive)
            } else {
                val folderId = files.files[0].id
                updateFiles(drive, folderId)
            }
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error checking folder")
            e.printStackTrace()
        }
    }

    private fun createFolder(drive: Drive) {
        val metadata = com.google.api.services.drive.model.File()
            .setParents(listOf("root"))
            .setMimeType("application/vnd.google-apps.folder")
            .setName("stronkLiftsAppDataFolder")

        try {
            val folder = drive.files().create(metadata).setFields("id").execute()
            if (folder != null) {
                Log.d("SettingsActivity", "Folder created: ${folder.id}")
                updateFiles(drive, folder.id)
            }
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error creating folder")
            e.printStackTrace()
        }

    }

    private fun updateFiles(drive: Drive, folderId: String) {
        val fileNames = listOf("stronklifts_database", "stronklifts_database-shm", "stronklifts_database-wal")
        val filePaths = listOf(dbPath, dbPathShm, dbPathWal)

        try {
            for (i in fileNames.indices) {
                val fileName = fileNames[i]
                val filePath = filePaths[i]

                val existingFiles = drive.files().list()
                    .setQ("name='$fileName' and parents in '$folderId'")
                    .setSpaces("drive")
                    .execute()
                if (existingFiles != null) {
                    if (existingFiles.files.isEmpty()) {
                        createFile(drive, folderId, fileName, filePath)
                    } else {
                        updateFile(drive, existingFiles.files[0].id, fileName, filePath)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error updating files")
            e.printStackTrace()
        }
    }

    private fun createFile(drive: Drive, folderId: String, fileName: String, filePath: String) {
        val storageFile = com.google.api.services.drive.model.File().apply {
            parents = listOf(folderId)
            name = fileName
        }

        val file = File(filePath)
        val mediaContent = FileContent("", file)

        val res = drive.files().create(storageFile, mediaContent).execute()
        if (res != null) {
            Log.d("SettingsActivity", "File created: ${res.id}")
        } else {
            Log.e("SettingsActivity", "Error creating file")
        }
    }

    private fun updateFile(drive: Drive, fileId: String, fileName: String, filePath: String) {
        val file = File(filePath)
        val mediaContent = FileContent("", file)

        val res = drive.files().update(fileId, null, mediaContent).execute()
        if (res != null) {
            Log.d("SettingsActivity", "File updated: ${res.id}")
        } else {
            Log.e("SettingsActivity", "Error updating file")
        }
    }

    fun onImport(view: View) {
        startForImportResult.launch(getGoogleSignInClient(this).signInIntent)
    }

    private val startForImportResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("SettingsActivity", "Result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (result.data != null) {
                val task: Task<GoogleSignInAccount>? =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)

                Log.d("SettingsActivity", "Got intent data: $result.data\ntask: $task")

                task?.addOnSuccessListener { googleAccount ->
                    val credential = GoogleAccountCredential.usingOAuth2(
                        this, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
                    )
                    credential.selectedAccount = googleAccount.account

                    Log.d("SettingsActivity", "Account logged in successfully: ${googleAccount.account}")

                    // get Drive Instance
                    drive = Drive
                        .Builder(
                            AndroidHttp.newCompatibleTransport(),
                            JacksonFactory.getDefaultInstance(),
                            credential
                        )
                        .setApplicationName(this.getString(R.string.app_name))
                        .build()

                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        Log.d("SettingsActivity", "Attempting to import DB from drive...")
                        val result = retrieveAndImportFiles(drive)
                        Log.d("SettingsActivity", "$result")
                        //Toast.makeText(applicationContext, "Database files imported from Google Drive!", Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Google Login Error!", Toast.LENGTH_LONG).show()
            }
        }
    }


    private suspend fun retrieveAndImportFiles(drive: Drive) = withContext(Dispatchers.IO) {
        try {
            // Create temp folder location
            val downloadDirectory = File(filesDir, "temp_downloads")
            if (downloadDirectory.exists()) {
                downloadDirectory.deleteRecursively()
            }
            downloadDirectory.mkdirs()

            // Check for app data folder in drive
            val folderName = "stronkLiftsAppDataFolder"
            val q = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
            val results = drive.files().list().setQ(q).execute()
            if (results.files.size > 0) {


                val folderId = results.files[0].id
                val query = "'$folderId' in parents"

                // Retrieve Files
                val fileList = drive.files().list().setQ(query).setSpaces("drive").execute()
                Log.d("SettingsActivity", "fileList: $fileList")
                fileList?.let {
                    for (file in fileList.files) {
                        Log.d("SettingsActivity", "File ID: ${file.id}, Name: ${file.name}")
                        val fileName = file.name
                        val outputStream = FileOutputStream(File(downloadDirectory, fileName))

                        drive.files().get(file.id)
                            .executeMediaAndDownloadTo(outputStream)
                    }

                    importDatabaseFiles(downloadDirectory)
                }
            } else {
                Log.e("SettingsActivity", "Error retrieving files: ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun importDatabaseFiles(downloadDirectory: File) {
        try {
            // Close DB
            var res = this.deleteDatabase("stronklifts_database")
            Log.e("SettingsActivity", "Deleting database... $res")

            // Replace DB files
            res = File(dbPath).delete()
            Log.e("SettingsActivity", "Deleting stronklifts_database... $res")
            var newFile = File(downloadDirectory, "stronklifts_database").copyTo(File(dbPath), true)
            Log.e("SettingsActivity", "Copying new stronklifts_database... $newFile")

            res = File(dbPathShm).delete()
            Log.e("SettingsActivity", "Deleting stronklifts_database-shm... $res")
            newFile = File(downloadDirectory, "stronklifts_database-shm").copyTo(File(dbPathShm), true)
            Log.e("SettingsActivity", "Copying new stronklifts_database... $newFile")

            res = File(dbPathWal).delete()
            Log.e("SettingsActivity", "Deleting stronklifts_database-wal... $res")
            newFile = File(downloadDirectory, "stronklifts_database-wal").copyTo(File(dbPathWal), true)
            Log.e("SettingsActivity", "Copying new stronklifts_database-wal... $newFile")

            // Rebuild the Room database
            StronkLiftsDatabase.getDatabase(this)
            Log.e("SettingsActivity", "Rebuilding database...")

        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error importing database: ", e)
        } finally {
            // Delete temp files
            downloadDirectory.deleteRecursively()
        }
    }

    fun onClickHome(view: View) {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}