package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

// ==========================================
// 📊 MODELS & DATA STRUCTURES
// ==========================================

enum class AnalyticsViewMode {
    QUICK_STATS,      // Fast at-a-glance scorecard & health gauge
    DEEP_ANALYTICS,   // Category trends, budget variance, account breakdown, day-of-week patterns & anomalies
    TRENDS_CASHFLOW   // Cash-flow waterfall & time-series curves
}

data class PeriodComparisonSummary(
    val currentSpent: Double,
    val previousSpent: Double,
    val spentChangePercent: Double,
    val currentIncome: Double,
    val previousIncome: Double,
    val incomeChangePercent: Double,
    val currentNetFlow: Double,
    val previousNetFlow: Double,
    val netFlowChangePercent: Double,
    val currentSavingsRate: Double,
    val previousSavingsRate: Double,
    val savingsRateDiffPoints: Double,
    val previousPeriodLabel: String
)

data class CategoryTrendSummary(
    val category: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val changePercent: Double,
    val percentageOfTotal: Double,
    val isIncreasing: Boolean,
    val count: Int,
    val transactions: List<Expense>
)

data class AccountAnalyticsSummary(
    val account: Account,
    val balance: Double,
    val totalInflow: Double,
    val totalOutflow: Double,
    val netFlow: Double,
    val percentageOfOutflow: Double,
    val transactions: List<Expense>
)

data class BudgetPerformanceSummary(
    val category: String,
    val limit: Double,
    val actualSpent: Double,
    val variance: Double,
    val percentUsed: Double,
    val isOverBudget: Boolean
)

data class SpendingPatternSummary(
    val peakDayName: String,
    val peakDayTotal: Double,
    val peakDayPercentage: Double,
    val dayAverages: Map<String, Double>,
    val unusualSpikes: List<Expense>,
    val recurringCandidates: List<Expense>
)

data class MetricInspectionData(
    val title: String,
    val primaryValueStr: String,
    val subtitle: String,
    val changePercent: Double?,
    val isPositiveGood: Boolean,
    val metricType: String,
    val currentPeriodExpenses: List<Expense>,
    val previousPeriodExpenses: List<Expense>,
    val dateRangeLabel: String,
    val currencySymbol: String
)

// ==========================================
// 🧮 ANALYTICS CALCULATIONS ENGINE
// ==========================================

object AnalyticsCalculator {

    /**
     * Converts an expense amount to the statistics currency for multi-currency integrity
     */
    fun normalize(expense: Expense, statsCurrencyCode: String): Double {
        val minor = Money.convert(expense.amountMinor, expense.currencyCode, statsCurrencyCode)
        return Money.toDouble(minor, statsCurrencyCode)
    }

    /**
     * Computes the immediately preceding comparative period of identical length
     */
    fun computePreviousDateRange(startMs: Long, endMs: Long): Pair<Long, Long> {
        val durationMs = (endMs - startMs).coerceAtLeast(86400000L)
        val prevEnd = startMs - 1L
        val prevStart = prevEnd - durationMs + 1L
        return Pair(prevStart, prevEnd)
    }

