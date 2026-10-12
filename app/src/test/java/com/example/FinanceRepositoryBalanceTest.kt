package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinanceRepositoryBalanceTest {

    private lateinit var database: FinanceDatabase
    private lateinit var repository: FinanceRepository
    private lateinit var dao: FinanceDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.financeDao()
        repository = FinanceRepository(dao, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testExpenseEditAmountUpdatesComputedBalance() = runBlocking {
        // Starting balance: ₹10,000 (1000000 paise)
        val accountId = repository.insertAccount(
            Account(name = "Main Bank", openingBalanceMinor = 1000000L, type = "BANK", currencyCode = "INR")
        )
        val account = repository.getAccountById(accountId)!!

        // Create expense: ₹500 (50000 paise)
        val expId = repository.insertExpense(
            Expense(
                amountMinor = 50000L,
                category = "Food",
                date = System.currentTimeMillis(),
                note = "Grocery",
                type = "EXPENSE",
                currencyCode = "INR",
                accountId = accountId
            )
        )

        // Current computed balance should be 950000 paise (₹9,500)
        var computedBal = repository.computeAccountBalanceMinor(account.id)
        assertEquals(950000L, computedBal)

        // Edit expense: ₹500 -> ₹1,000 (100000 paise)
        repository.updateExpense(
            Expense(
                id = expId,
                amountMinor = 100000L,
                category = "Food",
                date = System.currentTimeMillis(),
                note = "Grocery",
                type = "EXPENSE",
                currencyCode = "INR",
                accountId = accountId
            )
        )

        // Expected computed balance: 1000000 - 100000 = 900000 paise (₹9,000)
        computedBal = repository.computeAccountBalanceMinor(account.id)
        assertEquals(900000L, computedBal)
    }

    @Test
    fun testExpenseEditTypeUpdatesComputedBalance() = runBlocking {
        // Starting balance: ₹10,000
        val accountId = repository.insertAccount(
            Account(name = "Main Bank", openingBalanceMinor = 1000000L, type = "BANK", currencyCode = "INR")
        )
        val account = repository.getAccountById(accountId)!!

        // Create expense: ₹500 (balance becomes 9,500)
        val expId = repository.insertExpense(
            Expense(
                amountMinor = 50000L,
                category = "General",
                date = System.currentTimeMillis(),
                note = "Payment",
                type = "EXPENSE",
                currencyCode = "INR",
                accountId = accountId
            )
        )

        var computedBal = repository.computeAccountBalanceMinor(account.id)
        assertEquals(950000L, computedBal)

        // Change from EXPENSE ₹500 -> INCOME ₹500
        repository.updateExpense(
            Expense(
                id = expId,
                amountMinor = 50000L,
                category = "General",
                date = System.currentTimeMillis(),
                note = "Payment",
                type = "INCOME",
                currencyCode = "INR",
                accountId = accountId,
                kind = "INCOME"
            )
        )

        // Expected computed balance: 1000000 + 50000 = 1050000 paise (₹10,500)
        computedBal = repository.computeAccountBalanceMinor(account.id)
        assertEquals(1050000L, computedBal)
    }

    @Test
    fun testExpenseDeleteReversesComputedBalance() = runBlocking {
        val accountId = repository.insertAccount(
            Account(name = "Wallet", openingBalanceMinor = 200000L, type = "CASH", currencyCode = "INR")
        )
        val account = repository.getAccountById(accountId)!!

        val exp = Expense(
            amountMinor = 30000L,
            category = "Food",
            date = System.currentTimeMillis(),
            note = "Lunch",
            type = "EXPENSE",
            currencyCode = "INR",
            accountId = accountId
        )
        val expId = repository.insertExpense(exp)

        var computedBal = repository.computeAccountBalanceMinor(account.id)
        assertEquals(170000L, computedBal)

        repository.deleteExpense(exp.copy(id = expId))

        computedBal = repository.computeAccountBalanceMinor(account.id)
        assertEquals(200000L, computedBal)
    }

    @Test
    fun testAtomicRestoreAllData() = runBlocking {
        val accountA = Account(id = 1L, name = "Restored Bank", openingBalanceMinor = 1500000L, type = "BANK", currencyCode = "INR")
        val exp1 = Expense(
            id = 1L,
            amountMinor = 500000L,
            category = "Salary",
            date = System.currentTimeMillis(),
            note = "Deposit",
            type = "INCOME",
            currencyCode = "INR",
            accountId = 1L,
            kind = "INCOME"
        )

        repository.restoreAllData(
            expenses = listOf(exp1),
            accounts = listOf(accountA),
            budgets = emptyList(),
            goals = emptyList(),
            reminders = emptyList()
        )

        val restoredAccounts = repository.allAccounts.first()
        val restoredExpenses = repository.allExpenses.first()

        assertEquals(1, restoredAccounts.size)
        assertEquals(1, restoredExpenses.size)
        assertEquals(15000.0, restoredAccounts[0].openingBalance, 0.001)
    }

    @Test
    fun testExpenseCurrencyCodePersistenceAndAnalyticsNormalization() = runBlocking {
        // User is using USD
        val usdExpense = Expense(
            amountMinor = 5000L,
            category = "Dining",
            date = System.currentTimeMillis(),
            note = "Dinner in NYC",
            type = "EXPENSE",
            currencyCode = "USD"
        )
        val expId = repository.insertExpense(usdExpense)

        val retrieved = dao.getExpenseById(expId)
        assertNotNull(retrieved)
        assertEquals("USD", retrieved!!.currencyCode)
        assertEquals(50.0, retrieved.amount, 0.001)

        // Normalizing 50 USD with stats currency = USD must yield exactly 50.0
        val normalizedUsd = com.example.ui.AnalyticsCalculator.normalize(retrieved, "USD")
        assertEquals(50.0, normalizedUsd, 0.001)

        // Normalizing 50 USD with stats currency = INR converts using rate (> 50.0)
        val normalizedInr = com.example.ui.AnalyticsCalculator.normalize(retrieved, "INR")
        assertTrue("Normalized INR should be > 50 USD", normalizedInr > 1000.0)
    }

    @Test
    fun testCrossCurrencyExpenseUpdatesAccountBalanceCorrectly() = runBlocking {
        // Account in INR with ₹10,000 opening balance (1000000 paise)
        val inrAccountId = repository.insertAccount(
            Account(name = "India Bank", openingBalanceMinor = 1000000L, type = "BANK", currencyCode = "INR")
        )
        val account = repository.getAccountById(inrAccountId)!!

        // Expense of $10.00 USD (1000 cents)
        // Rate: 1 USD = 83.5 INR -> $10.00 = ₹835.00 (83500 paise)
        val expId = repository.insertExpense(
            Expense(
                amountMinor = 1000L,
                category = "Software",
                date = System.currentTimeMillis(),
                note = "US Subscription",
                type = "EXPENSE",
                currencyCode = "USD",
                accountId = inrAccountId
            )
        )

        // Balance must be ₹10,000 - ₹835 = ₹9,165 (916500 paise), NOT ₹10,000 - $10 = ₹9,990!
        val expectedBalance = 1000000L - 83500L
        val computedBal = repository.computeAccountBalanceMinor(account.id)
        assertEquals(expectedBalance, computedBal)
    }

    @Test
    fun testRestoreAllDataPreservesRecurringRulesAndIsAtomic(): Unit = runBlocking {
        // Setup initial data
        val accId = repository.insertAccount(
            Account(id = 1L, name = "Primary", openingBalanceMinor = 50000L, type = "BANK", currencyCode = "INR")
        )
        val initialRule = RecurringRule(
            id = 10L,
            title = "Gym",
            amountMinor = 200000L,
            category = "Health",
            frequency = "MONTHLY",
            startDate = 1000L,
            nextDueDate = 2000L,
            currencyCode = "INR",
            accountId = accId
        )
        repository.insertRecurringRule(initialRule)

        val exp = Expense(
            id = 100L,
            amountMinor = 5000L,
            amount = 50.0,
            category = "Health",
            date = 1500L,
            type = "EXPENSE",
            currencyCode = "INR",
            accountId = accId
        )
        repository.insertExpense(exp)

        // Verify initial state
        assertEquals(1, dao.getRecurringRulesSnapshot().size)
        assertEquals(1, dao.getExpensesSnapshot().size)

        // Restore new dataset with different rule and expense
        val newAccount = Account(id = 2L, name = "Secondary", openingBalanceMinor = 100000L, type = "CASH", currencyCode = "INR")
        val newRule = RecurringRule(
            id = 20L,
            title = "Streaming",
            amountMinor = 49900L,
            category = "Entertainment",
            frequency = "MONTHLY",
            startDate = 5000L,
            nextDueDate = 6000L,
            currencyCode = "INR",
            accountId = 2L
        )
        val newExpense = Expense(
            id = 200L,
            amountMinor = 12000L,
            amount = 120.0,
            category = "Food",
            date = 7000L,
            type = "EXPENSE",
            currencyCode = "INR",
            accountId = 2L
        )

        repository.restoreAllData(
            expenses = listOf(newExpense),
            accounts = listOf(newAccount),
            budgets = emptyList(),
            goals = emptyList(),
            reminders = emptyList(),
            rules = listOf(newRule)
        )

        val restoredRules = dao.getRecurringRulesSnapshot()
        assertEquals(1, restoredRules.size)
        assertEquals("Streaming", restoredRules[0].title)
        assertEquals(49900L, restoredRules[0].amountMinor)

        val restoredExpenses = dao.getExpensesSnapshot()
        assertEquals(1, restoredExpenses.size)
        assertEquals(12000L, restoredExpenses[0].amountMinor)
    }

    @Test
    fun testLocalRecoveryManagerCreateAndRestoreSnapshot(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val accId = repository.insertAccount(
            Account(id = 1L, name = "Savings Bank", openingBalanceMinor = 500000L, type = "BANK", currencyCode = "INR")
        )
        val rule = RecurringRule(
            id = 1L,
            title = "Internet Bill",
            amountMinor = 99900L,
            category = "Utilities",
            frequency = "MONTHLY",
            startDate = 1000L,
            nextDueDate = 2000L,
            currencyCode = "INR",
            accountId = accId
        )
        repository.insertRecurringRule(rule)
        val exp = Expense(
            id = 1L,
            amountMinor = 15000L,
            amount = 150.0,
            category = "Utilities",
            date = 1500L,
            type = "EXPENSE",
            currencyCode = "INR",
            accountId = accId
        )
        repository.insertExpense(exp)

        // Create snapshot
        val successCreate = LocalRecoveryManager.createSnapshot(context, database)
        assertTrue("Snapshot creation must succeed", successCreate)

        val snapshots = LocalRecoveryManager.listSnapshots(context)
        assertTrue("Snapshots list must not be empty", snapshots.isNotEmpty())
        val latest = snapshots[0]
        assertTrue("Snapshot must be marked valid", latest.isValid)
        assertEquals(1, latest.accountsCount)
        assertEquals(1, latest.expensesCount)

        // Clear data to simulate loss
        dao.clearAllData()
        assertEquals(0, dao.getAccountsSnapshot().size)
        assertEquals(0, dao.getExpensesSnapshot().size)

        // Restore snapshot
        var restoredCur = ""
        val successRestore = LocalRecoveryManager.restoreSnapshot(context, database, latest.fileName) { cur, _, _, _ ->
            restoredCur = cur
        }
        assertTrue("Snapshot restore must succeed", successRestore)
        assertEquals(1, dao.getAccountsSnapshot().size)
        assertEquals(1, dao.getExpensesSnapshot().size)
        assertEquals(1, dao.getRecurringRulesSnapshot().size)
        assertEquals("Internet Bill", dao.getRecurringRulesSnapshot()[0].title)

        // Cleanup
        LocalRecoveryManager.deleteSnapshot(context, latest.fileName)
        Unit
    }
}
