package com.example.nutrition.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.work.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FullBackupManager {
    private const val BACKUP_FILE_NAME = "NutritionApp_FullBackup.zip"
    private const val AUTO_BACKUP_PREFIX = "NutritionApp_Auto_"

    fun createAndShareBackup(context: Context) {
        val appContext = context.applicationContext
        val backupFile = File(appContext.cacheDir, BACKUP_FILE_NAME)

        try {
            performBackupToStream(appContext, FileOutputStream(backupFile))
            val uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                backupFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(
                Intent.createChooser(intent, "Backup sichern...")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Fehler beim Exportieren", Toast.LENGTH_SHORT).show()
        }
    }

    fun createAutoBackup(context: Context, folderUriString: String): Boolean {
        return try {
            val folderUri = Uri.parse(folderUriString)
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return false

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fileName = "$AUTO_BACKUP_PREFIX$timestamp.zip"

            val newFile = folder.createFile("application/zip", fileName) ?: return false

            context.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                performBackupToStream(context, outputStream)
            }

            rotateBackups(folder)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun performBackupToStream(context: Context, outputStream: OutputStream) {
        try {
            val dbFile = context.getDatabasePath("nutrition-db")
            if (dbFile.exists()) {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { it.moveToFirst() }
                db.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ZipOutputStream(outputStream).use { zos ->
            val dbFile = context.getDatabasePath("nutrition-db")
            listOf(dbFile, File(dbFile.path + "-shm"), File(dbFile.path + "-wal")).forEach { file ->
                if (file.exists()) addToZip(file, "database/${file.name}", zos)
            }

            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (sharedPrefsDir.exists()) {
                sharedPrefsDir.listFiles()?.forEach { file ->
                    addToZip(file, "prefs/${file.name}", zos)
                }
            }
        }
    }

    private fun rotateBackups(folder: DocumentFile) {
        val backups = folder.listFiles()
            .filter { it.name?.startsWith(AUTO_BACKUP_PREFIX) == true }
            .sortedByDescending { it.lastModified() }

        if (backups.size > 5) {
            backups.drop(5).forEach { it.delete() }
        }
    }

    private fun addToZip(file: File, zipPath: String, zos: ZipOutputStream) {
        val entry = ZipEntry(zipPath)
        zos.putNextEntry(entry)
        FileInputStream(file).use { fis -> fis.copyTo(zos) }
        zos.closeEntry()
    }

    fun restoreBackup(context: Context, uri: Uri) {
        try {
            context.deleteDatabase("nutrition-db")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.isDirectory) {
                            zis.closeEntry()
                            entry = zis.nextEntry
                            continue
                        }

                        val pureFileName = entry.name.substringAfterLast("/")
                        val targetFile = when {
                            entry.name.startsWith("database/") -> context.getDatabasePath(
                                pureFileName
                            )

                            entry.name.startsWith("prefs/") -> File(
                                context.applicationInfo.dataDir,
                                "shared_prefs/$pureFileName"
                            )

                            else -> null
                        }

                        targetFile?.let { file ->
                            file.parentFile?.mkdirs()
                            if (file.exists()) file.delete()
                            FileOutputStream(file).use { fos -> zis.copyTo(fos) }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Backup importiert! App startet neu...", Toast.LENGTH_LONG)
                    .show()
                Handler(Looper.getMainLooper()).postDelayed({
                    Process.killProcess(Process.myPid())
                    System.exit(0)
                }, 800)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Fehler beim Importieren", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateBackupSchedule(context: Context) {
        val prefs = context.getSharedPreferences("NutritionAppPrefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("auto_backup_enabled", false)
        val workManager = WorkManager.getInstance(context)

        if (!isEnabled) {
            workManager.cancelUniqueWork("NutritionAutoBackup")
            return
        }

        val frequency = prefs.getString("auto_backup_frequency", "Täglich")
        val intervalDays = if (frequency == "Wöchentlich") 7L else 1L
        val hour = prefs.getInt("auto_backup_hour", 2)
        val minute = prefs.getInt("auto_backup_minute", 0)

        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(intervalDays, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiresStorageNotLow(true).build())
            .build()

        workManager.enqueueUniquePeriodicWork(
            "NutritionAutoBackup",
            ExistingPeriodicWorkPolicy.UPDATE,
            backupRequest
        )
    }
}

class BackupWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val sharedPrefs =
            applicationContext.getSharedPreferences("NutritionAppPrefs", Context.MODE_PRIVATE)
        val folderUri = sharedPrefs.getString("auto_backup_folder_uri", null)
        val isEnabled = sharedPrefs.getBoolean("auto_backup_enabled", false)

        return if (isEnabled && folderUri != null) {
            val success = FullBackupManager.createAutoBackup(applicationContext, folderUri)
            if (success) Result.success() else Result.retry()
        } else {
            Result.failure()
        }
    }
}