    /**
     * Full Period Comparison (Current vs Previous)
     */
    fun computeComparison(
        currentExpenses: List<Expense>,
        previousExpenses: List<Expense>,
        statsCurrencyCode: String,
        prevLabel: String
    ): PeriodComparisonSummary {
        val curSpent = currentExpenses
            .filter { it.type != "INCOME" && it.category != "Locked Savings" }
            .sumOf { normalize(it, statsCurrencyCode) }

        val prevSpent = previousExpenses
            .filter { it.type != "INCOME" && it.category != "Locked Savings" }
            .sumOf { normalize(it, statsCurrencyCode) }

        val curIncome = currentExpenses
            .filter { it.type == "INCOME" && it.category != "Goal Withdrawal" }
            .sumOf { normalize(it, statsCurrencyCode) }

        val prevIncome = previousExpenses
            .filter { it.type == "INCOME" && it.category != "Goal Withdrawal" }
            .sumOf { normalize(it, statsCurrencyCode) }

        val curNet = curIncome - curSpent
        val prevNet = prevIncome - prevSpent

        val curSavingsRate = if (curIncome > 0) ((curNet / curIncome) * 100.0).coerceIn(-100.0, 100.0) else 0.0
        val prevSavingsRate = if (prevIncome > 0) ((prevNet / prevIncome) * 100.0).coerceIn(-100.0, 100.0) else 0.0

        val spentPct = if (prevSpent > 0) ((curSpent - prevSpent) / prevSpent) * 100.0 else if (curSpent > 0) 100.0 else 0.0
        val incomePct = if (prevIncome > 0) ((curIncome - prevIncome) / prevIncome) * 100.0 else if (curIncome > 0) 100.0 else 0.0
        val netPct = if (abs(prevNet) > 0) ((curNet - prevNet) / abs(prevNet)) * 100.0 else 0.0

        return PeriodComparisonSummary(
            currentSpent = curSpent,
            previousSpent = prevSpent,
            spentChangePercent = spentPct,
            currentIncome = curIncome,
            previousIncome = prevIncome,
            incomeChangePercent = incomePct,
            currentNetFlow = curNet,
            previousNetFlow = prevNet,
            netFlowChangePercent = netPct,
            currentSavingsRate = curSavingsRate,
            previousSavingsRate = prevSavingsRate,
            savingsRateDiffPoints = curSavingsRate - prevSavingsRate,
            previousPeriodLabel = prevLabel
        )
    }

    /**
     * Category Trends Comparison (Increasing vs Decreasing categories)
     */
    fun computeCategoryTrends(
        currentExpenses: List<Expense>,
        previousExpenses: List<Expense>,
        statsCurrencyCode: String
    ): List<CategoryTrendSummary> {
        val curNonIncome = currentExpenses.filter { it.type != "INCOME" && it.category != "Locked Savings" }
        val prevNonIncome = previousExpenses.filter { it.type != "INCOME" && it.category != "Locked Savings" }

        val totalCurSpent = curNonIncome.sumOf { normalize(it, statsCurrencyCode) }.coerceAtLeast(1.0)

        val curByCat = curNonIncome.groupBy { it.category }
        val prevByCat = prevNonIncome.groupBy { it.category }

        val allCategories = (curByCat.keys + prevByCat.keys).distinct()

        return allCategories.map { cat ->
            val curTxns = curByCat[cat] ?: emptyList()
            val prevTxns = prevByCat[cat] ?: emptyList()

            val curAmount = curTxns.sumOf { normalize(it, statsCurrencyCode) }
            val prevAmount = prevTxns.sumOf { normalize(it, statsCurrencyCode) }

            val changePct = if (prevAmount > 0) {
                ((curAmount - prevAmount) / prevAmount) * 100.0
            } else if (curAmount > 0) {
                100.0
            } else {
                0.0
            }

            CategoryTrendSummary(
                category = cat,
                currentAmount = curAmount,
                previousAmount = prevAmount,
                changePercent = changePct,
                percentageOfTotal = (curAmount / totalCurSpent) * 100.0,
                isIncreasing = curAmount > prevAmount,
                count = curTxns.size,
                transactions = curTxns.sortedByDescending { it.date }
            )
        }.sortedByDescending { it.currentAmount }
    }

    /**
     * Day of week and anomaly detection pattern analysis
     */
    fun computeSpendingPatterns(
        expenses: List<Expense>,
        statsCurrencyCode: String
    ): SpendingPatternSummary {
        val nonIncome = expenses.filter { it.type != "INCOME" && it.category != "Locked Savings" }
        val totalSpent = nonIncome.sumOf { normalize(it, statsCurrencyCode) }.coerceAtLeast(1.0)

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val daySums = mutableMapOf<String, Double>()
        dayNames.forEach { daySums[it] = 0.0 }

        val cal = Calendar.getInstance()
        nonIncome.forEach { exp ->
            cal.timeInMillis = exp.date
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val name = when (dayOfWeek) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> "Mon"
            }
            daySums[name] = (daySums[name] ?: 0.0) + normalize(exp, statsCurrencyCode)
        }

