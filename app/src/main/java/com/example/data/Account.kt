package com.example.data

import androidx.room.*

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String,
    val currencyCode: String = "INR",
    val openingBalance: Double = 0.0,
    val balance: Double = 0.0,
    val openingBalanceMinor: Long = 0L,
    val balanceMinor: Long = 0L
)
