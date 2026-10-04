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

    suspend fun performSync(
        database: FinanceDatabase,
        currencyCode: String,
        currencySymbol: String,
        currencyName: String,
        monthlyBudget: Double
    ): CloudSyncResult {
        val user = auth.currentUser ?: return CloudSyncResult.Failure("User not signed in")
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

            firestore.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .await()

            _lastSyncTimestamp.value = formatTimestamp(now)
            CloudSyncResult.Success("Cloud Sync Complete")
        } catch (e: Exception) {
            CloudSyncResult.Failure(e.message ?: "Unknown error")
        }
    }

    suspend fun restoreFromCloud(
        database: FinanceDatabase,
        currencyCallback: (String, String, String, Double) -> Unit
    ): CloudSyncResult {
        val user = auth.currentUser ?: return CloudSyncResult.Failure("User not signed in")
        val userId = user.uid
        val dao = database.financeDao()

        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (!doc.exists()) return CloudSyncResult.Failure("No cloud data found")

            val currencyCode = doc.getString("currencyCode") ?: "INR"
            val currencySymbol = doc.getString("currencySymbol") ?: "₹"
            val currencyName = doc.getString("currencyName") ?: "Indian Rupee"
            val monthlyBudget = doc.getDouble("monthly_budget") ?: 0.0

            database.withTransaction {
                dao.clearAllData()
                // ...
            }

            currencyCallback(currencyCode, currencySymbol, currencyName, monthlyBudget)
            val lastSync = doc.getLong("lastSync") ?: System.currentTimeMillis()
            _lastSyncTimestamp.value = formatTimestamp(lastSync)
            CloudSyncResult.Success("Cloud Restore Complete")
        } catch (e: Exception) {
            CloudSyncResult.Failure(e.message ?: "Unknown error")
        }
    }
}