        val peakDay = daySums.maxByOrNull { it.value } ?: java.util.AbstractMap.SimpleEntry("Fri", 0.0)
        val peakDayName = peakDay.key
        val peakDayTotal = peakDay.value
        val peakDayPct = (peakDayTotal / totalSpent) * 100.0

        // Daily average for anomaly detection
        val uniqueDaysCount = nonIncome.map {
            val c = Calendar.getInstance().apply { timeInMillis = it.date }
            "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
        }.distinct().size.coerceAtLeast(1)

        val dailyAvg = totalSpent / uniqueDaysCount
        // Anomaly threshold: 2.5x daily average or top single transactions
        val spikes = nonIncome.filter { normalize(it, statsCurrencyCode) >= dailyAvg * 2.0 && normalize(it, statsCurrencyCode) > 500.0 }
            .sortedByDescending { normalize(it, statsCurrencyCode) }
            .take(5)

        // Recurring candidate detection: matching notes/amounts
        val recurring = nonIncome.groupBy { (it.note ?: it.category).lowercase().trim() }
            .filter { it.value.size >= 2 }
            .flatMap { it.value }
            .distinctBy { it.id }
            .take(4)

        return SpendingPatternSummary(
            peakDayName = peakDayName,
            peakDayTotal = peakDayTotal,
            peakDayPercentage = peakDayPct,
            dayAverages = daySums,
            unusualSpikes = spikes,
            recurringCandidates = recurring
        )
    }

    /**
     * Budget planned vs actual variance
     */
    fun computeBudgetPerformance(
        expenses: List<Expense>,
        budgets: List<Budget>,
        statsCurrencyCode: String
    ): List<BudgetPerformanceSummary> {
        val nonIncome = expenses.filter { it.type != "INCOME" && it.category != "Locked Savings" }
        val curByCat = nonIncome.groupBy { it.category }

        return budgets.map { b ->
            val spent = (curByCat[b.category] ?: emptyList()).sumOf { normalize(it, statsCurrencyCode) }
            val variance = b.amountLimit - spent
            val pct = if (b.amountLimit > 0) (spent / b.amountLimit) * 100.0 else 0.0
            BudgetPerformanceSummary(
                category = b.category,
                limit = b.amountLimit,
                actualSpent = spent,
                variance = variance,
                percentUsed = pct,
                isOverBudget = spent > b.amountLimit
            )
        }.sortedByDescending { it.percentUsed }
    }
}

// ==========================================
// 📱 TOP INTERACTIVE ANALYTICS HEADER
// ==========================================

@Composable
fun InteractiveAnalyticsHeader(
    currentMode: AnalyticsViewMode,
    onModeChange: (AnalyticsViewMode) -> Unit,
    selectedTimeFilter: String,
    onTimeFilterChange: (String) -> Unit,
    timeFilterPresets: List<String>,
    onCustomDateRangeClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Mode Switcher (Pill style: Quick Stats | Deep Analytics | Trends & Cashflow)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SleekSurface)
                .border(1.dp, SleekBorder, RoundedCornerShape(18.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf(
                Triple(AnalyticsViewMode.QUICK_STATS, "Quick Stats", Icons.Default.FlashOn),
                Triple(AnalyticsViewMode.DEEP_ANALYTICS, "Deep Insights", Icons.Default.Analytics),
                Triple(AnalyticsViewMode.TRENDS_CASHFLOW, "Trends & Flow", Icons.Default.Timeline)
            )

            modes.forEach { (mode, label, icon) ->
                val isSelected = currentMode == mode
                val bg by animateColorAsState(
                    targetValue = if (isSelected) SleekPrimary else Color.Transparent,
                    animationSpec = tween(220),
                    label = "modeBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModeChange(mode)
                        }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else SleekTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = label,
                            fontSize = SleekSizes.textCaption,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SleekTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time Range Filter Bar with Custom Range chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SleekSurface)
                .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            timeFilterPresets.forEach { tf ->
                val isSelected = selectedTimeFilter == tf
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) SleekPrimaryContainer else Color.Transparent,
                    animationSpec = tween(200),
                    label = "filterBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(pillBg)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTimeFilterChange(tf)
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf,
                        fontSize = SleekSizes.textCaption,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SleekPrimary else SleekTextSecondary
                    )
                }
            }

            // Custom Range button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                .background(if (selectedTimeFilter == "Custom") SleekPrimaryContainer else Color.Transparent)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCustomDateRangeClick()
                }
                .padding(horizontal = 8.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Custom Range",
                        tint = if (selectedTimeFilter == "Custom") SleekPrimary else SleekTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Custom",
                        fontSize = SleekSizes.textCaption,
                        fontWeight = if (selectedTimeFilter == "Custom") FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTimeFilter == "Custom") SleekPrimary else SleekTextSecondary
                    )
                }
            }
        }
    }
}

