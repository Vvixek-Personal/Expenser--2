package com.example.data

import android.content.Context
import androidx.room.withTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

sealed class CloudSyncResult {
    data class Success(val message: String) : CloudSyncResult()
    data class Failure(val error: String, val isOffline: Boolean = false) : CloudSyncResult()
}

class CloudSyncManager private constructor(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _lastSyncTimestamp = MutableStateFlow("Never")
    val lastSyncTimestamp: StateFlow<String> = _lastSyncTimestamp.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: CloudSyncManager? = null
        fun getInstance(context: Context): CloudSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudSyncManager(context).also { INSTANCE = it }
            }
        }
    }

    private fun formatTimestamp(ts: Long): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ts))
    }

    private fun friendlyErrorMessage(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            e is java.net.UnknownHostException || e is java.net.SocketTimeoutException || msg.contains("UNAVAILABLE", ignoreCase = true) ->
                "Network connection unavailable. Please check your internet connection."
            msg.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Access denied to cloud vault. Please sign in again."
            msg.contains("UNAUTHENTICATED", ignoreCase = true) ->
                "Authentication expired. Please sign in again."
            else -> "Cloud operation could not be completed. Please try again."
        }
    }

    suspend fun performSync(
        database: FinanceDatabase,
        currencyCode: String,
        currencySymbol: String,
        currencyName: String,
        monthlyBudget: Double
    ): CloudSyncResult {
        val user = auth.currentUser ?: return CloudSyncResult.Failure("User not signed in. Please sign in to sync cloud data.")
        val userId = user.uid
        val dao = database.financeDao()

        return try {
            val data = mutableMapOf<String, Any>()
            data["currencyCode"] = currencyCode
            data["currencySymbol"] = currencySymbol
            data["currencyName"] = currencyName
            data["monthlyBudget"] = monthlyBudget
            val now = System.currentTimeMillis()
            data["lastSync"] = now

            val expenses = dao.getExpensesSnapshot()
            data["expenses"] = expenses.map { it.toMap() }

            val accounts = dao.getAccountsSnapshot()
            data["accounts"] = accounts.map { it.toMap() }

            val budgets = dao.getBudgetsSnapshot()
            data["budgets"] = budgets.map { it.toMap() }

            val goals = dao.getSavingsGoalsSnapshot()
            data["goals"] = goals.map { it.toMap() }

            val recurringRules = dao.getRecurringRulesSnapshot()
            data["recurringRules"] = recurringRules.map { it.toMap() }

            val reminders = dao.getRemindersSnapshot()
            data["reminders"] = reminders.map { it.toMap() }

            firestore.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .await()

            _lastSyncTimestamp.value = formatTimestamp(now)
            CloudSyncResult.Success("Cloud Sync Complete")
        } catch (e: Exception) {
            CloudSyncResult.Failure(friendlyErrorMessage(e))
        }
    }

    suspend fun restoreFromCloud(
        database: FinanceDatabase,
        currencyCallback: (String, String, String, Double) -> Unit
    ): CloudSyncResult {
        val user = auth.currentUser ?: return CloudSyncResult.Failure("User not signed in. Please sign in to restore cloud data.")
        val userId = user.uid
        val dao = database.financeDao()

        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (!doc.exists()) return CloudSyncResult.Failure("No cloud data found in your cloud vault.")

            val currencyCode = doc.getString("currencyCode") ?: "INR"
            val currencySymbol = doc.getString("currencySymbol") ?: "₹"
            val currencyName = doc.getString("currencyName") ?: "Indian Rupee"
            val monthlyBudget = doc.getDouble("monthlyBudget")
                ?: doc.getDouble("monthly_budget")
                ?: 0.0

            @Suppress("UNCHECKED_CAST")
            val rawAccounts = doc.get("accounts") as? List<Map<String, Any?>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val rawExpenses = doc.get("expenses") as? List<Map<String, Any?>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val rawBudgets = doc.get("budgets") as? List<Map<String, Any?>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val rawGoals = doc.get("goals") as? List<Map<String, Any?>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val rawRules = doc.get("recurringRules") as? List<Map<String, Any?>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val rawReminders = doc.get("reminders") as? List<Map<String, Any?>> ?: emptyList()

            // Parse objects before modifying the database
            val parsedAccounts = rawAccounts.map { it.toAccount() }
            val parsedExpenses = rawExpenses.map { it.toExpense() }
            val parsedBudgets = rawBudgets.map { it.toBudget() }
            val parsedGoals = rawGoals.map { it.toSavingsGoal() }
            val parsedRules = rawRules.map { it.toRecurringRule() }
            val parsedReminders = rawReminders.map { it.toReminderEntity() }

            // Take a pre-restore safety copy of SQLite database file
            PreMigrationBackup.backupDatabaseBeforeMigration(context, DATABASE_NAME)

            database.withTransaction {
                dao.clearAllData()
                // Insert accounts first to satisfy foreign key constraints
                parsedAccounts.forEach { dao.insertAccount(it) }
                parsedExpenses.forEach { dao.insertExpense(it) }
                parsedBudgets.forEach { dao.insertBudget(it) }
                parsedGoals.forEach { dao.insertSavingsGoal(it) }
                parsedRules.forEach { dao.insertRecurringRule(it) }
                parsedReminders.forEach { dao.insertReminder(it) }
            }

            currencyCallback(currencyCode, currencySymbol, currencyName, monthlyBudget)
            val lastSync = doc.getLong("lastSync") ?: System.currentTimeMillis()
            _lastSyncTimestamp.value = formatTimestamp(lastSync)
            CloudSyncResult.Success("Cloud Restore Complete: Restored ${parsedExpenses.size} transactions, ${parsedAccounts.size} accounts, ${parsedBudgets.size} budgets, and ${parsedGoals.size} goals.")
        } catch (e: Exception) {
            CloudSyncResult.Failure(friendlyErrorMessage(e))
        }
    }
}
