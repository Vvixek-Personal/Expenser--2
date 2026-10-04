package com.example.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

fun getFinanceDbMigrations(defaultCurrency: String): Array<Migration> {
    return arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, 
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        createMigration9To10(defaultCurrency),
        createMigration10To11(defaultCurrency),
        MIGRATION_11_12
    )
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `type` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `amountLimit` REAL NOT NULL, `monthYear` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `savings_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `currentAmount` REAL NOT NULL DEFAULT 0.0, `targetDate` INTEGER NOT NULL DEFAULT 0, `frequency` TEXT NOT NULL DEFAULT 'WEEKLY', `contributionAmount` REAL NOT NULL DEFAULT 0.0, `isAutoGap` INTEGER NOT NULL DEFAULT 1, `iconTag` TEXT NOT NULL DEFAULT '🎮', `category` TEXT NOT NULL DEFAULT 'Saving', `imageUri` TEXT)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Placeholder or actual migration
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        addColumnIfNotExists(db, "expenses", "type", "TEXT NOT NULL DEFAULT 'EXPENSE'")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        addColumnIfNotExists(db, "expenses", "currencyCode", "TEXT NOT NULL DEFAULT 'INR'")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        addColumnIfNotExists(db, "transactions", "currencyCode", "TEXT NOT NULL DEFAULT 'INR'")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `dueDate` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No-op or already handled
    }
}

fun createMigration9To10(defaultCurrency: String): Migration = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Migration to minor units
        // 1. Alter expenses to add amountMinor
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `amountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `expenses` SET `amountMinor` = CAST(`amount` * 100 AS INTEGER)") // Simple heuristic
        
        // 2. Alter accounts to add balanceMinor
        db.execSQL("ALTER TABLE `accounts` ADD COLUMN `balanceMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `accounts` SET `balanceMinor` = CAST(`balance` * 100 AS INTEGER)")
        
        // 3. Alter budgets to add amountLimitMinor
        db.execSQL("ALTER TABLE `budgets` ADD COLUMN `amountLimitMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `budgets` SET `amountLimitMinor` = CAST(`amountLimit` * 100 AS INTEGER)")
        
        // 4. Alter savings_goals to add minor unit fields
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `targetAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `currentAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `contributionAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT '$defaultCurrency'")
        
        db.execSQL("UPDATE `savings_goals` SET `targetAmountMinor` = CAST(`targetAmount` * 100 AS INTEGER)")
        db.execSQL("UPDATE `savings_goals` SET `currentAmountMinor` = CAST(`currentAmount` * 100 AS INTEGER)")
        db.execSQL("UPDATE `savings_goals` SET `contributionAmountMinor` = CAST(`contributionAmount` * 100 AS INTEGER)")
    }
}

fun createMigration10To11(defaultCurrency: String): Migration = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `accounts` ADD COLUMN `openingBalanceMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `accounts` SET `openingBalanceMinor` = `balanceMinor`")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `frequency` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT)")
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `recurringRuleId` INTEGER")
    }
}

fun addColumnIfNotExists(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnDef: String) {
    val cursor = db.query("PRAGMA table_info(`$tableName`)")
    var exists = false
    cursor.use {
        val nameIdx = it.getColumnIndex("name")
        while (it.moveToNext()) {
            if (it.getString(nameIdx) == columnName) {
                exists = true
                break
            }
        }
    }
    if (!exists) {
        db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDef")
    }
}
