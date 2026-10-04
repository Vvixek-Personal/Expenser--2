package com.example.data

import androidx.room.*

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double = 0.0,
    val amountMinor: Long = 0L,
    val category: String = "Others",
    val date: Long = System.currentTimeMillis(),
    val note: String? = null,
    val imagePath: String? = null,
    val type: String = "EXPENSE",
    val currencyCode: String = "INR",
    val accountId: Long = 1L,
    val kind: String = "EXPENSE",
    val goalId: Long? = null, // Changed to Long? for ID consistency
    val recurringRuleId: Long? = null
)
