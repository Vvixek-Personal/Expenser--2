package com.example.data

import android.content.Context
import java.util.*

object RecurringProcessor {

    private fun extractEndDate(rule: RecurringRule): Long? {
        val note = rule.note ?: return null
        val match = Regex("\\[END:(\\d+)\\]").find(note)
        return match?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    private fun extractRuleType(rule: RecurringRule): String {
        val note = rule.note ?: ""
        return when {
            note.contains("[TYPE:INCOME]") -> "INCOME"
            note.contains("[TYPE:EXPENSE]") -> "EXPENSE"
            rule.category.equals("Salary", ignoreCase = true) ||
            rule.category.equals("Income", ignoreCase = true) ||
            rule.category.equals("Investment", ignoreCase = true) ||
            rule.category.equals("Bonus", ignoreCase = true) -> "INCOME"
            else -> "EXPENSE"
        }
    }

    fun occurrencesBetween(rule: RecurringRule, startTime: Long, endTime: Long): List<Long> {
        val occurrences = mutableListOf<Long>()
        val endDate = extractEndDate(rule)
        val cal = Calendar.getInstance().apply {
            timeInMillis = rule.startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (cal.timeInMillis <= endTime) {
            val occurrenceTime = cal.timeInMillis
            if (endDate != null && occurrenceTime > endDate) break
            if (occurrenceTime >= startTime) {
                occurrences.add(occurrenceTime)
            }
            
            when (rule.frequency) {
                "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                "YEARLY" -> cal.add(Calendar.YEAR, 1)
                else -> return occurrences // Prevent infinite loop
            }
        }
        return occurrences
    }

    suspend fun processRecurringRules(context: Context, db: FinanceDatabase) {
        val dao = db.financeDao()
        val rules = dao.getRecurringRulesSnapshot()
        val now = System.currentTimeMillis()

        for (rule in rules) {
            if (!rule.isActive) continue
            val endDate = extractEndDate(rule)
            val ruleType = extractRuleType(rule)
            
            var nextDue = rule.nextDueDate
            while (nextDue <= now) {
                if (endDate != null && nextDue > endDate) {
                    dao.updateRecurringRule(rule.copy(isActive = false))
                    break
                }

                // Create transaction for this occurrence if it doesn't already exist
                if (!dao.recurringExpenseExists(rule.id, nextDue)) {
                    val cleanNote = (rule.note ?: "Recurring: ${rule.title}")
                        .replace(Regex("\\[TYPE:[^\\]]+\\]"), "")
                        .replace(Regex("\\[END:[^\\]]+\\]"), "")
                        .trim()

                    val expense = Expense(
                        amount = Money.toDouble(rule.amountMinor, rule.currencyCode),
                        amountMinor = rule.amountMinor,
                        currencyCode = rule.currencyCode,
                        category = rule.category,
                        date = nextDue,
                        note = cleanNote.ifBlank { "Recurring: ${rule.title}" },
                        type = ruleType,
                        accountId = rule.accountId,
                        kind = "RECURRING",
                        recurringRuleId = rule.id
                    )
                    dao.insertExpense(expense)
                }
                
                // Update next due date
                val cal = Calendar.getInstance().apply {
                    timeInMillis = nextDue
                }
                when (rule.frequency) {
                    "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                    "YEARLY" -> cal.add(Calendar.YEAR, 1)
                }
                nextDue = cal.timeInMillis
            }
            
            if (nextDue != rule.nextDueDate && (endDate == null || nextDue <= endDate)) {
                dao.updateRecurringRule(rule.copy(nextDueDate = nextDue))
            }
        }
    }
}
