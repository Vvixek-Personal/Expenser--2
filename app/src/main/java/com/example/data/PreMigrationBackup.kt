package com.example.data

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PreMigrationBackup {

    fun getBackupDir(context: Context): File {
        val dir = File(context.filesDirs[0], "db_backups")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private val Context.filesDirs: Array<File>
        get() = arrayOf(this.filesDir)

    fun backupDatabaseBeforeMigration(context: Context, dbName: String): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) return false

        return try {
            var version = 0
            try {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.path,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )
                version = db.version
                db.close()
            } catch (e: Exception) {
                // Ignore
            }

            val backupDir = getBackupDir(context)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "db_v${version}_${timestamp}.db")

            // Checkpoint WAL so uncommitted WAL rows are flushed to the main database file
            try {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.path,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { it.moveToFirst() }
                db.close()
            } catch (e: Exception) {
                // Ignore
            }

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun pruneOldBackups(backupDir: File, maxToKeep: Int) {
        val files = backupDir.listFiles() ?: return
        if (files.size <= maxToKeep) return
        
        files.sortBy { it.lastModified() }
        val toDelete = files.size - maxToKeep
        for (i in 0 until toDelete) {
            files[i].delete()
        }
    }
}
