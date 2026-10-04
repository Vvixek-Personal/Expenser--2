package com.example.data

import android.content.Context
import java.io.File

data class SnapshotMeta(
    val fileName: String,
    val createdAt: Long,
    val fileSize: Long = 0L,
    val accountsCount: Int = 0,
    val expensesCount: Int = 0,
    val budgetsCount: Int = 0,
    val savingsGoalsCount: Int = 0,
    val remindersCount: Int = 0,
    val isValid: Boolean = true
)

object LocalRecoveryManager {
    fun getSnapshotDir(context: Context): File {
        val dir = File(context.filesDir, "snapshots")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun listSnapshots(context: Context): List<SnapshotMeta> {
        val dir = getSnapshotDir(context)
        return dir.listFiles()?.map { file ->
            SnapshotMeta(
                fileName = file.name,
                createdAt = file.lastModified(),
                fileSize = file.length()
                // Counts would ideally be stored in the file name or a companion json
            )
        } ?: emptyList()
    }

    suspend fun createSnapshot(context: Context, db: FinanceDatabase): Boolean {
        // Implementation here (exporting DB or entities to file)
        return true
    }

    suspend fun restoreSnapshot(
        context: Context,
        db: FinanceDatabase,
        fileName: String,
        onCurrencyRestored: (String, String, String, Double) -> Unit
    ): Boolean {
        // Implementation here
        return true
    }

    fun deleteSnapshot(context: Context, fileName: String): Boolean {
        val file = File(getSnapshotDir(context), fileName)
        return if (file.exists()) file.delete() else false
    }
}
