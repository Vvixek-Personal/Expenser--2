package com.example.data

fun Expense.toMap() = mapOf(
    "id" to id,
    "amount" to amount,
    "amountMinor" to amountMinor,
    "category" to category,
    "date" to date,
    "note" to note,
    "imagePath" to imagePath,
    "type" to type,
    "currencyCode" to currencyCode,
    "accountId" to accountId,
    "kind" to kind,
    "goalId" to goalId,
    "recurringRuleId" to recurringRuleId
)

fun Account.toMap() = mapOf(
    "id" to id,
    "name" to name,
    "type" to type,
    "currencyCode" to currencyCode,
    "openingBalance" to openingBalance,
    "balance" to balance,
    "openingBalanceMinor" to openingBalanceMinor,
    "balanceMinor" to balanceMinor
)

fun Budget.toMap() = mapOf(
    "id" to id,
    "category" to category,
    "amountLimit" to amountLimit,
    "amountLimitMinor" to amountLimitMinor,
    "monthYear" to monthYear,
    "currencyCode" to currencyCode
)

fun SavingsGoal.toMap() = mapOf(
    "id" to id,
    "name" to name,
    "targetAmount" to targetAmount,
    "currentAmount" to currentAmount,
    "targetAmountMinor" to targetAmountMinor,
    "currentAmountMinor" to currentAmountMinor,
    "targetDate" to targetDate,
    "frequency" to frequency,
    "contributionAmount" to contributionAmount,
    "contributionAmountMinor" to contributionAmountMinor,
    "isAutoGap" to isAutoGap,
    "iconTag" to iconTag,
    "category" to category,
    "imageUri" to imageUri,
    "currencyCode" to currencyCode
)

fun RecurringRule.toMap() = mapOf(
    "id" to id,
    "title" to title,
    "amountMinor" to amountMinor,
    "category" to category,
    "frequency" to frequency,
    "startDate" to startDate,
    "nextDueDate" to nextDueDate,
    "isActive" to isActive,
    "currencyCode" to currencyCode,
    "accountId" to accountId,
    "note" to note
)

fun ReminderEntity.toMap() = mapOf(
    "id" to id,
    "text" to text,
    "dueDate" to dueDate,
    "isCompleted" to isCompleted,
    "isEnabled" to isEnabled
)