// ==========================================
// 🔍 INTERACTIVE SCORECARD WITH ENLARGE-ON-TAP
// ==========================================

@Composable
fun InteractiveScorecardRow(
    comparison: PeriodComparisonSummary,
    currencySymbol: String,
    onMetricClick: (MetricInspectionData) -> Unit,
    currentExpenses: List<Expense>,
    previousExpenses: List<Expense>,
    dateRangeLabel: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Income Metric Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .weight(1f)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMetricClick(
                        MetricInspectionData(
                            title = "Total Income",
                            primaryValueStr = "$currencySymbol%,.2f".format(comparison.currentIncome),
                            subtitle = "Income over selected period",
                            changePercent = comparison.incomeChangePercent,
                            isPositiveGood = true,
                            metricType = "INCOME",
                            currentPeriodExpenses = currentExpenses.filter { it.type == "INCOME" },
                            previousPeriodExpenses = previousExpenses.filter { it.type == "INCOME" },
                            dateRangeLabel = dateRangeLabel,
                            currencySymbol = currencySymbol
                        )
                    )
                }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Income", fontSize = 11.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.OpenInFull, contentDescription = "Enlarge", tint = SleekTextSecondary, modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currencySymbol%,.0f".format(comparison.currentIncome),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Comparison Pill vs Previous Period
                PeriodChangeBadge(
                    changePercent = comparison.incomeChangePercent,
                    isPositiveGood = true,
                    prevLabel = comparison.previousPeriodLabel
                )
            }
        }

        // Expense Metric Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .weight(1f)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMetricClick(
                        MetricInspectionData(
                            title = "Total Expenses",
                            primaryValueStr = "$currencySymbol%,.2f".format(comparison.currentSpent),
                            subtitle = "Expenses over selected period",
                            changePercent = comparison.spentChangePercent,
                            isPositiveGood = false,
                            metricType = "EXPENSE",
                            currentPeriodExpenses = currentExpenses.filter { it.type != "INCOME" && it.category != "Locked Savings" },
                            previousPeriodExpenses = previousExpenses.filter { it.type != "INCOME" && it.category != "Locked Savings" },
                            dateRangeLabel = dateRangeLabel,
                            currencySymbol = currencySymbol
                        )
                    )
                }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Expenses", fontSize = 11.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.OpenInFull, contentDescription = "Enlarge", tint = SleekTextSecondary, modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currencySymbol%,.0f".format(comparison.currentSpent),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.height(4.dp))
                PeriodChangeBadge(
                    changePercent = comparison.spentChangePercent,
                    isPositiveGood = false,
                    prevLabel = comparison.previousPeriodLabel
                )
            }
        }

        // Net Flow Metric Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .weight(1f)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMetricClick(
                        MetricInspectionData(
                            title = "Net Cash Flow",
                            primaryValueStr = if (comparison.currentNetFlow >= 0) "+$currencySymbol%,.2f".format(comparison.currentNetFlow) else "-$currencySymbol%,.2f".format(abs(comparison.currentNetFlow)),
                            subtitle = "Income minus expenses net balance",
                            changePercent = comparison.netFlowChangePercent,
                            isPositiveGood = true,
                            metricType = "NET_FLOW",
                            currentPeriodExpenses = currentExpenses,
                            previousPeriodExpenses = previousExpenses,
                            dateRangeLabel = dateRangeLabel,
                            currencySymbol = currencySymbol
                        )
                    )
                }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Net Flow", fontSize = 11.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.OpenInFull, contentDescription = "Enlarge", tint = SleekTextSecondary, modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                val isPositive = comparison.currentNetFlow >= 0
                Text(
                    text = if (isPositive) "+$currencySymbol%,.0f".format(comparison.currentNetFlow) else "-$currencySymbol%,.0f".format(abs(comparison.currentNetFlow)),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.height(4.dp))
                PeriodChangeBadge(
                    changePercent = comparison.netFlowChangePercent,
                    isPositiveGood = true,
                    prevLabel = comparison.previousPeriodLabel
                )
            }
        }
    }
}

