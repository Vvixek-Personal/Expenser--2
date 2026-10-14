package com.example.data

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

private const val TAG = "LocalRecoveryManager"
private const val SNAPSHOT_FORMAT_VERSION = 2
private const val MAX_RETAINED_SNAPSHOTS = 10

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

    private fun sha256(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(data)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun listSnapshots(context: Context): List<SnapshotMeta> {
        val dir = getSnapshotDir(context)
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".json") } ?: return emptyList()

        return files.map { file ->
            try {
                val jsonStr = file.readText(Charsets.UTF_8)
                val json = JSONObject(jsonStr)
                val payloadStr = json.optString("payload", "")
                val expectedChecksum = json.optString("checksum", "")
                val isValidChecksum = expectedChecksum.isNotBlank() &&
                    sha256(payloadStr.toByteArray(Charsets.UTF_8)) == expectedChecksum

                val payload = if (payloadStr.isNotBlank()) JSONObject(payloadStr) else json
                val meta = payload.optJSONObject("meta")
                val createdAt = meta?.optLong("createdAt", file.lastModified()) ?: file.lastModified()
                val counts = meta?.optJSONObject("counts")

                SnapshotMeta(
                    fileName = file.name,
                    createdAt = createdAt,
                    fileSize = file.length(),
                    accountsCount = counts?.optInt("accounts", 0) ?: 0,
                    expensesCount = counts?.optInt("expenses", 0) ?: 0,
                    budgetsCount = counts?.optInt("budgets", 0) ?: 0,
                    savingsGoalsCount = counts?.optInt("savingsGoals", 0) ?: 0,
                    remindersCount = counts?.optInt("reminders", 0) ?: 0,
                    isValid = isValidChecksum || (!json.has("checksum") && json.has("accounts"))
                )
            } catch (e: Exception) {
                Log.w(TAG, "Corrupt or unreadable snapshot: ${file.name}", e)
                SnapshotMeta(
                    fileName = file.name,
                    createdAt = file.lastModified(),
                    fileSize = file.length(),
                    isValid = false
                )
            }
        }.sortedByDescending { it.createdAt }
    }

    suspend fun createSnapshot(context: Context, db: FinanceDatabase): Boolean {
        return try {
            val dao = db.financeDao()
            val accounts = dao.getAccountsSnapshot()
            val expenses = dao.getExpensesSnapshot()
            val budgets = dao.getBudgetsSnapshot()
            val goals = dao.getSavingsGoalsSnapshot()
            val reminders = dao.getRemindersSnapshot()
            val rules = dao.getRecurringRulesSnapshot()

            val prefs = context.getSharedPreferences(AppSettingsManager.PREFS_NAME, Context.MODE_PRIVATE)
            val currencyCode = prefs.getString("currency_code", "INR") ?: "INR"
            val currencySymbol = prefs.getString("currency_symbol", "₹") ?: "₹"
            val currencyName = prefs.getString("currency_name", "Indian Rupee") ?: "Indian Rupee"
            val monthlyBudget = prefs.getFloat("monthly_budget", 0f).toDouble()

            val now = System.currentTimeMillis()

            val payload = JSONObject()
            payload.put("formatVersion", SNAPSHOT_FORMAT_VERSION)
            payload.put("schemaVersion", FINANCE_DB_VERSION)
            payload.put("createdAt", now)

            val meta = JSONObject().apply {
                put("createdAt", now)
                val counts = JSONObject().apply {
                    put("accounts", accounts.size)
                    put("expenses", expenses.size)
                    put("budgets", budgets.size)
                    put("savingsGoals", goals.size)
                    put("reminders", reminders.size)
                    put("recurringRules", rules.size)
                }
                put("counts", counts)
            }
            payload.put("meta", meta)

            val settingsObj = JSONObject().apply {
                put("currencyCode", currencyCode)
                put("currencySymbol", currencySymbol)
                put("currencyName", currencyName)
                put("monthlyBudget", monthlyBudget)
            }
            payload.put("settings", settingsObj)

            // Accounts
            val accountsArr = JSONArray()
            accounts.forEach { acc ->
                val obj = JSONObject().apply {
                    put("id", acc.id)
                    put("name", acc.name)
                    put("type", acc.type)
                    put("currencyCode", acc.currencyCode)
                    put("openingBalance", acc.openingBalance)
                    put("balance", acc.balance)
                    put("openingBalanceMinor", acc.openingBalanceMinor)
                    put("balanceMinor", acc.balanceMinor)
                }
                accountsArr.put(obj)
            }
            payload.put("accounts", accountsArr)

            // Recurring Rules
            val rulesArr = JSONArray()
            rules.forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("title", r.title)
                    put("amountMinor", r.amountMinor)
                    put("category", r.category)
                    put("frequency", r.frequency)
                    put("startDate", r.startDate)
                    put("nextDueDate", r.nextDueDate)
                    put("isActive", r.isActive)
                    put("currencyCode", r.currencyCode)
                    put("accountId", r.accountId)
                    put("note", r.note ?: "")
                }
                rulesArr.put(obj)
            }
            payload.put("recurringRules", rulesArr)

            // Expenses
            val expensesArr = JSONArray()
            expenses.forEach { exp ->
                val obj = JSONObject().apply {
                    put("id", exp.id)
                    put("amount", exp.amount)
                    put("amountMinor", exp.amountMinor)
                    put("category", exp.category)
                    put("date", exp.date)
                    put("note", exp.note ?: "")
                    put("imagePath", exp.imagePath ?: "")
                    put("type", exp.type)
                    put("currencyCode", exp.currencyCode)
                    put("accountId", exp.accountId)
                    put("kind", exp.kind)
                    put("goalId", exp.goalId ?: -1L)
                    put("recurringRuleId", exp.recurringRuleId ?: -1L)
                }
                expensesArr.put(obj)
            }
            payload.put("expenses", expensesArr)

            // Budgets
            val budgetsArr = JSONArray()
            budgets.forEach { b ->
                val obj = JSONObject().apply {
                    put("id", b.id)
                    put("category", b.category)
                    put("amountLimit", b.amountLimit)
                    put("amountLimitMinor", b.amountLimitMinor)
                    put("monthYear", b.monthYear)
                    put("currencyCode", b.currencyCode)
                }
                budgetsArr.put(obj)
            }
            payload.put("budgets", budgetsArr)

            // Savings Goals
            val goalsArr = JSONArray()
            goals.forEach { g ->
                val obj = JSONObject().apply {
                    put("id", g.id)
                    put("name", g.name)
                    put("targetAmount", g.targetAmount)
                    put("currentAmount", g.currentAmount)
                    put("targetAmountMinor", g.targetAmountMinor)
                    put("currentAmountMinor", g.currentAmountMinor)
                    put("targetDate", g.targetDate)
                    put("frequency", g.frequency)
                    put("contributionAmount", g.contributionAmount)
                    put("contributionAmountMinor", g.contributionAmountMinor)
                    put("isAutoGap", g.isAutoGap)
                    put("iconTag", g.iconTag)
                    put("category", g.category)
                    put("imageUri", g.imageUri ?: "")
                    put("currencyCode", g.currencyCode)
                }
                goalsArr.put(obj)
            }
            payload.put("savingsGoals", goalsArr)

            // Reminders
            val remindersArr = JSONArray()
            reminders.forEach { rem ->
                val obj = JSONObject().apply {
                    put("id", rem.id)
                    put("text", rem.text)
                    put("dueDate", rem.dueDate)
                    put("isCompleted", rem.isCompleted)
                    put("isEnabled", rem.isEnabled)
                }
                remindersArr.put(obj)
            }
            payload.put("reminders", remindersArr)

            val payloadString = payload.toString()
            val checksum = sha256(payloadString.toByteArray(Charsets.UTF_8))

            val wrapper = JSONObject().apply {
                put("formatVersion", SNAPSHOT_FORMAT_VERSION)
                put("checksum", checksum)
                put("payload", payloadString)
            }

            val dir = getSnapshotDir(context)
            val fileName = "snapshot_${now}.json"
            val tempFile = File(dir, "${fileName}.tmp")
            val targetFile = File(dir, fileName)

            FileOutputStream(tempFile).use { out ->
                out.write(wrapper.toString(2).toByteArray(Charsets.UTF_8))
                out.flush()
            }
            tempFile.renameTo(targetFile)

            pruneOldSnapshots(context, MAX_RETAINED_SNAPSHOTS)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create local recovery snapshot", e)
            false
        }
    }

    suspend fun restoreSnapshot(
        context: Context,
        db: FinanceDatabase,
        fileName: String,
        allowUnverifiedLegacy: Boolean = false,
        onCurrencyRestored: (String, String, String, Double) -> Unit
    ): Boolean {
        val file = File(getSnapshotDir(context), fileName)
        if (!file.exists() || !file.canRead()) {
            Log.w(TAG, "Snapshot file does not exist or unreadable: $fileName")
            return false
        }

        return try {
            val jsonStr = file.readText(Charsets.UTF_8)
            val root = JSONObject(jsonStr)

            val payloadObj = if (root.has("payload")) {
                val payloadString = root.getString("payload")
                val expectedChecksum = root.optString("checksum", "")
                val actualChecksum = sha256(payloadString.toByteArray(Charsets.UTF_8))
                if (expectedChecksum.isBlank() || expectedChecksum != actualChecksum) {
                    Log.w(TAG, "Snapshot checksum mismatch for $fileName")
                    return false
                }
                JSONObject(payloadString)
            } else {
                if (!allowUnverifiedLegacy) {
                    Log.w(TAG, "Snapshot lacks checksum and unverified legacy restore not permitted: $fileName")
                    return false
                }
                root
            }

            // Parse accounts
            val rawAccounts = payloadObj.optJSONArray("accounts") ?: JSONArray()
            val accounts = (0 until rawAccounts.length()).map { idx ->
                val obj = rawAccounts.getJSONObject(idx)
                val cur = obj.optString("currencyCode", "INR")
                val minor = if (obj.has("balanceMinor")) obj.getLong("balanceMinor") else Money.fromDouble(obj.optDouble("balance", 0.0), cur)
                val opMinor = if (obj.has("openingBalanceMinor")) obj.getLong("openingBalanceMinor") else minor
                val name = obj.optString("name", "Account").trim()
                if (name.isBlank()) throw IllegalArgumentException("Account at index $idx has empty name")
                Account(
                    id = obj.optLong("id", 0L),
                    name = name,
                    type = obj.optString("type", "BANK"),
                    currencyCode = cur,
                    openingBalance = Money.toDouble(opMinor, cur),
                    balance = Money.toDouble(minor, cur),
                    openingBalanceMinor = opMinor,
                    balanceMinor = minor
                )
            }

            val validAccountIds = accounts.map { it.id }.toSet()

            // Parse recurring rules
            val rawRules = payloadObj.optJSONArray("recurringRules") ?: JSONArray()
            val rules = (0 until rawRules.length()).map { idx ->
                val obj = rawRules.getJSONObject(idx)
                val cur = obj.optString("currencyCode", "INR")
                val amtMinor = obj.optLong("amountMinor", 0L)
                val accId = obj.optLong("accountId", 0L)
                RecurringRule(
                    id = obj.optLong("id", 0L),
                    title = obj.optString("title", "Recurring").ifBlank { "Recurring Bill" },
                    amountMinor = amtMinor,
                    category = obj.optString("category", "General"),
                    frequency = obj.optString("frequency", "MONTHLY"),
                    startDate = obj.optLong("startDate", System.currentTimeMillis()),
                    nextDueDate = obj.optLong("nextDueDate", System.currentTimeMillis()),
                    isActive = obj.optBoolean("isActive", true),
                    currencyCode = cur,
                    accountId = if (validAccountIds.contains(accId)) accId else (accounts.firstOrNull()?.id ?: 1L),
                    note = obj.optString("note", "")
                )
            }

            // Parse expenses
            val rawExpenses = payloadObj.optJSONArray("expenses") ?: JSONArray()
            val expenses = (0 until rawExpenses.length()).map { idx ->
                val obj = rawExpenses.getJSONObject(idx)
                val cur = obj.optString("currencyCode", "INR")
                val minor = if (obj.has("amountMinor")) obj.getLong("amountMinor") else Money.fromDouble(obj.optDouble("amount", 0.0), cur)
                val gId = obj.optLong("goalId", -1L)
                val rId = obj.optLong("recurringRuleId", -1L)
                val accId = obj.optLong("accountId", 0L)
                val resolvedAccountId = if (validAccountIds.contains(accId)) accId else (accounts.firstOrNull()?.id ?: 1L)
                Expense(
                    id = obj.optLong("id", 0L),
                    amount = Money.toDouble(minor, cur),
                    amountMinor = minor,
                    category = obj.optString("category", "General"),
                    date = obj.optLong("date", System.currentTimeMillis()),
                    note = if (obj.has("note")) obj.getString("note").ifBlank { null } else null,
                    imagePath = if (obj.has("imagePath")) obj.getString("imagePath").ifBlank { null } else null,
                    type = obj.optString("type", "EXPENSE"),
                    currencyCode = cur,
                    accountId = resolvedAccountId,
                    kind = obj.optString("kind", "REGULAR"),
                    goalId = if (gId > 0L) gId else null,
                    recurringRuleId = if (rId > 0L) rId else null
                )
            }

            // Parse budgets
            val rawBudgets = payloadObj.optJSONArray("budgets") ?: JSONArray()
            val budgets = (0 until rawBudgets.length()).map { idx ->
                val obj = rawBudgets.getJSONObject(idx)
                val cur = obj.optString("currencyCode", "INR")
                val limitMinor = if (obj.has("amountLimitMinor")) obj.getLong("amountLimitMinor") else Money.fromDouble(obj.optDouble("amountLimit", 0.0), cur)
                Budget(
                    id = obj.optLong("id", 0L),
                    category = obj.optString("category", "General"),
                    amountLimit = Money.toDouble(limitMinor, cur),
                    amountLimitMinor = limitMinor,
                    monthYear = obj.optString("monthYear", "ALL"),
                    currencyCode = cur
                )
            }

            // Parse goals
            val rawGoals = payloadObj.optJSONArray("savingsGoals") ?: JSONArray()
            val goals = (0 until rawGoals.length()).map { idx ->
                val obj = rawGoals.getJSONObject(idx)
                val cur = obj.optString("currencyCode", "INR")
                val targetMinor = if (obj.has("targetAmountMinor")) obj.getLong("targetAmountMinor") else Money.fromDouble(obj.optDouble("targetAmount", 0.0), cur)
                val curMinor = if (obj.has("currentAmountMinor")) obj.getLong("currentAmountMinor") else Money.fromDouble(obj.optDouble("currentAmount", 0.0), cur)
                val contribMinor = if (obj.has("contributionAmountMinor")) obj.getLong("contributionAmountMinor") else Money.fromDouble(obj.optDouble("contributionAmount", 0.0), cur)
                SavingsGoal(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", "Goal").ifBlank { "Savings Goal" },
                    targetAmount = Money.toDouble(targetMinor, cur),
                    currentAmount = Money.toDouble(curMinor, cur),
                    targetAmountMinor = targetMinor,
                    currentAmountMinor = curMinor,
                    targetDate = obj.optLong("targetDate", 0L),
                    frequency = obj.optString("frequency", "MONTHLY"),
                    contributionAmount = Money.toDouble(contribMinor, cur),
                    contributionAmountMinor = contribMinor,
                    isAutoGap = obj.optBoolean("isAutoGap", true),
                    iconTag = obj.optString("iconTag", "🎯"),
                    category = obj.optString("category", "Savings"),
                    imageUri = if (obj.has("imageUri")) obj.getString("imageUri").ifBlank { null } else null,
                    currencyCode = cur
                )
            }

            // Parse reminders
            val rawReminders = payloadObj.optJSONArray("reminders") ?: JSONArray()
            val reminders = (0 until rawReminders.length()).map { idx ->
                val obj = rawReminders.getJSONObject(idx)
                ReminderEntity(
                    id = obj.optLong("id", 0L),
                    text = obj.optString("text", "Reminder"),
                    dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    isEnabled = obj.optBoolean("isEnabled", true)
                )
            }

            // Mandatory Pre-restore Safety Snapshot
            val safetySnapshotCreated = createSnapshot(context, db)
            if (!safetySnapshotCreated) {
                Log.e(TAG, "Pre-restore safety snapshot failed! Aborting restoration.")
                return false
            }

            // Atomic transactional restore
            val dao = db.financeDao()
            db.withTransaction {
                dao.clearAllData()
                accounts.forEach { dao.insertAccount(it) }
                rules.forEach { dao.insertRecurringRule(it) }
                expenses.forEach { dao.insertExpense(it) }
                budgets.forEach { dao.insertBudget(it) }
                goals.forEach { dao.insertSavingsGoal(it) }
                reminders.forEach { dao.insertReminder(it) }

                // Verification of restored record counts
                val restoredAccounts = dao.getAccountsSnapshot()
                val restoredExpenses = dao.getExpensesSnapshot()
                if (restoredAccounts.size != accounts.size || restoredExpenses.size != expenses.size) {
                    throw IllegalStateException("Record count mismatch after restore! Accounts: ${restoredAccounts.size}/${accounts.size}, Expenses: ${restoredExpenses.size}/${expenses.size}")
                }
            }

            // Restore settings
            val settings = payloadObj.optJSONObject("settings")
            val currencyCode = settings?.optString("currencyCode", "INR") ?: "INR"
            val currencySymbol = settings?.optString("currencySymbol", "₹") ?: "₹"
            val currencyName = settings?.optString("currencyName", "Indian Rupee") ?: "Indian Rupee"
            val monthlyBudget = settings?.optDouble("monthlyBudget", 0.0) ?: 0.0

            val prefs = context.getSharedPreferences(AppSettingsManager.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString("currency_code", currencyCode)
                .putString("currency_symbol", currencySymbol)
                .putString("currency_name", currencyName)
                .putFloat("monthly_budget", monthlyBudget.toFloat())
                .apply()

            onCurrencyRestored(currencyCode, currencySymbol, currencyName, monthlyBudget)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Restoration failed for snapshot $fileName", e)
            false
        }
    }

    fun deleteSnapshot(context: Context, fileName: String): Boolean {
        val file = File(getSnapshotDir(context), fileName)
        return if (file.exists()) file.delete() else false
    }

    fun pruneOldSnapshots(context: Context, maxToKeep: Int) {
        val dir = getSnapshotDir(context)
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".json") } ?: return
        if (files.size <= maxToKeep) return
        files.sortBy { it.lastModified() }
        val toDelete = files.size - maxToKeep
        for (i in 0 until toDelete) {
            files[i].delete()
        }
    }
}
