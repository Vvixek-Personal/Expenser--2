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
        // No-op (already matches v4)
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
        // Ensure budgets has currencyCode
        addColumnIfNotExists(db, "budgets", "currencyCode", "TEXT NOT NULL DEFAULT 'INR'")
    }
}

fun createMigration9To10(defaultCurrency: String): Migration = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Minor units migration
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `amountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `expenses` SET `amountMinor` = CAST(ROUND(`amount` * 100) AS INTEGER)")

        db.execSQL("ALTER TABLE `accounts` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT '$defaultCurrency'")
        db.execSQL("ALTER TABLE `accounts` ADD COLUMN `balanceMinor` INTEGER NOT NULL DEFAULT 0")
        
        // Infer currency for accounts from transactions
        db.execSQL("""
            UPDATE accounts SET currencyCode = (
                SELECT currencyCode FROM transactions 
                WHERE transactions.accountId = accounts.id 
                LIMIT 1
            ) WHERE EXISTS (
                SELECT 1 FROM transactions WHERE transactions.accountId = accounts.id
            )
        """.trimIndent())

        // Update balanceMinor based on currency (simple heuristic for common ones)
        db.execSQL("UPDATE accounts SET balanceMinor = CAST(ROUND(balance * 100) AS INTEGER) WHERE currencyCode NOT IN ('JPY', 'KRW', 'CLP', 'VND', 'PYG')")
        db.execSQL("UPDATE accounts SET balanceMinor = CAST(ROUND(balance * 1) AS INTEGER) WHERE currencyCode IN ('JPY', 'KRW', 'CLP', 'VND', 'PYG')")
        db.execSQL("UPDATE accounts SET balanceMinor = CAST(ROUND(balance * 1000) AS INTEGER) WHERE currencyCode IN ('KWD', 'BHD', 'OMR', 'JOD', 'LYD', 'TND')")

        db.execSQL("ALTER TABLE `budgets` ADD COLUMN `amountLimitMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `budgets` SET `amountLimitMinor` = CAST(ROUND(`amountLimit` * 100) AS INTEGER)")

        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `targetAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `currentAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `contributionAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT '$defaultCurrency'")

        db.execSQL("UPDATE `savings_goals` SET `targetAmountMinor` = CAST(ROUND(`targetAmount` * 100) AS INTEGER)")
        db.execSQL("UPDATE `savings_goals` SET `currentAmountMinor` = CAST(ROUND(`currentAmount` * 100) AS INTEGER)")
        db.execSQL("UPDATE `savings_goals` SET `contributionAmountMinor` = CAST(ROUND(`contributionAmount` * 100) AS INTEGER)")
        
        // Update transactions to minor units
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `amountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `transactions` SET `amountMinor` = CAST(ROUND(`amount` * 100) AS INTEGER)")
    }
}

fun createMigration10To11(defaultCurrency: String): Migration = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Unified Ledger Migration
        db.execSQL("ALTER TABLE `accounts` ADD COLUMN `openingBalance` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `accounts` ADD COLUMN `openingBalanceMinor` INTEGER NOT NULL DEFAULT 0")
        
        // 2. Add missing columns to expenses (recreate table to add FK and not null constraints properly)
        db.execSQL("CREATE TABLE `expenses_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `type` TEXT NOT NULL, `currencyCode` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `kind` TEXT NOT NULL, `goalId` INTEGER, `recurringRuleId` INTEGER, FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        
        // Copy old expenses to new (default accountId to 1, kind to 'REGULAR')
        // We assume account 1 exists or will be created. 
        db.execSQL("""
            INSERT INTO expenses_new (id, amount, amountMinor, category, date, note, imagePath, type, currencyCode, accountId, kind)
            SELECT id, amount, amountMinor, category, date, note, imagePath, type, currencyCode, 1, 'REGULAR'
            FROM expenses
        """.trimIndent())
        
        // Move transactions to expenses
        db.execSQL("""
            INSERT INTO expenses_new (amount, amountMinor, category, date, note, imagePath, type, currencyCode, accountId, kind)
            SELECT amount, amountMinor, category, timestamp, title || (CASE WHEN note IS NOT NULL THEN ': ' || note ELSE '' END), imagePath, type, currencyCode, accountId, 'REGULAR'
            FROM transactions
        """.trimIndent())

        // Compute opening balances for accounts
        // openingBalance = currentBalance - (sum of all expenses/incomes in that account)
        db.execSQL("""
            UPDATE accounts SET openingBalanceMinor = balanceMinor - (
                SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE -amountMinor END), 0)
                FROM expenses_new WHERE expenses_new.accountId = accounts.id
            )
        """.trimIndent())
        db.execSQL("UPDATE accounts SET openingBalance = CAST(openingBalanceMinor AS REAL) / 100.0")

        db.execSQL("DROP TABLE expenses")
        db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_accountId` ON `expenses` (`accountId`)")

        // Keep transactions as legacy
        db.execSQL("ALTER TABLE transactions RENAME TO transactions_legacy")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `frequency` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT)")
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
