package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Expense
import com.example.data.SavingsGoal
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * ⚡ Quick Action Hub for Home Screen
 * 1-tap shortcuts for immediate financial operations
 */
@Composable
fun DashboardQuickActionHub(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onQuickSplit: () -> Unit,
    onQuickConvert: () -> Unit,
    onViewGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickActionButton(
                icon = Icons.Default.TrendingDown,
                label = "+ Expense",
                tintColor = Color(0xFFEF4444),
                bgBrush = Brush.linearGradient(listOf(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626).copy(alpha = 0.08f))),
                onClick = onAddExpense,
                testTag = "home_quick_add_expense"
            )

            QuickActionButton(
                icon = Icons.Default.TrendingUp,
                label = "+ Income",
                tintColor = Color(0xFF10B981),
                bgBrush = Brush.linearGradient(listOf(Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF059669).copy(alpha = 0.08f))),
                onClick = onAddIncome,
                testTag = "home_quick_add_income"
            )

            QuickActionButton(
                icon = Icons.Default.CallSplit,
                label = "Split Bill",
                tintColor = Color(0xFF6366F1),
                bgBrush = Brush.linearGradient(listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color(0xFF4F46E5).copy(alpha = 0.08f))),
                onClick = onQuickSplit,
                testTag = "home_quick_split_bill"
            )

            QuickActionButton(
                icon = Icons.Default.CurrencyExchange,
                label = "Convert",
                tintColor = Color(0xFF0EA5E9),
                bgBrush = Brush.linearGradient(listOf(Color(0xFF0EA5E9).copy(alpha = 0.15f), Color(0xFF0284C7).copy(alpha = 0.08f))),
                onClick = onQuickConvert,
                testTag = "home_quick_convert"
            )

            QuickActionButton(
                icon = Icons.Default.Savings,
                label = "Goals",
                tintColor = Color(0xFFF59E0B),
                bgBrush = Brush.linearGradient(listOf(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706).copy(alpha = 0.08f))),
                onClick = onViewGoals,
                testTag = "home_quick_goals"
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tintColor: Color,
    bgBrush: Brush,
    onClick: () -> Unit,
    testTag: String
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btn_press_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgBrush)
                .border(BorderStroke(1.dp, tintColor.copy(alpha = 0.3f)), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SleekTextPrimary,
            maxLines = 1
        )
    }
}

/**
 * 📅 Safe-to-Spend Today / Daily Spending Allowance Widget
 * Calculates the exact safe daily spending budget based on remaining monthly budget and remaining days.
 */
@Composable
fun DailySpendingAllowanceWidget(
    monthlyBudget: Double,
    currentMonthExpense: Double,
    todayExpense: Double,
    currencySymbol: String,
    onAdjustBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val daysInMonth = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val currentDay = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)

    val remainingMonthlyBudget = (monthlyBudget - currentMonthExpense).coerceAtLeast(0.0)
    val safeDailyAllowance = if (monthlyBudget > 0) {
        (remainingMonthlyBudget / remainingDays).coerceAtLeast(0.0)
    } else 0.0

    val remainingToday = (safeDailyAllowance - todayExpense).coerceAtLeast(0.0)
    val isOverToday = monthlyBudget > 0 && todayExpense > safeDailyAllowance
    val todayProgress = if (safeDailyAllowance > 0) {
        (todayExpense / safeDailyAllowance).toFloat().coerceIn(0f, 1f)
    } else 0f

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_daily_allowance_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0EA5E9).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = Color(0xFF0EA5E9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Daily Spending Pace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "$remainingDays days remaining this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (monthlyBudget > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isOverToday) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isOverToday) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isOverToday) "Over Target" else "On Track",
                            color = if (isOverToday) Color(0xFFDC2626) else Color(0xFF059669),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    TextButton(
                        onClick = onAdjustBudget,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Set Budget", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (monthlyBudget > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Safe to spend today",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, safeDailyAllowance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isOverToday) "Over budget by" else "Remaining for today",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverToday) Color(0xFFEF4444) else SleekTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isOverToday) {
                                String.format(Locale.getDefault(), "+%s%,.2f", currencySymbol, todayExpense - safeDailyAllowance)
                            } else {
                                String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, remainingToday)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverToday) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }

                // Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { todayProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (isOverToday) Color(0xFFEF4444) else Color(0xFF0EA5E9),
                        trackColor = SleekBorder.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: ${String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, todayExpense)}",
                            fontSize = 10.sp,
                            color = SleekTextSecondary
                        )
                        Text(
                            text = "Target: ${String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, safeDailyAllowance)}/day",
                            fontSize = 10.sp,
                            color = SleekTextSecondary
                        )
                    }
                }
            } else {
                Text(
                    text = "Configure a monthly budget target to unlock smart daily spending allowances and stay on track!",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }
        }
    }
}

