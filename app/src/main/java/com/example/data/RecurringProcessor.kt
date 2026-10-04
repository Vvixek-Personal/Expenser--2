package com.example.data

import android.content.Context
import java.util.*

object RecurringProcessor {

    fun occurrencesBetween(rule: RecurringRule, startTime: Long, endTime: Long): List<Long> {
        val occurrences = mutableListOf<Long>()
        val cal = Calendar.getInstance().apply {
            timeInMillis = rule.startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (cal.timeInMillis <= endTime) {
            val occurrenceTime = cal.timeInMillis
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
            
            var nextDue = rule.nextDueDate
            while (nextDue <= now) {
                // Create expense for this occurrence
                val expense = Expense(
                    amount = Money.toDouble(rule.amountMinor, rule.currencyCode),
                    amountMinor = rule.amountMinor,
                    currencyCode = rule.currencyCode,
                    category = rule.category,
                    date = nextDue,
                    note = rule.note ?: "Recurring: ${rule.title}",
                    type = "EXPENSE",
                    accountId = rule.accountId,
                    recurringRuleId = rule.id
                )
                dao.insertExpense(expense)
                
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
            
            if (nextDue != rule.nextDueDate) {
                dao.updateRecurringRule(rule.copy(nextDueDate = nextDue))
            }
        }
    }
}
