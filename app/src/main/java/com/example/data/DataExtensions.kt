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

fun Map<String, Any?>.toExpense(): Expense {
    val currency = this["currencyCode"] as? String ?: "INR"
    val amt = (this["amount"] as? Number)?.toDouble() ?: 0.0
    val amtMinor = (this["amountMinor"] as? Number)?.toLong() ?: Money.fromDouble(amt, currency)
    val finalAmt = Money.toDouble(amtMinor, currency)
    return Expense(
        id = (this["id"] as? Number)?.toLong() ?: 0L,
        amount = finalAmt,
        amountMinor = amtMinor,
        category = this["category"] as? String ?: "Other",
        date = (this["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        note = this["note"] as? String,
        imagePath = this["imagePath"] as? String,
        type = this["type"] as? String ?: "EXPENSE",
        currencyCode = currency,
        accountId = (this["accountId"] as? Number)?.toLong() ?: 1L,
        kind = this["kind"] as? String ?: "EXPENSE",
        goalId = (this["goalId"] as? Number)?.toLong(),
        recurringRuleId = (this["recurringRuleId"] as? Number)?.toLong()
    )
}

fun Map<String, Any?>.toAccount(): Account {
    val currency = this["currencyCode"] as? String ?: "INR"
    val bal = (this["balance"] as? Number)?.toDouble() ?: 0.0
    val balMinor = (this["balanceMinor"] as? Number)?.toLong() ?: Money.fromDouble(bal, currency)
    val opBal = (this["openingBalance"] as? Number)?.toDouble() ?: 0.0
    val opBalMinor = (this["openingBalanceMinor"] as? Number)?.toLong() ?: Money.fromDouble(opBal, currency)
    return Account(
        id = (this["id"] as? Number)?.toLong() ?: 0L,
        name = this["name"] as? String ?: "Account",
        type = this["type"] as? String ?: "CASH",
        currencyCode = currency,
        openingBalance = Money.toDouble(opBalMinor, currency),
        balance = Money.toDouble(balMinor, currency),
        openingBalanceMinor = opBalMinor,
        balanceMinor = balMinor
    )
}

fun Map<String, Any?>.toBudget(): Budget {
    val currency = this["currencyCode"] as? String ?: "INR"
    val limit = (this["amountLimit"] as? Number)?.toDouble() ?: 0.0
    val limitMinor = (this["amountLimitMinor"] as? Number)?.toLong() ?: Money.fromDouble(limit, currency)
    return Budget(
        id = (this["id"] as? Number)?.toLong() ?: 0L,
        category = this["category"] as? String ?: "General",
        amountLimit = Money.toDouble(limitMinor, currency),
        amountLimitMinor = limitMinor,
        monthYear = this["monthYear"] as? String ?: "",
        currencyCode = currency
    )
}

fun Map<String, Any?>.toSavingsGoal(): SavingsGoal {
    val currency = this["currencyCode"] as? String ?: "INR"
    val target = (this["targetAmount"] as? Number)?.toDouble() ?: 0.0
    val targetMinor = (this["targetAmountMinor"] as? Number)?.toLong() ?: Money.fromDouble(target, currency)
    val current = (this["currentAmount"] as? Number)?.toDouble() ?: 0.0
    val currentMinor = (this["currentAmountMinor"] as? Number)?.toLong() ?: Money.fromDouble(current, currency)
    val contrib = (this["contributionAmount"] as? Number)?.toDouble() ?: 0.0
    val contribMinor = (this["contributionAmountMinor"] as? Number)?.toLong() ?: Money.fromDouble(contrib, currency)
    return SavingsGoal(
        id = (this["id"] as? Number)?.toLong() ?: 0L,
        name = this["name"] as? String ?: "Goal",
        targetAmount = Money.toDouble(targetMinor, currency),
        currentAmount = Money.toDouble(currentMinor, currency),
        targetAmountMinor = targetMinor,
        currentAmountMinor = currentMinor,
        targetDate = (this["targetDate"] as? Number)?.toLong() ?: 0L,
        frequency = this["frequency"] as? String ?: "WEEKLY",
        contributionAmount = Money.toDouble(contribMinor, currency),
        contributionAmountMinor = contribMinor,
        isAutoGap = (this["isAutoGap"] as? Boolean) ?: true,
        iconTag = this["iconTag"] as? String ?: "🎮",
        category = this["category"] as? String ?: "Saving",
        imageUri = this["imageUri"] as? String,
        currencyCode = currency
    )
}

fun Map<String, Any?>.toRecurringRule(): RecurringRule {
    val currency = this["currencyCode"] as? String ?: "INR"
    val amtMinor = (this["amountMinor"] as? Number)?.toLong() ?: 0L
    return RecurringRule(
        id = (this["id"] as? Number)?.toLong() ?: 0L,
        title = this["title"] as? String ?: "Rule",
        amountMinor = amtMinor,
        category = this["category"] as? String ?: "General",
        frequency = this["frequency"] as? String ?: "MONTHLY",
        startDate = (this["startDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        nextDueDate = (this["nextDueDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        isActive = (this["isActive"] as? Boolean) ?: true,
        currencyCode = currency,
        accountId = (this["accountId"] as? Number)?.toLong() ?: 1L,
        note = this["note"] as? String
    )
}

fun Map<String, Any?>.toReminderEntity(): ReminderEntity {
    return ReminderEntity(
        id = (this["id"] as? Number)?.toLong() ?: 0L,
        text = this["text"] as? String ?: "",
        dueDate = (this["dueDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        isCompleted = (this["isCompleted"] as? Boolean) ?: false,
        isEnabled = (this["isEnabled"] as? Boolean) ?: true
    )
}

