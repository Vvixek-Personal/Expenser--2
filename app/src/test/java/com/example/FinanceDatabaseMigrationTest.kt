package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinanceDatabaseMigrationTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun databaseVersionConstantIsThirteen() {
        assertEquals("FINANCE_DB_VERSION should be 13", 13, FINANCE_DB_VERSION)
    }

    @Test
    fun preMigrationBackupCreatesFilesSafely() {
        val dbName = "test_finance_database"
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(10) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE test_table (id INTEGER PRIMARY KEY)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val db = helper.writableDatabase
        db.close()
        helper.close()

        val success = PreMigrationBackup.backupDatabaseBeforeMigration(context, dbName)
        assertTrue("Pre-migration backup should return true when version < FINANCE_DB_VERSION", success)

        val backupDir = PreMigrationBackup.getBackupDir(context)
        val backupFiles = backupDir.listFiles { file -> file.isFile && file.name.startsWith("db_v10_") } ?: emptyArray()
        assertTrue("Timestamped backup file should exist in backup directory", backupFiles.isNotEmpty())

        // Cleanup
        context.deleteDatabase(dbName)
        backupDir.deleteRecursively()
    }

    @Test
    fun preMigrationBackupHandlesMissingDatabaseGracefully() {
        val success = PreMigrationBackup.backupDatabaseBeforeMigration(context, "non_existent_database_1234")
        assertFalse("Backup of non-existent database should return false without throwing", success)
    }

    @Test
    fun migration_8_9_addsCurrencyCodeColumn() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test_migration_8_9.db")
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(8) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Create tables as of version 8
                        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `type` TEXT NOT NULL DEFAULT 'EXPENSE')")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT)")
                        db.execSQL("INSERT INTO `expenses` (`amount`, `category`, `date`) VALUES (100.0, 'Food', 123456)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // Execute migration 8 -> 9
        MIGRATION_8_9.migrate(db)

        // Verify currencyCode column was added to expenses
        val cursorExpenses = db.query("PRAGMA table_info(`expenses`)")
        var hasCurrencyCodeExpenses = false
        cursorExpenses.use {
            val nameIndex = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (nameIndex != -1 && it.getString(nameIndex) == "currencyCode") {
                    hasCurrencyCodeExpenses = true
                    break
                }
            }
        }
        assertTrue("expenses table must contain currencyCode column after migration 8->9", hasCurrencyCodeExpenses)

        // Verify existing data is preserved
        val queryCursor = db.query("SELECT amount, currencyCode FROM expenses")
        queryCursor.use {
            assertTrue(it.moveToFirst())
            assertEquals(100.0, it.getDouble(0), 0.001)
            assertEquals("INR", it.getString(1))
        }

        db.close()
        helper.close()
        context.deleteDatabase("test_migration_8_9.db")
    }

    @Test
    fun migration_7_8_createsRemindersTable() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test_migration_7_8.db")
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(7) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `type` TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `amountLimit` REAL NOT NULL, `monthYear` TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `currencyCode` TEXT NOT NULL DEFAULT 'INR')")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // Execute migration 7 -> 8
        MIGRATION_7_8.migrate(db)

        // Verify reminders table exists
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='reminders'")
        var exists = false
        cursor.use {
            exists = it.moveToFirst()
        }
        assertTrue("reminders table must exist after migration 7->8", exists)

        db.close()
        helper.close()
        context.deleteDatabase("test_migration_7_8.db")
    }

    @Test
    fun migration_9_10_convertsAmountsToMinorAndInfersCurrencies() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test_migration_9_10.db")
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(9) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Create tables as of schema version 9
                        db.execSQL(
                            "CREATE TABLE `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `type` TEXT NOT NULL)"
                        )
                        db.execSQL(
                            "CREATE TABLE `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `currencyCode` TEXT NOT NULL DEFAULT 'INR', FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                        )
                        db.execSQL(
                            "CREATE TABLE `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `type` TEXT NOT NULL DEFAULT 'EXPENSE', `currencyCode` TEXT NOT NULL DEFAULT 'INR')"
                        )
                        db.execSQL(
                            "CREATE TABLE `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `amountLimit` REAL NOT NULL, `monthYear` TEXT NOT NULL)"
                        )
                        db.execSQL(
                            "CREATE TABLE `savings_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `currentAmount` REAL NOT NULL DEFAULT 0.0, `targetDate` INTEGER NOT NULL DEFAULT 0, `frequency` TEXT NOT NULL DEFAULT 'WEEKLY', `contributionAmount` REAL NOT NULL DEFAULT 0.0, `isAutoGap` INTEGER NOT NULL DEFAULT 1, `iconTag` TEXT NOT NULL DEFAULT '🎮', `category` TEXT NOT NULL DEFAULT 'Saving', `imageUri` TEXT)"
                        )

                        // Insert test data
                        // Account 1: 10000.0, with USD transactions
                        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`, `type`) VALUES (1, 'USD Bank', 10000.0, 'BANK')")
                        db.execSQL("INSERT INTO `transactions` (`id`, `title`, `amount`, `type`, `category`, `timestamp`, `accountId`, `currencyCode`) VALUES (1, 'USD Tx', 19.99, 'EXPENSE', 'General', 1000, 1, 'USD')")

                        // Account 2: 500.0, with JPY transactions
                        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`, `type`) VALUES (2, 'Tokyo Cash', 500.0, 'CASH')")
                        db.execSQL("INSERT INTO `transactions` (`id`, `title`, `amount`, `type`, `category`, `timestamp`, `accountId`, `currencyCode`) VALUES (2, 'JPY Tx', 500.0, 'EXPENSE', 'Food', 2000, 2, 'JPY')")

                        // Account 3: 1.234, with KWD transactions
                        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`, `type`) VALUES (3, 'Kuwait Wallet', 1.234, 'BANK')")
                        db.execSQL("INSERT INTO `transactions` (`id`, `title`, `amount`, `type`, `category`, `timestamp`, `accountId`, `currencyCode`) VALUES (3, 'KWD Tx', 1.234, 'EXPENSE', 'Bills', 3000, 3, 'KWD')")

                        // Expenses
                        db.execSQL("INSERT INTO `expenses` (`id`, `amount`, `category`, `date`, `currencyCode`) VALUES (1, 19.99, 'Shopping', 1000, 'USD')")
                        db.execSQL("INSERT INTO `expenses` (`id`, `amount`, `category`, `date`, `currencyCode`) VALUES (2, 500.0, 'Food', 2000, 'JPY')")
                        db.execSQL("INSERT INTO `expenses` (`id`, `amount`, `category`, `date`, `currencyCode`) VALUES (3, 1.234, 'Fuel', 3000, 'KWD')")

                        // Budget & Goal
                        db.execSQL("INSERT INTO `budgets` (`id`, `category`, `amountLimit`, `monthYear`) VALUES (1, 'Food', 500.0, '09-2026')")
                        db.execSQL("INSERT INTO `savings_goals` (`id`, `name`, `targetAmount`, `currentAmount`, `contributionAmount`) VALUES (1, 'Vacation', 2000.0, 500.0, 100.0)")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase

        // Execute migration 9 -> 10 with default currency INR
        val migration = createMigration9To10("INR")
        migration.migrate(db)

        // 1. Verify expenses conversion
        db.query("SELECT id, amountMinor, currencyCode FROM expenses ORDER BY id ASC").use { c ->
            assertTrue(c.moveToNext())
            assertEquals(1, c.getInt(0))
            assertEquals(1999L, c.getLong(1)) // USD 19.99 -> 1999
            assertEquals("USD", c.getString(2))

            assertTrue(c.moveToNext())
            assertEquals(2, c.getInt(0))
            assertEquals(500L, c.getLong(1)) // JPY 500 -> 500
            assertEquals("JPY", c.getString(2))

            assertTrue(c.moveToNext())
            assertEquals(3, c.getInt(0))
            assertEquals(1234L, c.getLong(1)) // KWD 1.234 -> 1234
            assertEquals("KWD", c.getString(2))
        }

        // 2. Verify transactions conversion
        db.query("SELECT id, amountMinor, currencyCode FROM transactions ORDER BY id ASC").use { c ->
            assertTrue(c.moveToNext())
            assertEquals(1, c.getInt(0))
            assertEquals(1999L, c.getLong(1))
            assertEquals("USD", c.getString(2))

            assertTrue(c.moveToNext())
            assertEquals(2, c.getInt(0))
            assertEquals(500L, c.getLong(1))
            assertEquals("JPY", c.getString(2))

            assertTrue(c.moveToNext())
            assertEquals(3, c.getInt(0))
            assertEquals(1234L, c.getLong(1))
            assertEquals("KWD", c.getString(2))
        }

        // 3. Verify account currency inference and balanceMinor
        db.query("SELECT id, balanceMinor, currencyCode FROM accounts ORDER BY id ASC").use { c ->
            assertTrue(c.moveToNext())
            assertEquals(1, c.getLong(0))
            assertEquals("USD", c.getString(2)) // Inferred from USD Tx
            assertEquals(1000000L, c.getLong(1)) // 10000.0 * 100

            assertTrue(c.moveToNext())
            assertEquals(2, c.getLong(0))
            assertEquals("JPY", c.getString(2)) // Inferred from JPY Tx
            assertEquals(500L, c.getLong(1)) // 500.0 * 1

            assertTrue(c.moveToNext())
            assertEquals(3, c.getLong(0))
            assertEquals("KWD", c.getString(2)) // Inferred from KWD Tx
            assertEquals(1234L, c.getLong(1)) // 1.234 * 1000
        }

        // 4. Verify foreign key check succeeds with 0 violations
        db.query("PRAGMA foreign_key_check").use { c ->
            assertFalse("Foreign key check should return no rows", c.moveToFirst())
        }

        db.close()
        helper.close()
        context.deleteDatabase("test_migration_9_10.db")
    }

    @Test
    fun migration_10_11_unifiesLedgerAndOpeningBalances() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test_migration_10_11.db")
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(10) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balanceMinor` INTEGER NOT NULL, `type` TEXT NOT NULL, `currencyCode` TEXT NOT NULL DEFAULT 'INR')")
                        db.execSQL("CREATE TABLE `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `type` TEXT NOT NULL DEFAULT 'EXPENSE', `currencyCode` TEXT NOT NULL DEFAULT 'INR')")
                        db.execSQL("CREATE TABLE `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `currencyCode` TEXT NOT NULL DEFAULT 'INR', FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")

                        // Account with balance 10,000 (1000000 paise)
                        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balanceMinor`, `type`, `currencyCode`) VALUES (1, 'Main Bank', 1000000, 'BANK', 'INR')")
                        // Expense 500 (50000 paise)
                        db.execSQL("INSERT INTO `expenses` (`id`, `amountMinor`, `category`, `date`, `currencyCode`) VALUES (1, 50000, 'Food', 1000, 'INR')")
                        // Income transaction 2,000 (200000 paise)
                        db.execSQL("INSERT INTO `transactions` (`id`, `title`, `amountMinor`, `type`, `category`, `timestamp`, `accountId`, `currencyCode`) VALUES (1, 'Salary', 200000, 'INCOME', 'Salary', 2000, 1, 'INR')")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase
        val migration = createMigration10To11("INR")
        migration.migrate(db)

        // 1. Verify expenses now contains the unified ledger entries (both original expense and copied transaction)
        db.query("SELECT COUNT(*) FROM expenses").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(2L, c.getLong(0))
        }

        // 2. Verify openingBalanceMinor was computed so that openingBalance + entries = original balance (1000000)
        // Original balance = 1,000,000. Entries: -50,000 (expense) + 200,000 (income) = +150,000.
        // Opening balance = 1,000,000 - (+150,000) = 850,000.
        db.query("SELECT openingBalanceMinor FROM accounts WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(850000L, c.getLong(0))
        }

        // 3. Verify transactions_legacy exists
        db.query("SELECT COUNT(*) FROM transactions_legacy").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1L, c.getLong(0))
        }

        // 4. Foreign key check
        db.query("PRAGMA foreign_key_check").use { c ->
            assertFalse("Foreign key check should have no violations", c.moveToFirst())
        }

        db.close()
        helper.close()
        context.deleteDatabase("test_migration_10_11.db")
    }

    @Test
    fun migration_11_12_addsRecurringRulesAndColumn() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test_migration_11_12.db")
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(11) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `openingBalanceMinor` INTEGER NOT NULL, `type` TEXT NOT NULL, `currencyCode` TEXT NOT NULL DEFAULT 'INR')")
                        db.execSQL("CREATE TABLE `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `type` TEXT NOT NULL DEFAULT 'EXPENSE', `currencyCode` TEXT NOT NULL DEFAULT 'INR', `accountId` INTEGER NOT NULL DEFAULT 1, `kind` TEXT NOT NULL DEFAULT 'EXPENSE', `goalId` INTEGER)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase
        MIGRATION_11_12.migrate(db)

        val c = db.query("PRAGMA table_info(`recurring_rules`)")
        assertTrue(c.count > 0)
        c.close()

        val cExp = db.query("PRAGMA table_info(`expenses`)")
        var hasRecurringRuleId = false
        cExp.use {
            val nameIdx = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (it.getString(nameIdx).equals("recurringRuleId", ignoreCase = true)) {
                    hasRecurringRuleId = true
                    break
                }
            }
        }
        assertTrue(hasRecurringRuleId)

        db.close()
        helper.close()
        context.deleteDatabase("test_migration_11_12.db")
    }

    @Test
    fun testOccurrencesBetweenPureFunction() {
        val startCal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.JANUARY, 1, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startTime = startCal.timeInMillis

        val rule = RecurringRule(
            id = 1L,
            title = "Rent",
            amountMinor = 1000000L,
            category = "Housing",
            frequency = "MONTHLY",
            startDate = startTime,
            nextDueDate = startTime,
            isActive = true
        )

        val endCal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.APRIL, 15, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val endTime = endCal.timeInMillis

        val occurrences = RecurringProcessor.occurrencesBetween(rule, startTime, endTime)
        assertEquals(4, occurrences.size)
    }

    @Test
    fun testPreMigrationBackupCopiesWalRowsWithoutClosing() {
        val testDbName = "test_wal_backup.db"
        context.deleteDatabase(testDbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(testDbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE items (id INTEGER PRIMARY KEY, name TEXT)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase
        db.query("PRAGMA journal_mode=WAL").use { it.moveToFirst() }

        // Write rows without closing the database connection
        db.execSQL("INSERT INTO items (id, name) VALUES (1, 'Uncommitted in WAL')")
        db.execSQL("INSERT INTO items (id, name) VALUES (2, 'Second WAL entry')")

        // Perform pre-migration backup while db connection is still open
        val backupDir = PreMigrationBackup.getBackupDir(context)
        backupDir.listFiles()?.forEach { it.delete() }

        val backupResult = PreMigrationBackup.backupDatabaseBeforeMigration(context, testDbName)
        assertTrue("Backup should succeed", backupResult)

        val backedUpDbFile = backupDir.listFiles { f -> f.name.endsWith(".db") }?.firstOrNull()
        assertNotNull(backedUpDbFile)

        // Open the backed-up copy and verify the unclosed rows are readable
        val copyDb = android.database.sqlite.SQLiteDatabase.openDatabase(
            backedUpDbFile!!.path,
            null,
            android.database.sqlite.SQLiteDatabase.OPEN_READONLY
        )
        copyDb.rawQuery("SELECT COUNT(*) FROM items", null).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(2, cursor.getInt(0))
        }
        copyDb.close()

        // Clean up
        db.close()
        helper.close()
        context.deleteDatabase(testDbName)
        PreMigrationBackup.pruneOldBackups(backupDir, maxToKeep = 0)
    }

    @Test
    fun testRoomMigrationFromVersions3To13() {
        for (version in 3..12) {
            val dbName = "test_migration_v${version}_to_13.db"
            context.deleteDatabase(dbName)

            val helper = FrameworkSQLiteOpenHelperFactory().create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(dbName)
                    .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(version) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            if (version >= 1) {
                                db.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `dueDate` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)")
                            }
                            if (version in 3..11) {
                                db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `type` TEXT NOT NULL)")
                                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT)")
                                db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `amountLimit` REAL NOT NULL, `monthYear` TEXT NOT NULL)")
                                db.execSQL("CREATE TABLE IF NOT EXISTS `savings_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `currentAmount` REAL NOT NULL DEFAULT 0.0, `targetDate` INTEGER NOT NULL DEFAULT 0, `frequency` TEXT NOT NULL DEFAULT 'WEEKLY', `contributionAmount` REAL NOT NULL DEFAULT 0.0, `isAutoGap` INTEGER NOT NULL DEFAULT 1, `iconTag` TEXT NOT NULL DEFAULT '🎮', `category` TEXT NOT NULL DEFAULT 'Saving', `imageUri` TEXT)")
                            }
                            if (version in 2..11) {
                                db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT)")
                            }
                            if (version in 5..11) {
                                try { db.execSQL("ALTER TABLE `expenses` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'EXPENSE'") } catch (e: Exception) {}
                            }
                            if (version in 6..11) {
                                try { db.execSQL("ALTER TABLE `expenses` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT 'INR'") } catch (e: Exception) {}
                            }
                            if (version in 7..11) {
                                try { db.execSQL("ALTER TABLE `transactions` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT 'INR'") } catch (e: Exception) {}
                            }
                            if (version >= 12) {
                                db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `currencyCode` TEXT NOT NULL, `openingBalanceMinor` INTEGER NOT NULL DEFAULT 0, `balanceMinor` INTEGER NOT NULL DEFAULT 0)")
                                db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `type` TEXT NOT NULL, `currencyCode` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `kind` TEXT NOT NULL, `goalId` INTEGER, `recurringRuleId` INTEGER)")
                                db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `amountLimitMinor` INTEGER NOT NULL, `monthYear` TEXT NOT NULL, `currencyCode` TEXT NOT NULL)")
                                db.execSQL("CREATE TABLE IF NOT EXISTS `savings_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmountMinor` INTEGER NOT NULL, `currentAmountMinor` INTEGER NOT NULL, `targetDate` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `contributionAmountMinor` INTEGER NOT NULL, `isAutoGap` INTEGER NOT NULL, `iconTag` TEXT NOT NULL, `category` TEXT NOT NULL, `imageUri` TEXT, `currencyCode` TEXT NOT NULL)")
                                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `category` TEXT NOT NULL, `frequency` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `note` TEXT)")
                            }

                            if (version < 12) {
                                try { db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`, `type`) VALUES (1, 'Test Acc', 1000.0, 'BANK')") } catch (e: Exception) {}
                                try { db.execSQL("INSERT INTO `budgets` (`id`, `category`, `amountLimit`, `monthYear`) VALUES (1, 'Food', 500.0, '10-2026')") } catch (e: Exception) {}
                                try { db.execSQL("INSERT INTO `transactions` (`id`, `title`, `amount`, `type`, `category`, `timestamp`, `accountId`) VALUES (1, 'Tx', 50.0, 'EXPENSE', 'Food', 1234, 1)") } catch (e: Exception) {}
                                try { db.execSQL("INSERT INTO `expenses` (`id`, `amount`, `category`, `date`) VALUES (1, 50.0, 'Food', 1234)") } catch (e: Exception) {}
                                try { db.execSQL("INSERT INTO `savings_goals` (`id`, `name`, `targetAmount`, `currentAmount`) VALUES (1, 'Goal', 1000.0, 100.0)") } catch (e: Exception) {}
                            } else {
                                try { db.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `currencyCode`, `openingBalanceMinor`, `balanceMinor`) VALUES (1, 'Test Acc', 'BANK', 'INR', 100000, 100000)") } catch (e: Exception) {}
                                try { db.execSQL("INSERT INTO `budgets` (`id`, `category`, `amountLimitMinor`, `monthYear`, `currencyCode`) VALUES (1, 'Food', 50000, '10-2026', 'INR')") } catch (e: Exception) {}
                                try { db.execSQL("INSERT INTO `expenses` (`id`, `amountMinor`, `category`, `date`, `type`, `currencyCode`, `accountId`, `kind`) VALUES (1, 5000, 'Food', 1234, 'EXPENSE', 'INR', 1, 'REGULAR')") } catch (e: Exception) {}
                                try { db.execSQL("INSERT INTO `savings_goals` (`id`, `name`, `targetAmountMinor`, `currentAmountMinor`, `targetDate`, `frequency`, `contributionAmountMinor`, `isAutoGap`, `iconTag`, `category`, `currencyCode`) VALUES (1, 'Goal', 100000, 10000, 12345, 'WEEKLY', 5000, 1, '🎮', 'Saving', 'INR')") } catch (e: Exception) {}
                            }
                        }
                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    })
                    .build()
            )
            helper.writableDatabase.close()
            helper.close()

            val roomDb = Room.databaseBuilder(context, FinanceDatabase::class.java, dbName)
                .addMigrations(*getFinanceDbMigrations("INR"))
                .allowMainThreadQueries()
                .build()

            val dao = roomDb.financeDao()
            val accounts = kotlinx.coroutines.runBlocking { dao.getAccountsSnapshot() }
            assertNotNull("Accounts must not be null after migrating from version $version to 13", accounts)
            assertTrue("Accounts must contain at least 1 account after migration", accounts.isNotEmpty())

            val budgetsTableInfo = roomDb.openHelper.writableDatabase.query("PRAGMA table_info(`budgets`)")
            var hasCurrencyCode = false
            budgetsTableInfo.use {
                val nameIdx = it.getColumnIndex("name")
                while (it.moveToNext()) {
                    if (it.getString(nameIdx) == "currencyCode") {
                        hasCurrencyCode = true
                        break
                    }
                }
            }
            assertTrue("Budgets table must have currencyCode after migration from version $version", hasCurrencyCode)

            roomDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun testMigration12To13FromExactV12Schema() {
        val dbName = "test_migration_exact_v12_to_13.db"
        context.deleteDatabase(dbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(12) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Create exact v12 tables according to schemas/12.json
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `accounts` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `name` TEXT NOT NULL,
                                `type` TEXT NOT NULL,
                                `currencyCode` TEXT NOT NULL,
                                `openingBalanceMinor` INTEGER NOT NULL DEFAULT 0,
                                `balanceMinor` INTEGER NOT NULL DEFAULT 0
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `expenses` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `amountMinor` INTEGER NOT NULL,
                                `category` TEXT NOT NULL,
                                `date` INTEGER NOT NULL,
                                `note` TEXT,
                                `imagePath` TEXT,
                                `type` TEXT NOT NULL,
                                `currencyCode` TEXT NOT NULL,
                                `accountId` INTEGER NOT NULL,
                                `kind` TEXT NOT NULL,
                                `goalId` INTEGER,
                                `recurringRuleId` INTEGER,
                                FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `budgets` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `category` TEXT NOT NULL,
                                `amountLimitMinor` INTEGER NOT NULL,
                                `monthYear` TEXT NOT NULL,
                                `currencyCode` TEXT NOT NULL
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `savings_goals` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `name` TEXT NOT NULL,
                                `targetAmountMinor` INTEGER NOT NULL,
                                `currentAmountMinor` INTEGER NOT NULL,
                                `targetDate` INTEGER NOT NULL,
                                `frequency` TEXT NOT NULL,
                                `contributionAmountMinor` INTEGER NOT NULL,
                                `isAutoGap` INTEGER NOT NULL,
                                `iconTag` TEXT NOT NULL,
                                `category` TEXT NOT NULL,
                                `imageUri` TEXT,
                                `currencyCode` TEXT NOT NULL
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `recurring_rules` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `title` TEXT NOT NULL,
                                `amountMinor` INTEGER NOT NULL,
                                `category` TEXT NOT NULL,
                                `frequency` TEXT NOT NULL,
                                `startDate` INTEGER NOT NULL,
                                `nextDueDate` INTEGER NOT NULL,
                                `isActive` INTEGER NOT NULL,
                                `currencyCode` TEXT NOT NULL,
                                `accountId` INTEGER NOT NULL,
                                `note` TEXT
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `reminders` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `text` TEXT NOT NULL,
                                `dueDate` INTEGER NOT NULL,
                                `isCompleted` INTEGER NOT NULL,
                                `isEnabled` INTEGER NOT NULL
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `transactions_legacy` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `title` TEXT NOT NULL,
                                `amount` REAL NOT NULL,
                                `type` TEXT NOT NULL,
                                `category` TEXT NOT NULL,
                                `timestamp` INTEGER NOT NULL,
                                `accountId` INTEGER NOT NULL,
                                `note` TEXT,
                                `imagePath` TEXT,
                                `currencyCode` TEXT NOT NULL DEFAULT 'INR'
                            )
                        """.trimIndent())

                        // Seed test data in v12 tables
                        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `currencyCode`, `openingBalanceMinor`, `balanceMinor`) VALUES (1, 'Main Bank', 'BANK', 'INR', 500000, 500000)")
                        db.execSQL("INSERT INTO `expenses` (`id`, `amountMinor`, `category`, `date`, `note`, `type`, `currencyCode`, `accountId`, `kind`) VALUES (1, 15000, 'Groceries', 123456789, 'Weekly groceries', 'EXPENSE', 'INR', 1, 'REGULAR')")
                        db.execSQL("INSERT INTO `budgets` (`id`, `category`, `amountLimitMinor`, `monthYear`, `currencyCode`) VALUES (1, 'Groceries', 100000, '10-2026', 'INR')")
                        db.execSQL("INSERT INTO `savings_goals` (`id`, `name`, `targetAmountMinor`, `currentAmountMinor`, `targetDate`, `frequency`, `contributionAmountMinor`, `isAutoGap`, `iconTag`, `category`, `currencyCode`) VALUES (1, 'Vacation', 2000000, 500000, 170000000, 'MONTHLY', 100000, 1, '✈️', 'Travel', 'INR')")
                        db.execSQL("INSERT INTO `recurring_rules` (`id`, `title`, `amountMinor`, `category`, `frequency`, `startDate`, `nextDueDate`, `isActive`, `currencyCode`, `accountId`, `note`) VALUES (1, 'Netflix', 64900, 'Entertainment', 'MONTHLY', 123456, 123456, 1, 'INR', 1, 'Sub')")
                        db.execSQL("INSERT INTO `reminders` (`id`, `text`, `dueDate`, `isCompleted`, `isEnabled`) VALUES (1, 'Pay Rent', 123456, 0, 1)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val writableDb = helper.writableDatabase

        // Execute migration 12 -> 13
        val migration12To13 = createMigration12To13("INR")
        migration12To13.migrate(writableDb)
        writableDb.close()
        helper.close()

        // Open with Room Database at version 13
        val roomDb = Room.databaseBuilder(context, FinanceDatabase::class.java, dbName)
            .addMigrations(*getFinanceDbMigrations("INR"))
            .allowMainThreadQueries()
            .build()

        val dao = roomDb.financeDao()

        // Verify Expenses: both amount and amountMinor must be present and accurate
        val expenses = kotlinx.coroutines.runBlocking { dao.getExpensesSnapshot() }
        assertEquals(1, expenses.size)
        assertEquals(15000L, expenses[0].amountMinor)
        assertEquals(150.0, expenses[0].amount, 0.001)
        assertEquals("Groceries", expenses[0].category)
        assertEquals(1L, expenses[0].accountId)

        // Verify Accounts: openingBalance, balance, openingBalanceMinor, balanceMinor
        val accounts = kotlinx.coroutines.runBlocking { dao.getAccountsSnapshot() }
        assertEquals(1, accounts.size)
        assertEquals(500000L, accounts[0].balanceMinor)
        assertEquals(5000.0, accounts[0].balance, 0.001)

        // Verify Budgets: amountLimit and amountLimitMinor
        val budgets = kotlinx.coroutines.runBlocking { dao.getBudgetsSnapshot() }
        assertEquals(1, budgets.size)
        assertEquals(100000L, budgets[0].amountLimitMinor)
        assertEquals(1000.0, budgets[0].amountLimit, 0.001)

        // Verify Savings Goals: targetAmount, currentAmount, etc.
        val goals = kotlinx.coroutines.runBlocking { dao.getSavingsGoalsSnapshot() }
        assertEquals(1, goals.size)
        assertEquals(2000000L, goals[0].targetAmountMinor)
        assertEquals(20000.0, goals[0].targetAmount, 0.001)
        assertEquals(500000L, goals[0].currentAmountMinor)
        assertEquals(5000.0, goals[0].currentAmount, 0.001)

        // Verify Recurring Rules
        val rules = kotlinx.coroutines.runBlocking { dao.getRecurringRulesSnapshot() }
        assertEquals(1, rules.size)
        assertEquals("Netflix", rules[0].title)
        assertEquals(64900L, rules[0].amountMinor)

        // Verify Reminders
        val reminders = kotlinx.coroutines.runBlocking { dao.getRemindersSnapshot() }
        assertEquals(1, reminders.size)
        assertEquals("Pay Rent", reminders[0].text)

        roomDb.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun normalizeLedgerSchema_withForeignKeysEnabled_preservesAllExpensesAndAccounts() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("test_fk_on_migration.db")
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(9) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("PRAGMA foreign_keys = ON")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `balance` REAL NOT NULL DEFAULT 0.0)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `imagePath` TEXT, `type` TEXT NOT NULL DEFAULT 'EXPENSE', `currencyCode` TEXT NOT NULL DEFAULT 'INR', `accountId` INTEGER NOT NULL, FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL DEFAULT 'INR', `note` TEXT, `imagePath` TEXT)")

                        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `balance`) VALUES (1, 'Main Bank', 'BANK', 5000.0)")
                        db.execSQL("INSERT INTO `expenses` (`id`, `amount`, `category`, `date`, `note`, `type`, `currencyCode`, `accountId`) VALUES (10, 250.0, 'Dining', 1700000000000, 'Dinner', 'EXPENSE', 'INR', 1)")
                        db.execSQL("INSERT INTO `expenses` (`id`, `amount`, `category`, `date`, `note`, `type`, `currencyCode`, `accountId`) VALUES (11, 100.0, 'Groceries', 1700000001000, 'Apples', 'EXPENSE', 'INR', 1)")
                        db.execSQL("INSERT INTO `transactions` (`id`, `title`, `amount`, `type`, `category`, `timestamp`, `accountId`, `currencyCode`) VALUES (20, 'Coffee', 50.0, 'EXPENSE', 'Food', 1700000002000, 1, 'INR')")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val db = helper.writableDatabase
        db.execSQL("PRAGMA foreign_keys = ON")

        // Run normalizeLedgerSchema with foreign keys explicitly ON
        normalizeLedgerSchema(db, "INR")

        // Verify expenses survived (2 initial expenses + 1 migrated transaction = 3 total)
        val cursor = db.query("SELECT COUNT(*) FROM expenses")
        var count = 0
        cursor.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        assertEquals("All expenses and transactions must survive migration with foreign_keys = ON", 3, count)

        // Verify accounts survived
        val accCursor = db.query("SELECT COUNT(*) FROM accounts")
        var accCount = 0
        accCursor.use {
            if (it.moveToFirst()) accCount = it.getInt(0)
        }
        assertEquals("Account must survive migration with foreign_keys = ON", 1, accCount)

        db.close()
        helper.close()
        context.deleteDatabase("test_fk_on_migration.db")
    }
}
