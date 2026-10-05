package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["accountId"])],
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double = 0.0,
    val amountMinor: Long = 0,
    val category: String,
    val date: Long,
    val note: String? = null,
    val imagePath: String? = null,
    val type: String = "EXPENSE", // "EXPENSE", "INCOME"
    val currencyCode: String = "INR",
    val accountId: Long = 0,
    val kind: String = "REGULAR", // "REGULAR", "SAVINGS", "RECURRING"
    val goalId: Long? = null,
    val recurringRuleId: Long? = null
)

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "BANK",
    val currencyCode: String = "INR",
    val openingBalance: Double = 0.0,
    val balance: Double = 0.0,
    val openingBalanceMinor: Long = 0,
    val balanceMinor: Long = 0
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amountLimit: Double = 0.0,
    val amountLimitMinor: Long = 0,
    val monthYear: String,
    val currencyCode: String = "INR"
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val targetAmountMinor: Long = 0,
    val currentAmountMinor: Long = 0,
    val targetDate: Long = 0,
    val frequency: String = "MONTHLY",
    val contributionAmount: Double = 0.0,
    val contributionAmountMinor: Long = 0,
    val isAutoGap: Boolean = true,
    val iconTag: String = "🎯",
    val category: String = "Savings",
    val imageUri: String? = null,
    val currencyCode: String = "INR"
)

@Entity(tableName = "recurring_rules")
data class RecurringRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountMinor: Long = 0,
    val category: String,
    val frequency: String,
    val startDate: Long,
    val nextDueDate: Long,
    val isActive: Boolean = true,
    val currencyCode: String = "INR",
    val accountId: Long = 0,
    val note: String? = null
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val isEnabled: Boolean = true
)
