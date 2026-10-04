package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val isEnabled: Boolean = true
)
