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
        var computedBal = repository.computeAccountBalanceMinor(account)
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
        computedBal = repository.computeAccountBalanceMinor(account)
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

        var computedBal = repository.computeAccountBalanceMinor(account)
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
        computedBal = repository.computeAccountBalanceMinor(account)
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

        var computedBal = repository.computeAccountBalanceMinor(account)
        assertEquals(170000L, computedBal)

        repository.deleteExpense(exp.copy(id = expId))

        computedBal = repository.computeAccountBalanceMinor(account)
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
            goals = emptyList()
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
        val computedBal = repository.computeAccountBalanceMinor(account)
        assertEquals(expectedBalance, computedBal)
    }
}
