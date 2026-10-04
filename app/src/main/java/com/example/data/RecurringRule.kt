package com.example.data

import androidx.room.*

@Entity(tableName = "recurring_rules")
data class RecurringRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountMinor: Long,
    val category: String,
    val frequency: String,
    val startDate: Long,
    val nextDueDate: Long,
    val isActive: Boolean = true,
    val currencyCode: String = "INR",
    val accountId: Long = 1L,
    val note: String? = null
)