/**
 * 🧾 Upcoming Bills Widget for Home Screen
 * Displays upcoming or overdue bills with 1-tap "Pay Now" action
 */
@Composable
fun UpcomingBillsDashboardWidget(
    bills: List<BillEntry>,
    currencySymbol: String,
    onPayBill: (BillEntry) -> Unit,
    onViewAllBills: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_upcoming_bills_widget")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Upcoming Bills",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = if (bills.isNotEmpty()) "${bills.size} pending due" else "No pending dues",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(
                    onClick = onViewAllBills,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("View All", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            }

            if (bills.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "All caught up! No bills pending right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bills.take(3).forEach { bill ->
                        val isDueToday = bill.dueDate == todayStr
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDueToday) Color(0xFFF59E0B).copy(alpha = 0.08f) else SleekBg,
                            border = BorderStroke(1.dp, if (isDueToday) Color(0xFFF59E0B).copy(alpha = 0.4f) else SleekBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isDueToday) Color(0xFFF59E0B).copy(alpha = 0.2f) else SleekSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payment,
                                            contentDescription = null,
                                            tint = if (isDueToday) Color(0xFFD97706) else SleekPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = bill.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekTextPrimary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (isDueToday) "Due Today!" else "Due ${bill.dueDate}",
                                                fontSize = 11.sp,
                                                color = if (isDueToday) Color(0xFFD97706) else SleekTextSecondary,
                                                fontWeight = if (isDueToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, bill.amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )

                                    Button(
                                        onClick = { onPayBill(bill) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎯 Savings Goals Spotlight Widget for Home Screen
 * Displays the top active savings goal with progress & 1-tap quick deposit
 */
@Composable
fun SavingsGoalSpotlightWidget(
    goals: List<SavingsGoal>,
    currencySymbol: String,
    onQuickDeposit: (SavingsGoal) -> Unit,
    onViewAllGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (goals.isEmpty()) return

    val topGoal = remember(goals) {
        goals.firstOrNull { it.currentAmount < it.targetAmount } ?: goals.first()
    }
    val progressFraction = remember(topGoal) {
        if (topGoal.targetAmount > 0) {
            (topGoal.currentAmount / topGoal.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f
    }
    val percentInt = remember(progressFraction) { (progressFraction * 100).roundToInt() }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_savings_goal_spotlight")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(topGoal.iconTag.ifEmpty { "🎯" }, fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = topGoal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Savings Target • $percentInt% saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(
                    onClick = onViewAllGoals,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("All Goals", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // Amounts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saved so far",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, topGoal.currentAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Goal Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, topGoal.targetAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFFF59E0B),
                trackColor = SleekBorder.copy(alpha = 0.5f)
            )

            // Quick Deposit Button
            FilledTonalButton(
                onClick = { onQuickDeposit(topGoal) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFF59E0B).copy(alpha = 0.14f),
                    contentColor = Color(0xFFB45309)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Deposit to this Goal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/**
 * 📊 Monthly Top Spending Categories Widget
 * Visual breakdown of top categories this month with progress bars
 */
@Composable
fun TopSpendingCategoriesWidget(
    expensesThisMonth: List<Expense>,
    categoryIcons: Map<String, String>,
    currencySymbol: String,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (expensesThisMonth.isEmpty()) return

    val totalSpent = remember(expensesThisMonth) {
        expensesThisMonth.sumOf { it.amount }.coerceAtLeast(1.0)
    }

    val topCategories = remember(expensesThisMonth) {
        expensesThisMonth
            .groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                cat to sum
            }
            .sortedByDescending { it.second }
            .take(4)
    }

    val palette = remember {
        listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF0EA5E9), // Sky
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899)  // Pink
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_top_categories_widget")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Top Spending This Month",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Where your money went",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(
                    onClick = onNavigateToAnalytics,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Analytics", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                topCategories.forEachIndexed { index, (category, amount) ->
                    val pct = ((amount / totalSpent) * 100).coerceIn(0.0, 100.0)
                    val color = palette[index % palette.size]
                    val iconStr = categoryIcons[category] ?: "💳"

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(iconStr, fontSize = 16.sp)
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SleekTextPrimary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%,.0f", currencySymbol, amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "(${pct.roundToInt()}%)",
                                    fontSize = 11.sp,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (pct / 100.0).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = SleekBorder.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 💡 Daily Smart Financial Insight / Health Card
 * Contextual tips and feedback computed from real financial data
 */
@Composable
fun DailyFinancialInsightWidget(
    thisMonthIncome: Double,
    thisMonthExpense: Double,
    monthlyBudget: Double,
    streakCount: Int,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var isDismissed by remember { mutableStateOf(false) }
    if (isDismissed) return

    val insightMessage = remember(thisMonthIncome, thisMonthExpense, monthlyBudget, streakCount) {
        when {
            monthlyBudget > 0 && thisMonthExpense > monthlyBudget -> {
                "⚠️ You have exceeded your monthly budget by ${currencySymbol}${String.format(Locale.getDefault(), "%,.0f", thisMonthExpense - monthlyBudget)}. Review discretionary categories to rebalance."
            }
            monthlyBudget > 0 && (thisMonthExpense / monthlyBudget) >= 0.85 -> {
                "⚡ You've utilized ${((thisMonthExpense / monthlyBudget) * 100).roundToInt()}% of your budget. Pacing yourself for the rest of the month will keep you in the green!"
            }
            thisMonthIncome > 0 && (thisMonthIncome - thisMonthExpense) > 0 -> {
                val savingsRate = (((thisMonthIncome - thisMonthExpense) / thisMonthIncome) * 100).roundToInt()
                "🌟 Outstanding! You're currently saving $savingsRate% of your income this month. Consider locking a portion into a Savings Goal."
            }
            streakCount >= 3 -> {
                "🔥 Impressive consistency! You're on a $streakCount-day tracking streak. Regular logging is the #1 predictor of financial freedom."
            }
            else -> {
                "💡 Pro-Tip: Log transactions as soon as they happen. Accurate tracking gives you full control over your cash flow."
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SleekPrimaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SleekPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = insightMessage,
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextPrimary,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { isDismissed = true },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = SleekTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * ⚡ Quick Deposit Dialog for Home Screen
 */
@Composable
fun QuickGoalDepositDialog(
    goal: SavingsGoal,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirmDeposit: (Double) -> Unit
) {
    var depositText by remember { mutableStateOf("500") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Deposit to ${goal.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                Text(
                    text = "Target: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", goal.targetAmount)} | Saved: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", goal.currentAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )

                OutlinedTextField(
                    value = depositText,
                    onValueChange = { depositText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Deposit Amount") },
                    leadingIcon = {
                        Text(
                            text = currencySymbol,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("100", "500", "1000", "2000").forEach { preset ->
                        FilledTonalButton(
                            onClick = { depositText = preset },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+$preset", fontSize = 11.sp)
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
                        Text("Cancel", color = SleekTextSecondary)
                    }

                    Button(
                        onClick = {
                            val amt = depositText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirmDeposit(amt)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deposit", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * ⚡ Quick Split Bill Dialog directly accessible on Home Screen
 */
@Composable
fun QuickSplitBillDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onRecordExpense: (Double, String) -> Unit
) {
    var billText by remember { mutableStateOf("1200") }
    var tipPercent by remember { mutableIntStateOf(10) }
    var peopleCount by remember { mutableIntStateOf(3) }

    val bill = billText.toDoubleOrNull() ?: 0.0
    val tip = (bill * tipPercent) / 100.0
    val total = bill + tip
    val perPerson = if (peopleCount > 0) total / peopleCount else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Quick Split Bill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextSecondary)
                    }
                }

                // Summary result box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Per Person Share", fontSize = 11.sp, color = SleekTextSecondary)
                        Text(
                            text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, perPerson),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1)
                        )
                        Text(
                            text = "Total: $currencySymbol${String.format(Locale.getDefault(), "%,.2f", total)} (Tip: $currencySymbol${String.format(Locale.getDefault(), "%,.2f", tip)})",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                OutlinedTextField(
                    value = billText,
                    onValueChange = { billText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Total Bill Amount") },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, color = SleekPrimary, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // People counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Split between:", style = MaterialTheme.typography.bodyMedium, color = SleekTextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { if (peopleCount > 1) peopleCount-- },
                            modifier = Modifier.size(32.dp).background(SleekBg, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text("$peopleCount People", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = { peopleCount++ },
                            modifier = Modifier.size(32.dp).background(SleekBg, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Tip Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 5, 10, 15, 20).forEach { tip ->
                        FilterChip(
                            selected = tipPercent == tip,
                            onClick = { tipPercent = tip },
                            label = { Text("$tip% Tip", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        onRecordExpense(perPerson, "Split bill ($peopleCount people)")
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record My Share as Expense")
                }
            }
        }
    }
}

/**
 * 💱 Quick Currency Convert Dialog directly accessible on Home Screen
 */
@Composable
fun QuickCurrencyConvertDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onOpenFullSettings: () -> Unit
) {
    var amountText by remember { mutableStateOf("100") }
    var targetCode by remember { mutableStateOf(if (currencyCode == "USD") "EUR" else "USD") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val targetCurrency = remember(targetCode) { CurrencyManager.getByCode(targetCode) }
    val sourceCurrency = remember(currencyCode) { CurrencyManager.getByCode(currencyCode) }

    val converted = remember(amount, currencyCode, targetCode) {
        CurrencyManager.convert(amount, currencyCode, targetCode)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💱 Quick Converter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SleekPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${targetCurrency.flag} ${targetCurrency.name}", fontSize = 12.sp, color = SleekTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${targetCurrency.symbol}${String.format(Locale.getDefault(), "%,.2f", converted)} ${targetCurrency.code}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        Text(
                            text = "${sourceCurrency.symbol}${String.format(Locale.getDefault(), "%,.2f", amount)} $currencyCode",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount in $currencyCode") },
                    leadingIcon = { Text(sourceCurrency.symbol, fontWeight = FontWeight.Bold, color = SleekPrimary, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick target currency chips
                Text("Convert into:", fontSize = 12.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("USD", "EUR", "GBP", "INR", "AED", "JPY").filter { it != currencyCode }.take(4).forEach { code ->
                        val item = CurrencyManager.getByCode(code)
                        FilterChip(
                            selected = targetCode == code,
                            onClick = { targetCode = code },
                            label = { Text("${item.flag} $code", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFullSettings()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("100+ More", fontSize = 12.sp, color = SleekPrimary)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
     * 🎯 Interactive Savings Goal Mini-Carousel Widget
     */
@Composable
fun SavingsGoalsMiniCarouselWidget(
    goals: List<SavingsGoal>,
    currencySymbol: String,
    onQuickDeposit: (SavingsGoal, Double) -> Unit,
    onViewAllGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (goals.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Active Savings Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
            TextButton(onClick = onViewAllGoals) {
                Text("View All", color = SleekPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(goals) { goal ->
                val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                val pct = (progress * 100).toInt()

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .width(240.dp)
                        .testTag("savings_goal_carousel_card_\${goal.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(goal.iconTag.ifEmpty { "🎯" }, fontSize = 16.sp)
                                }
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(110.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("$pct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(String.format("%s%,.0f", currencySymbol, goal.currentAmount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                Text(String.format("of %s%,.0f", currencySymbol, goal.targetAmount), style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFF59E0B),
                                trackColor = SleekBorder.copy(alpha = 0.5f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onQuickDeposit(goal, 50.0) },
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Text("+50", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                            OutlinedButton(
                                onClick = { onQuickDeposit(goal, 100.0) },
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Text("+100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ⚡ Smart Budget Health Gauge Ring Widget
 */
@Composable
fun SmartBudgetHealthGaugeWidget(
    monthlyBudget: Double,
    currentMonthExpense: Double,
    currencySymbol: String,
    onUpdateBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (monthlyBudget <= 0) return

    val spentPct = ((currentMonthExpense / monthlyBudget) * 100).coerceAtLeast(0.0)
    val remaining = monthlyBudget - currentMonthExpense
    val isExceeded = remaining < 0
    val progressFraction = (currentMonthExpense / monthlyBudget).toFloat().coerceIn(0f, 1f)

    val gaugeColor = when {
        isExceeded -> Color(0xFFEF4444)
        spentPct >= 80 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val statusText = when {
        isExceeded -> "Budget Exceeded!"
        spentPct >= 90 -> "Critical (90%+ Spent)"
        spentPct >= 80 -> "Warning (80%+ Spent)"
        else -> "Healthy & On Track"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_budget_health_gauge")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(gaugeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = gaugeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Smart Budget Health Gauge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = gaugeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(
                    onClick = onUpdateBudget,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Adjust", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Spent this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                    Text(
                        text = String.format("%s%,.2f", currencySymbol, currentMonthExpense),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = String.format("Limit: %s%,.2f (%d%%)", currencySymbol, monthlyBudget, spentPct.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        drawArc(
                            color = SleekBorder,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        drawArc(
                            color = gaugeColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progressFraction,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${spentPct.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Used",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🌟 Dynamic Financial Health Score & Diagnostics Widget
 * Calculates 0-100 real-time score from Savings Rate, Budget Adherence, Bill Promptness, and Liquidity
 */
@Composable
fun FinancialHealthScoreWidget(
    incomeThisMonth: Double,
    expenseThisMonth: Double,
    monthlyBudget: Double,
    overdueBillsCount: Int,
    currencySymbol: String,
    onExploreBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDiagnostics by remember { mutableStateOf(false) }

    // Dynamic score calculation
    val savingsRatio = if (incomeThisMonth > 0) ((incomeThisMonth - expenseThisMonth) / incomeThisMonth).coerceIn(0.0, 1.0) else 0.4
    val budgetScore = if (monthlyBudget > 0) {
        val spentRatio = expenseThisMonth / monthlyBudget
        when {
            spentRatio <= 0.7 -> 1.0
            spentRatio <= 0.9 -> 0.8
            spentRatio <= 1.0 -> 0.6
            else -> 0.2
        }
    } else 0.7

    val billScore = if (overdueBillsCount == 0) 1.0 else (1.0 - overdueBillsCount * 0.25).coerceAtLeast(0.0)
    val overallScore = ((savingsRatio * 40) + (budgetScore * 40) + (billScore * 20)).roundToInt().coerceIn(15, 99)

    val (badgeText, badgeColor, tipText) = when {
        overallScore >= 80 -> Triple("EXCELLENT", Color(0xFF10B981), "Outstanding financial discipline! You're saving consistently and well within your limits.")
        overallScore >= 65 -> Triple("STABLE", Color(0xFF0EA5E9), "Good financial position. Try increasing your monthly savings rate to reach 'Excellent'.")
        overallScore >= 45 -> Triple("MODERATE", Color(0xFFF59E0B), "Caution advised: High budget burn rate detected this cycle. Review non-essential expenses.")
        else -> Triple("ACTION NEEDED", Color(0xFFEF4444), "Immediate action needed: Expenses exceed safe thresholds or overdue bills need settlement.")
    }

    val animatedScore by animateIntAsState(
        targetValue = overallScore,
        animationSpec = androidx.compose.animation.core.tween(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "health_score"
    )

    val sweepProgress by animateFloatAsState(
        targetValue = overallScore / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "gauge_sweep"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_financial_health_score")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HealthAndSafety,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Financial Wellness Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Real-time AI Health Diagnostic",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$animatedScore",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        )
                        Text(
                            text = " / 100",
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekTextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tipText,
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Custom Canvas Radial Gauge
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 8.dp.toPx()
                        drawArc(
                            color = SleekBorder,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color(0xFF6366F1), badgeColor, badgeColor)
                            ),
                            startAngle = 135f,
                            sweepAngle = 270f * sweepProgress,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.AutoGraph,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Diagnostic Drilldown Expand Toggle
            HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedDiagnostics = !expandedDiagnostics },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expandedDiagnostics) "Hide Diagnostic Pillars" else "View Diagnostic Breakdown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekPrimary
                )
                Icon(
                    imageVector = if (expandedDiagnostics) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expandedDiagnostics,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthPillarRow("Savings Efficiency", "${(savingsRatio * 100).toInt()}% Net Saved", if (savingsRatio >= 0.2) Color(0xFF10B981) else Color(0xFFF59E0B))
                    HealthPillarRow("Budget Burn Rate", if (monthlyBudget > 0) "${((expenseThisMonth / monthlyBudget) * 100).toInt()}% Utilized" else "No limit set", if (budgetScore >= 0.8) Color(0xFF10B981) else Color(0xFFEF4444))
                    HealthPillarRow("Bill Punctuality", if (overdueBillsCount == 0) "100% On-Time" else "$overdueBillsCount Overdue Bill(s)", if (overdueBillsCount == 0) Color(0xFF10B981) else Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
private fun HealthPillarRow(title: String, value: String, indicatorColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SleekBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(indicatorColor))
            Text(title, style = MaterialTheme.typography.bodySmall, color = SleekTextPrimary, fontWeight = FontWeight.Medium)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = indicatorColor)
    }
}