@Composable
fun PeriodChangeBadge(
    changePercent: Double,
    isPositiveGood: Boolean,
    prevLabel: String
) {
    val isZero = abs(changePercent) < 0.1
    val isUp = changePercent > 0

    val isBeneficial = if (isPositiveGood) isUp else !isUp
    val tintColor = if (isZero) SleekTextSecondary else if (isBeneficial) Color(0xFF10B981) else Color(0xFFEF4444)
    val bgColor = tintColor.copy(alpha = 0.12f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        if (!isZero) {
            Icon(
                imageVector = if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(10.dp)
            )
        }
        Text(
            text = if (isZero) "0.0%" else "${if (isUp) "+" else ""}${changePercent.roundToInt()}%",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = tintColor
        )
    }
}

// ==========================================
// 🏷️ CATEGORY TRENDS WITH DRILL-DOWN
// ==========================================

@Composable
fun CategoryTrendsSection(
    categoryTrends: List<CategoryTrendSummary>,
    currencySymbol: String,
    categoryColors: Map<String, Color>,
    onCategoryClick: (CategoryTrendSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Category Trends & Velocity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Tap any category to inspect daily trend & transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = SleekSizes.textCaption
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SleekPrimaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${categoryTrends.size} categories",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (categoryTrends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No category transactions recorded in this period", fontSize = 13.sp, color = SleekTextSecondary)
                }
            } else {
                categoryTrends.take(8).forEach { item ->
                    val color = categoryColors[item.category] ?: SleekPrimary
                    val isUp = item.changePercent > 0
                    val isZero = abs(item.changePercent) < 0.1

                    Surface(
                        onClick = { onCategoryClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color.copy(alpha = 0.15f))
                                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getCategoryEmoji(item.category),
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.category,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = "$currencySymbol%,.0f".format(item.currentAmount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SleekTextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(5.dp))

                                // Progress bar showing % of total
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SleekBorder.copy(alpha = 0.5f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth((item.percentageOfTotal / 100.0).toFloat().coerceIn(0.02f, 1f))
                                                .background(color)
                                        )
                                    }

                                    // Trend % vs previous period
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        if (!isZero) {
                                            Icon(
                                                imageVector = if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isUp) Color(0xFFEF4444) else Color(0xFF10B981), // higher expense is red
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isZero) "0%" else "${if (isUp) "+" else ""}${item.changePercent.roundToInt()}%",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isZero) SleekTextSecondary else if (isUp) Color(0xFFEF4444) else Color(0xFF10B981)
                                        )
                                    }

                                    Text(
                                        text = "${item.percentageOfTotal.roundToInt()}%",
                                        fontSize = 10.5.sp,
                                        color = SleekTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Drill Down",
                                tint = SleekTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 💰 CASH-FLOW WATERFALL ANALYSIS
// ==========================================

@Composable
fun CashFlowWaterfallCard(
    comparison: PeriodComparisonSummary,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash Flow Waterfall",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Income → Expenses → Net Savings flow",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Inflow
            WaterfallStepRow(
                title = "1. Total Inflow (Income)",
                amount = comparison.currentIncome,
                currencySymbol = currencySymbol,
                color = Color(0xFF10B981),
                isPositive = true,
                maxScale = maxOf(comparison.currentIncome, comparison.currentSpent, 1.0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 2: Outflow (Expenses)
            WaterfallStepRow(
                title = "2. Total Outflow (Expenses)",
                amount = comparison.currentSpent,
                currencySymbol = currencySymbol,
                color = Color(0xFFEF4444),
                isPositive = false,
                maxScale = maxOf(comparison.currentIncome, comparison.currentSpent, 1.0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 3: Ending Net Flow
            val isNetPositive = comparison.currentNetFlow >= 0
            val netColor = if (isNetPositive) Color(0xFF10B981) else Color(0xFFEF4444)
            WaterfallStepRow(
                title = "3. Net Retained Balance",
                amount = abs(comparison.currentNetFlow),
                currencySymbol = currencySymbol,
                color = netColor,
                isPositive = isNetPositive,
                maxScale = maxOf(comparison.currentIncome, comparison.currentSpent, 1.0)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Savings Rate Performance Meter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SleekPrimaryContainer.copy(alpha = 0.35f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Savings Rate", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SleekTextPrimary)
                            Text(
                                text = if (comparison.currentSavingsRate >= 20.0) "🌟 Healthy savings rate (>20%)" else "⚠️ Target 20%+ savings",
                                fontSize = 10.5.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }
                    Text(
                        text = "${comparison.currentSavingsRate.roundToInt()}%",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (comparison.currentSavingsRate >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterfallStepRow(
    title: String,
    amount: Double,
    currencySymbol: String,
    color: Color,
    isPositive: Boolean,
    maxScale: Double
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 12.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
            Text(
                text = "${if (isPositive) "+" else "-"}$currencySymbol%,.0f".format(amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val ratio = (amount / maxScale).toFloat().coerceIn(0.04f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SleekBorder.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

// ==========================================
// 🎯 BUDGET PERFORMANCE & VARIANCE
// ==========================================

@Composable
fun BudgetPerformanceSection(
    budgetPerformance: List<BudgetPerformanceSummary>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Budget Performance & Variance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Planned limit vs actual expense tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = SleekSizes.textCaption
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (budgetPerformance.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No budgets configured yet. Add budgets in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary
                    )
                }
            } else {
                budgetPerformance.forEach { item ->
                    val isExceeded = item.isOverBudget
                    val progressColor = if (isExceeded) Color(0xFFEF4444) else if (item.percentUsed > 80.0) Color(0xFFF59E0B) else Color(0xFF10B981)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.category,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = SleekTextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$currencySymbol%,.0f / $currencySymbol%,.0f".format(item.actualSpent, item.limit),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SleekTextSecondary
                                )
                                Text(
                                    text = "${item.percentUsed.roundToInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SleekBorder.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((item.percentUsed / 100.0).toFloat().coerceIn(0.02f, 1f))
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(progressColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = if (isExceeded)
                                "⚠️ Exceeded by $currencySymbol%,.0f".format(abs(item.variance))
                            else
                                "Remaining: $currencySymbol%,.0f".format(item.variance),
                            fontSize = 10.5.sp,
                            color = if (isExceeded) Color(0xFFEF4444) else SleekTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 💳 ACCOUNT-WISE SPENDING & INFLOW
// ==========================================

@Composable
fun AccountWiseAnalyticsSection(
    accounts: List<Account>,
    expenses: List<Expense>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Account Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Balances and activity across your accounts",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = SleekSizes.textCaption
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No accounts created yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary
                    )
                }
            } else {
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (acc.type) {
                                        "BANK" -> Icons.Default.AccountBalance
                                        "CREDIT" -> Icons.Default.CreditCard
                                        "SAVINGS" -> Icons.Default.Savings
                                        else -> Icons.Default.Payments
                                    },
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekTextPrimary)
                                Text(acc.type, fontSize = 10.5.sp, color = SleekTextSecondary)
                            }
                        }

                        Text(
                            text = "$currencySymbol%,.2f".format(acc.balance),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (acc.balance >= 0) SleekTextPrimary else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 🔥 SPENDING PATTERN & ANOMALY DETECTION
// ==========================================

@Composable
fun SpendingPatternDetectionSection(
    pattern: SpendingPatternSummary,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Habits & Spike Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Smart anomaly detection & weekly rhythm",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA580C).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val hasAnySpending = pattern.dayAverages.values.sum() > 0.0 && pattern.peakDayTotal > 0.0
            if (!hasAnySpending) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = SleekTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No spending recorded in this period yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SleekTextSecondary
                        )
                    }
                }
            } else {
                // Peak Spend Day Highlight Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF7ED))
                        .border(1.dp, Color(0xFFF97316).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🔥", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "Peak Spending Day: ${pattern.peakDayName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFC2410C)
                            )
                            val formattedPeakTotal = String.format(Locale.getDefault(), "%,.0f", pattern.peakDayTotal)
                            Text(
                                text = "${pattern.peakDayName} represents ${pattern.peakDayPercentage.roundToInt()}% of your active period expenses ($currencySymbol$formattedPeakTotal total).",
                                fontSize = 11.sp,
                                color = Color(0xFF9A3412)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Day of Week Distribution Bars
                Text("Day of Week Distribution", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                val maxDayVal = pattern.dayAverages.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    pattern.dayAverages.forEach { (day, amount) ->
                        val ratio = (amount / maxDayVal).toFloat().coerceIn(0.08f, 1f)
                        val isPeak = day == pattern.peakDayName

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (amount > 0) String.format(Locale.getDefault(), "%s%,.0f", currencySymbol, amount) else "-",
                                fontSize = 9.sp,
                                color = if (isPeak) Color(0xFFF97316) else SleekTextSecondary,
                                fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height((60 * ratio).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (isPeak) Color(0xFFF97316) else SleekPrimary.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPeak) Color(0xFFF97316) else SleekTextSecondary
                            )
                        }
                    }
                }

                // Anomaly / Spike alert cards if any
                if (pattern.unusualSpikes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("⚠️ Unusually High Expense Spikes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.height(6.dp))

                    pattern.unusualSpikes.take(3).forEach { spike ->
                        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(spike.note ?: spike.category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF991B1B))
                                Text("${spike.category} • ${sdf.format(Date(spike.date))}", fontSize = 11.sp, color = Color(0xFFB91C1C))
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, spike.amount),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 🔍 ENLARGED DRILL-DOWN MODAL INSPECTION
// ==========================================

@Composable
fun MetricInspectionModal(
    data: MetricInspectionData,
    onDismiss: () -> Unit,
    onExpenseClick: (Expense) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SleekSurface,
            border = BorderStroke(1.5.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = data.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = data.dateRangeLabel,
                            fontSize = 11.5.sp,
                            color = SleekTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SleekBorder.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextPrimary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Enlarged Amount Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekPrimaryContainer.copy(alpha = 0.45f))
                        .padding(18.dp)
                ) {
                    Column {
                        Text("Current Measurement", fontSize = 11.5.sp, color = SleekTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.primaryValueStr,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekTextPrimary
                        )
                        if (data.changePercent != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            PeriodChangeBadge(
                                changePercent = data.changePercent,
                                isPositiveGood = data.isPositiveGood,
                                prevLabel = "previous period"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Contributing Transactions
                Text(
                    text = "Contributing Transactions (${data.currentPeriodExpenses.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (data.currentPeriodExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No records found in this time range", fontSize = 13.sp, color = SleekTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(data.currentPeriodExpenses.sortedByDescending { it.date }) { exp ->
                            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            Surface(
                                onClick = { onExpenseClick(exp) },
                                shape = RoundedCornerShape(14.dp),
                                color = SleekBg,
                                border = BorderStroke(1.dp, SleekBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = exp.note ?: exp.category,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SleekTextPrimary
                                        )
                                        Text(
                                            text = "${exp.category} • ${sdf.format(Date(exp.date))}",
                                            fontSize = 11.sp,
                                            color = SleekTextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${data.currencySymbol}%,.2f".format(exp.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (exp.type == "INCOME") Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Inspector", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 🔍 CATEGORY DRILL-DOWN DIALOG
// ==========================================

@Composable
fun CategoryDrillDownDialog(
    item: CategoryTrendSummary,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onExpenseClick: (Expense) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SleekSurface,
            border = BorderStroke(1.5.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(getCategoryEmoji(item.category), fontSize = 26.sp)
                        Column {
                            Text(item.category, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Category Drill-Down Analytics", fontSize = 11.sp, color = SleekTextSecondary)
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SleekBorder.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextPrimary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SleekPrimaryContainer.copy(alpha = 0.4f))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Spent", fontSize = 11.sp, color = SleekTextSecondary)
                        Text("$currencySymbol%,.0f".format(item.currentAmount), fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekTextPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(SleekBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Share of Budget", fontSize = 11.sp, color = SleekTextSecondary)
                        Text("${item.percentageOfTotal.roundToInt()}%", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(SleekBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("vs Prev Period", fontSize = 11.sp, color = SleekTextSecondary)
                        PeriodChangeBadge(changePercent = item.changePercent, isPositiveGood = false, prevLabel = "prev")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("All ${item.transactions.size} Transactions in Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(item.transactions) { exp ->
                        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        Surface(
                            onClick = { onExpenseClick(exp) },
                            shape = RoundedCornerShape(14.dp),
                            color = SleekBg,
                            border = BorderStroke(1.dp, SleekBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exp.note ?: "No description",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SleekTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(sdf.format(Date(exp.date)), fontSize = 10.5.sp, color = SleekTextSecondary)
                                }
                                Text(
                                    text = "$currencySymbol%,.2f".format(exp.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 📅 CUSTOM DATE RANGE PICKER DIALOG
// ==========================================

@Composable
fun CustomDateRangePickerDialog(
    initialStartMs: Long,
    initialEndMs: Long,
    onDismiss: () -> Unit,
    onApplyRange: (Long, Long) -> Unit
) {
    var startCal by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialStartMs }) }
    var endCal by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialEndMs }) }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Custom Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                Text(
                    text = "Select custom boundary dates for detailed financial analytics.",
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )

                // Start Date Display Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekBg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SleekBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Start Date", fontSize = 11.sp, color = SleekTextSecondary)
                            Text(sdf.format(startCal.time), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekTextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    val c = Calendar.getInstance().apply { timeInMillis = startCal.timeInMillis; add(Calendar.DAY_OF_YEAR, -7) }
                                    startCal = c
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("-7d", fontSize = 11.sp)
                            }
                            FilledTonalButton(
                                onClick = {
                                    val c = Calendar.getInstance().apply { timeInMillis = startCal.timeInMillis; add(Calendar.DAY_OF_YEAR, 7) }
                                    if (c.timeInMillis < endCal.timeInMillis) startCal = c
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+7d", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // End Date Display Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekBg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SleekBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("End Date", fontSize = 11.sp, color = SleekTextSecondary)
                            Text(sdf.format(endCal.time), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekTextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    val c = Calendar.getInstance().apply { timeInMillis = endCal.timeInMillis; add(Calendar.DAY_OF_YEAR, -7) }
                                    if (c.timeInMillis > startCal.timeInMillis) endCal = c
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("-7d", fontSize = 11.sp)
                            }
                            FilledTonalButton(
                                onClick = {
                                    val c = Calendar.getInstance().apply { timeInMillis = endCal.timeInMillis; add(Calendar.DAY_OF_YEAR, 7) }
                                    endCal = c
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+7d", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val s = startCal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                            val e = endCal.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                            onApplyRange(s, e)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply Range", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
