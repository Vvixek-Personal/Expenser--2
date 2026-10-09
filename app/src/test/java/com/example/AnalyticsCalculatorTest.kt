package com.example

import com.example.data.Budget
import com.example.data.Expense
import com.example.ui.AnalyticsCalculator
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale
import kotlin.math.roundToInt

class AnalyticsCalculatorTest {

    @Test
    fun testEmptyExpensesSpendingPatternsDoesNotCrash() {
        val pattern = AnalyticsCalculator.computeSpendingPatterns(emptyList(), "INR")
        assertNotNull(pattern)
        assertEquals(0.0, pattern.peakDayTotal, 0.001)
        assertEquals(0.0, pattern.peakDayPercentage, 0.001)
        assertTrue(pattern.unusualSpikes.isEmpty())

        // Verify formatting string does not throw UnknownFormatConversionException
        val currencySymbol = "₹"
        val formattedPeakTotal = String.format(Locale.getDefault(), "%,.0f", pattern.peakDayTotal)
        val text = "${pattern.peakDayName} represents ${pattern.peakDayPercentage.roundToInt()}% of your active period expenses ($currencySymbol$formattedPeakTotal total)."
        assertNotNull(text)
        assertTrue(text.contains("0%"))
    }

    @Test
    fun testSpendingPatternsWithExpensesCalculatesPeakDay() {
        val expenses = listOf(
            Expense(id = 1L, amountMinor = 50000L, amount = 500.0, category = "Food", date = 1700000000000L, type = "EXPENSE", currencyCode = "INR"),
            Expense(id = 2L, amountMinor = 150000L, amount = 1500.0, category = "Travel", date = 1700000000000L, type = "EXPENSE", currencyCode = "INR")
        )
        val pattern = AnalyticsCalculator.computeSpendingPatterns(expenses, "INR")
        assertNotNull(pattern)
        assertTrue(pattern.peakDayTotal > 0.0)
        assertTrue(pattern.peakDayPercentage > 0.0)
    }

    @Test
    fun testCategoryTrendsEmptyAndNonEmpty() {
        val emptyTrends = AnalyticsCalculator.computeCategoryTrends(emptyList(), emptyList(), "INR")
        assertTrue(emptyTrends.isEmpty())

        val expenses = listOf(
            Expense(id = 1L, amountMinor = 10000L, amount = 100.0, category = "Food", date = 1700000000000L, type = "EXPENSE", currencyCode = "INR")
        )
        val trends = AnalyticsCalculator.computeCategoryTrends(expenses, emptyList(), "INR")
        assertEquals(1, trends.size)
        assertEquals("Food", trends[0].category)
        assertEquals(100.0, trends[0].currentAmount, 0.001)
    }

    @Test
    fun testBudgetPerformanceEmptyAndNonEmpty() {
        val emptyPerf = AnalyticsCalculator.computeBudgetPerformance(emptyList(), emptyList(), "INR")
        assertTrue(emptyPerf.isEmpty())

        val budgets = listOf(
            Budget(id = 1L, category = "Food", amountLimit = 500.0, amountLimitMinor = 50000L, monthYear = "10-2026", currencyCode = "INR")
        )
        val expenses = listOf(
            Expense(id = 1L, amountMinor = 20000L, amount = 200.0, category = "Food", date = 1700000000000L, type = "EXPENSE", currencyCode = "INR")
        )
        val perf = AnalyticsCalculator.computeBudgetPerformance(expenses, budgets, "INR")
        assertEquals(1, perf.size)
        assertEquals(200.0, perf[0].actualSpent, 0.001)
        assertEquals(500.0, perf[0].limit, 0.001)
        assertFalse(perf[0].isOverBudget)
    }

    @Test
    fun testSingleTransactionSpendingPatterns() {
        val singleExpense = listOf(
            Expense(
                id = 42L,
                amountMinor = 120000L,
                amount = 1200.0,
                category = "Groceries",
                date = 1700000000000L,
                type = "EXPENSE",
                currencyCode = "INR"
            )
        )
        val pattern = AnalyticsCalculator.computeSpendingPatterns(singleExpense, "INR")
        assertNotNull(pattern)
        assertTrue(pattern.peakDayTotal > 0.0)
        assertEquals(100.0, pattern.peakDayPercentage, 0.01)
        assertEquals(1200.0, pattern.peakDayTotal, 0.01)
        assertFalse(pattern.dayAverages.isEmpty())
    }

    @Test
    fun testNonInrStatsCurrencyNormalization() {
        val expenseUsd = Expense(
            id = 101L,
            amountMinor = 5000L, // $50.00
            amount = 50.0,
            category = "Software",
            date = 1700000000000L,
            type = "EXPENSE",
            currencyCode = "USD"
        )
        // Normalize against USD and EUR
        val normalizedUsd = AnalyticsCalculator.normalize(expenseUsd, "USD")
        assertEquals(50.0, normalizedUsd, 0.01)

        val patterns = AnalyticsCalculator.computeSpendingPatterns(listOf(expenseUsd), "USD")
        assertEquals(50.0, patterns.peakDayTotal, 0.01)
    }

    @Test
    fun testLargeDatasetPerformanceAndStability() {
        val largeList = (1..1000).map { i ->
            Expense(
                id = i.toLong(),
                amountMinor = (i * 100).toLong(),
                amount = i.toDouble(),
                category = if (i % 2 == 0) "Food" else "Shopping",
                date = 1700000000000L + (i * 3600000L),
                type = if (i % 5 == 0) "INCOME" else "EXPENSE",
                currencyCode = "INR"
            )
        }
        val pattern = AnalyticsCalculator.computeSpendingPatterns(largeList, "INR")
        assertNotNull(pattern)
        assertTrue(pattern.peakDayTotal > 0.0)
    }
}
