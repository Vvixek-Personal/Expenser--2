package com.example.data

import androidx.room.*

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String = "Others",
    val amountLimit: Double = 0.0,
    val amountLimitMinor: Long = 0L,
    val monthYear: String = "",
    val currencyCode: String = "INR"
)
