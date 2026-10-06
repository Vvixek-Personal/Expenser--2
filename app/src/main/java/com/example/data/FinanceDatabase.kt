package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

const val FINANCE_DB_VERSION = 13
const val DATABASE_NAME = "finance_database"

@Database(
    entities = [
        Expense::class,
        Account::class,
        Budget::class,
        SavingsGoal::class,
        RecurringRule::class,
        ReminderEntity::class
    ],
    version = FINANCE_DB_VERSION,
    exportSchema = true
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context, defaultCurrency: String = "INR"): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                .addMigrations(*getFinanceDbMigrations(defaultCurrency))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
