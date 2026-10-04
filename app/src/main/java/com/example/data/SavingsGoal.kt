package com.example.data

import androidx.room.*

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val targetAmountMinor: Long = 0L,
    val currentAmountMinor: Long = 0L,
    val targetDate: Long = 0,
    val frequency: String = "WEEKLY",
    val contributionAmount: Double = 0.0,
    val contributionAmountMinor: Long = 0L,
    val isAutoGap: Boolean = true,
    val iconTag: String = "🎮",
    val category: String = "Saving",
    val imageUri: String? = null,
    val currencyCode: String = "INR"
)
