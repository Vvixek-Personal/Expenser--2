package com.example.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

fun getFinanceDbMigrations(defaultCurrency: String): Array<Migration> {
    return arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        createMigration9To10(defaultCurrency),
        createMigration10To11(defaultCurrency),
        MIGRATION_11_12,
        createMigration12To13(defaultCurrency)
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
        addColumnIfNotExists(db, "expenses", "currencyCode", "TEXT NOT NULL DEFAULT 'INR'")
        addColumnIfNotExists(db, "budgets", "currencyCode", "TEXT NOT NULL DEFAULT 'INR'")
    }
}

fun createMigration9To10(defaultCurrency: String): Migration = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Minor units migration
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `amountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `expenses` SET `amountMinor` = CAST(ROUND(`amount` * ${unit("currencyCode")}) AS INTEGER)")

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

        // Update balanceMinor based on currency
        db.execSQL("UPDATE accounts SET balanceMinor = CAST(ROUND(balance * ${unit("currencyCode")}) AS INTEGER)")

        db.execSQL("ALTER TABLE `budgets` ADD COLUMN `amountLimitMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `budgets` SET `amountLimitMinor` = CAST(ROUND(`amountLimit` * ${unit("'$defaultCurrency'")}) AS INTEGER)")

        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `targetAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `currentAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `contributionAmountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT '$defaultCurrency'")

        db.execSQL("UPDATE `savings_goals` SET `targetAmountMinor` = CAST(ROUND(`targetAmount` * ${unit("currencyCode")}) AS INTEGER)")
        db.execSQL("UPDATE `savings_goals` SET `currentAmountMinor` = CAST(ROUND(`currentAmount` * ${unit("currencyCode")}) AS INTEGER)")
        db.execSQL("UPDATE `savings_goals` SET `contributionAmountMinor` = CAST(ROUND(`contributionAmount` * ${unit("currencyCode")}) AS INTEGER)")
        
        // Update transactions to minor units
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `amountMinor` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `transactions` SET `amountMinor" + "` = CAST(ROUND(`amount` * ${unit("currencyCode")}) AS INTEGER)")
    }
}

fun createMigration10To11(defaultCurrency: String): Migration = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        normalizeLedgerSchema(db, defaultCurrency)
    }
}

fun createMigration12To13(defaultCurrency: String): Migration = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        normalizeLedgerSchema(db, defaultCurrency)
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `frequency` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT)")
        addColumnIfNotExists(db, "expenses", "recurringRuleId", "INTEGER")
    }
}

fun addColumnIfNotExists(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnDef: String) {
    if (!tableExists(db, tableName)) return
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

private const val ZERO_DEC = "('JPY','KRW','CLP','VND','PYG')"
private const val THREE_DEC = "('KWD','BHD','OMR','JOD','LYD','TND')"

/** SQL: minor units per 1 major unit for the currency column (or literal) [cur]. */
private fun unit(cur: String) =
    "(CASE WHEN $cur IN $ZERO_DEC THEN 1.0 WHEN $cur IN $THREE_DEC THEN 1000.0 ELSE 100.0 END)"

private fun net(accRef: String) =
    "(SELECT COALESCE(SUM(CASE WHEN x.type = 'INCOME' THEN x.amountMinor ELSE -x.amountMinor END), 0) " +
        "FROM expenses_n x WHERE x.accountId = $accRef)"

private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean =
    db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$table'").use { it.moveToFirst() }

private fun columnsOf(db: SupportSQLiteDatabase, table: String): Set<String> {
    val out = mutableSetOf<String>()
    db.query("PRAGMA table_info(`$table`)").use { c ->
        val i = c.getColumnIndex("name")
        while (c.moveToNext()) out.add(c.getString(i))
    }
    return out
}

/**
 * Rebuilds expenses/accounts/budgets/savings_goals/recurring_rules into exactly the
 * shape of Entities.kt, whatever older layout they were in. Safe to run repeatedly.
 */
fun normalizeLedgerSchema(db: SupportSQLiteDatabase, defaultCurrency: String) {
    val hasTx = tableExists(db, "transactions")
    val hasLegacy = tableExists(db, "transactions_legacy")
    val hasExp = tableExists(db, "expenses")
    val hasAcc = tableExists(db, "accounts")
    val hasBud = tableExists(db, "budgets")
    val hasGoal = tableExists(db, "savings_goals")
    val hasRec = tableExists(db, "recurring_rules")
    
    val e = if (hasExp) columnsOf(db, "expenses") else emptySet()
    val a = if (hasAcc) columnsOf(db, "accounts") else emptySet()
    val b = if (hasBud) columnsOf(db, "budgets") else emptySet()
    val g = if (hasGoal) columnsOf(db, "savings_goals") else emptySet()
    val r = if (hasRec) columnsOf(db, "recurring_rules") else emptySet()
    val lit = "'" + defaultCurrency.replace("'", "''") + "'"

    // ---------- expenses ----------
    if (hasExp) {
        db.execSQL("DROP TABLE IF EXISTS expenses_n")
        db.execSQL("CREATE TABLE expenses_n (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, amount REAL NOT NULL, amountMinor INTEGER NOT NULL, category TEXT NOT NULL, date INTEGER NOT NULL, note TEXT, imagePath TEXT, type TEXT NOT NULL, currencyCode TEXT NOT NULL, accountId INTEGER NOT NULL, kind TEXT NOT NULL, goalId INTEGER, recurringRuleId INTEGER, FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        val amountExpr = if ("amount" in e) "amount" else "CAST(amountMinor AS REAL) / ${unit("currencyCode")}"
        val minorExpr = if ("amountMinor" in e) "amountMinor" else "CAST(ROUND(amount * ${unit("currencyCode")}) AS INTEGER)"
        val accExpr = if ("accountId" in e) "accountId" else "COALESCE((SELECT MIN(id) FROM accounts), 1)"
        val kindExpr = if ("kind" in e) "kind" else "'REGULAR'"
        val goalExpr = if ("goalId" in e) "goalId" else "NULL"
        val recExpr = if ("recurringRuleId" in e) "recurringRuleId" else "NULL"
        db.execSQL("INSERT INTO expenses_n (id, amount, amountMinor, category, date, note, imagePath, type, currencyCode, accountId, kind, goalId, recurringRuleId) SELECT id, $amountExpr, $minorExpr, category, date, note, imagePath, type, currencyCode, $accExpr, $kindExpr, $goalExpr, $recExpr FROM expenses")

        if (hasTx) {
            val t = columnsOf(db, "transactions")
            val tAmt = if ("amount" in t) "amount" else "CAST(amountMinor AS REAL) / ${unit("currencyCode")}"
            val tMin = if ("amountMinor" in t) "amountMinor" else "CAST(ROUND(amount * ${unit("currencyCode")}) AS INTEGER)"
            db.execSQL("INSERT INTO expenses_n (amount, amountMinor, category, date, note, imagePath, type, currencyCode, accountId, kind) SELECT $tAmt, $tMin, category, timestamp, title || (CASE WHEN note IS NOT NULL THEN ': ' || note ELSE '' END), imagePath, type, currencyCode, accountId, 'REGULAR' FROM transactions")
            if (!hasLegacy) db.execSQL("ALTER TABLE transactions RENAME TO transactions_legacy")
            else db.execSQL("DROP TABLE transactions")
        }
    }

    // ---------- accounts ----------
    if (hasAcc) {
        db.execSQL("DROP TABLE IF EXISTS accounts_n")
        db.execSQL("CREATE TABLE accounts_n (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL, currencyCode TEXT NOT NULL, openingBalance REAL NOT NULL, balance REAL NOT NULL, openingBalanceMinor INTEGER NOT NULL, balanceMinor INTEGER NOT NULL)")
        val cur = if ("currencyCode" in a) "currencyCode" else lit
        val hasOpen = "openingBalanceMinor" in a
        val hasBal = "balanceMinor" in a
        val balSrc: String? = when {
            hasBal -> "balanceMinor"
            "balance" in a -> "CAST(ROUND(balance * ${unit(cur)}) AS INTEGER)"
            else -> null
        }
        val openMinor = when {
            hasOpen -> "openingBalanceMinor"
            balSrc != null -> "($balSrc - ${net("accounts.id")})"
            else -> "0"
        }
        val balMinor = when {
            balSrc != null -> balSrc
            hasOpen -> "(openingBalanceMinor + ${net("accounts.id")})"
            else -> "0"
        }
        val openD = if ("openingBalance" in a) "openingBalance" else "0.0"
        val balD = if ("balance" in a) "balance" else "0.0"
        db.execSQL("INSERT INTO accounts_n (id, name, type, currencyCode, openingBalance, balance, openingBalanceMinor, balanceMinor) SELECT id, name, type, $cur, $openD, $balD, $openMinor, $balMinor FROM accounts")
        if ("openingBalance" !in a) db.execSQL("UPDATE accounts_n SET openingBalance = CAST(openingBalanceMinor AS REAL) / ${unit("currencyCode")}")
        if ("balance" !in a) db.execSQL("UPDATE accounts_n SET balance = CAST(balanceMinor AS REAL) / ${unit("currencyCode")}")
        // Expenses but no accounts at all: create a default "Cash" account so nothing is orphaned
        db.execSQL("INSERT INTO accounts_n (id, name, type, currencyCode, openingBalance, balance, openingBalanceMinor, balanceMinor) SELECT 1, 'Cash', 'CASH', $lit, 0.0, 0.0, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM accounts_n) AND EXISTS (SELECT 1 FROM expenses_n)")
        db.execSQL("UPDATE accounts_n SET balanceMinor = ${net("accounts_n.id")}, balance = CAST(${net("accounts_n.id")} AS REAL) / ${unit("currencyCode")} WHERE id = 1 AND name = 'Cash' AND openingBalanceMinor = 0 AND NOT EXISTS (SELECT 1 FROM accounts)")
    }

    // ---------- budgets ----------
    if (hasBud) {
        db.execSQL("DROP TABLE IF EXISTS budgets_n")
        db.execSQL("CREATE TABLE budgets_n (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category TEXT NOT NULL, amountLimit REAL NOT NULL, amountLimitMinor INTEGER NOT NULL, monthYear TEXT NOT NULL, currencyCode TEXT NOT NULL)")
        val bc = if ("currencyCode" in b) "currencyCode" else lit
        val bMin = if ("amountLimitMinor" in b) "amountLimitMinor" else "CAST(ROUND(amountLimit * ${unit(bc)}) AS INTEGER)"
        val bDbl = if ("amountLimit" in b) "amountLimit" else "CAST(amountLimitMinor AS REAL) / ${unit(bc)}"
        db.execSQL("INSERT INTO budgets_n (id, category, amountLimit, amountLimitMinor, monthYear, currencyCode) SELECT id, category, $bDbl, $bMin, monthYear, $bc FROM budgets")
    }

    // ---------- savings_goals ----------
    if (hasGoal) {
        db.execSQL("DROP TABLE IF EXISTS savings_goals_n")
        db.execSQL("CREATE TABLE savings_goals_n (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, targetAmount REAL NOT NULL, currentAmount REAL NOT NULL, targetAmountMinor INTEGER NOT NULL, currentAmountMinor INTEGER NOT NULL, targetDate INTEGER NOT NULL, frequency TEXT NOT NULL, contributionAmount REAL NOT NULL, contributionAmountMinor INTEGER NOT NULL, isAutoGap INTEGER NOT NULL, iconTag TEXT NOT NULL, category TEXT NOT NULL, imageUri TEXT, currencyCode TEXT NOT NULL)")
        val gc = if ("currencyCode" in g) "currencyCode" else lit
        fun dbl(d: String, m: String) = if (d in g) d else "CAST($m AS REAL) / ${unit(gc)}"
        fun minor(d: String, m: String) = if (m in g) m else "CAST(ROUND($d * ${unit(gc)}) AS INTEGER)"
        db.execSQL("INSERT INTO savings_goals_n (id, name, targetAmount, currentAmount, targetAmountMinor, currentAmountMinor, targetDate, frequency, contributionAmount, contributionAmountMinor, isAutoGap, iconTag, category, imageUri, currencyCode) SELECT id, name, ${dbl("targetAmount", "targetAmountMinor")}, ${dbl("currentAmount", "currentAmountMinor")}, ${minor("targetAmount", "targetAmountMinor")}, ${minor("currentAmount", "currentAmountMinor")}, targetDate, frequency, ${dbl("contributionAmount", "contributionAmountMinor")}, ${minor("contributionAmount", "contributionAmountMinor")}, isAutoGap, iconTag, category, imageUri, $gc FROM savings_goals")
    }

    // ---------- recurring_rules ----------
    if (hasRec) {
        db.execSQL("DROP TABLE IF EXISTS recurring_rules_n")
        db.execSQL("CREATE TABLE recurring_rules_n (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, amountMinor INTEGER NOT NULL, category TEXT NOT NULL, frequency TEXT NOT NULL, startDate INTEGER NOT NULL, nextDueDate INTEGER NOT NULL, isActive INTEGER NOT NULL, currencyCode TEXT NOT NULL, accountId INTEGER NOT NULL, note TEXT)")
        if (r.isNotEmpty()) {
            db.execSQL("INSERT INTO recurring_rules_n (id, title, amountMinor, category, frequency, startDate, nextDueDate, isActive, currencyCode, accountId, note) SELECT id, title, amountMinor, category, frequency, startDate, nextDueDate, isActive, currencyCode, accountId, note FROM recurring_rules")
        }
    }

    // ---------- swap new tables in ----------
    for (t in listOf("expenses", "accounts", "budgets", "savings_goals", "recurring_rules")) {
        if (tableExists(db, t + "_n")) {
            db.execSQL("DROP TABLE IF EXISTS $t")
            db.execSQL("ALTER TABLE ${t}_n RENAME TO $t")
        }
    }
    if (tableExists(db, "expenses") && tableExists(db, "accounts")) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_accountId ON expenses(accountId)")
    }
}
