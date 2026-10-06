package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao, private val database: FinanceDatabase) {
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val allAccounts: Flow<List<Account>> = dao.getAllAccounts()
    val allBudgets: Flow<List<Budget>> = dao.getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoal>> = dao.getAllSavingsGoals()
    val allRecurringRules: Flow<List<RecurringRule>> = dao.getAllRecurringRules()
    val allReminders: Flow<List<ReminderEntity>> = dao.getAllReminders()

    suspend fun insertExpense(expense: Expense): Long {
        val targetAccountId = if (expense.accountId > 0L) {
            if (dao.getAccountById(expense.accountId) == null) {
                dao.insertAccount(
                    Account(
                        id = expense.accountId,
                        name = "Cash",
                        type = "CASH",
                        currencyCode = expense.currencyCode,
                        openingBalanceMinor = 0L,
                        balanceMinor = 0L
                    )
                )
            }
            expense.accountId
        } else {
            dao.getAccountsSnapshot().firstOrNull()?.id ?: dao.insertAccount(
                Account(
                    name = "Cash",
                    type = "CASH",
                    currencyCode = expense.currencyCode,
                    openingBalanceMinor = 0L,
                    balanceMinor = 0L
                )
            )
        }

        val toInsert = syncExpenseAmounts(expense.copy(accountId = targetAccountId))
        return dao.insertExpense(toInsert)
    }

    suspend fun updateExpense(expense: Expense) = dao.updateExpense(syncExpenseAmounts(expense))
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = dao.deleteExpenseById(id)
    suspend fun deleteAllExpenses() = dao.deleteAllExpenses()

    suspend fun insertAccount(account: Account): Long {
        return dao.insertAccount(syncAccountAmounts(account))
    }

    suspend fun updateAccount(account: Account) = dao.updateAccount(syncAccountAmounts(account))
    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)
    suspend fun getAccountById(id: Long) = dao.getAccountById(id)

    suspend fun getExpenseById(id: Long) = dao.getExpenseById(id)

    suspend fun computeAccountBalanceMinor(accountId: Long): Long {
        val account = dao.getAccountById(accountId) ?: return 0L
        val expenses = dao.getExpensesSnapshot().filter { it.accountId == accountId }
        val netChange = expenses.sumOf { exp ->
            val sign = if (exp.type == "INCOME") 1L else -1L
            val amountInAccountCurrencyMinor = if (exp.currencyCode == account.currencyCode) {
                exp.amountMinor
            } else {
                Money.convert(exp.amountMinor, exp.currencyCode, account.currencyCode)
            }
            sign * amountInAccountCurrencyMinor
        }
        return account.openingBalanceMinor + netChange
    }

    suspend fun insertBudget(budget: Budget) = dao.insertBudget(syncBudgetAmounts(budget))
    suspend fun updateBudget(budget: Budget) = dao.updateBudget(syncBudgetAmounts(budget))
    suspend fun deleteBudget(budget: Budget) = dao.deleteBudget(budget)

    suspend fun insertSavingsGoal(goal: SavingsGoal) = dao.insertSavingsGoal(syncSavingsGoalAmounts(goal))
    suspend fun updateSavingsGoal(goal: SavingsGoal) = dao.updateSavingsGoal(syncSavingsGoalAmounts(goal))
    suspend fun deleteSavingsGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    suspend fun insertRecurringRule(rule: RecurringRule) = dao.insertRecurringRule(rule)
    suspend fun updateRecurringRule(rule: RecurringRule) = dao.updateRecurringRule(rule)
    suspend fun deleteRecurringRule(rule: RecurringRule) = dao.deleteRecurringRule(rule)
    suspend fun getRecurringRuleById(id: Long) = dao.getRecurringRuleById(id)

    private fun syncExpenseAmounts(expense: Expense): Expense {
        val currency = expense.currencyCode
        return when {
            expense.amount != 0.0 && expense.amountMinor == 0L -> {
                expense.copy(amountMinor = Money.fromDouble(expense.amount, currency))
            }
            expense.amount == 0.0 && expense.amountMinor != 0L -> {
                expense.copy(amount = Money.toDouble(expense.amountMinor, currency))
            }
            expense.amount != 0.0 && expense.amountMinor != 0L -> {
                val expectedMinor = Money.fromDouble(expense.amount, currency)
                if (expectedMinor != expense.amountMinor) {
                    expense.copy(amountMinor = expectedMinor)
                } else {
                    expense
                }
            }
            else -> expense
        }
    }

    private fun syncAccountAmounts(account: Account): Account {
        val currency = account.currencyCode
        var acc = account
        if (acc.openingBalance != 0.0 && acc.openingBalanceMinor == 0L) {
            acc = acc.copy(openingBalanceMinor = Money.fromDouble(acc.openingBalance, currency))
        } else if (acc.openingBalance == 0.0 && acc.openingBalanceMinor != 0L) {
            acc = acc.copy(openingBalance = Money.toDouble(acc.openingBalanceMinor, currency))
        } else if (acc.openingBalance != 0.0 && acc.openingBalanceMinor != 0L) {
            val expMinor = Money.fromDouble(acc.openingBalance, currency)
            if (expMinor != acc.openingBalanceMinor) acc = acc.copy(openingBalanceMinor = expMinor)
        }

        if (acc.balance != 0.0 && acc.balanceMinor == 0L) {
            acc = acc.copy(balanceMinor = Money.fromDouble(acc.balance, currency))
        } else if (acc.balance == 0.0 && acc.balanceMinor != 0L) {
            acc = acc.copy(balance = Money.toDouble(acc.balanceMinor, currency))
        } else if (acc.balance != 0.0 && acc.balanceMinor != 0L) {
            val expMinor = Money.fromDouble(acc.balance, currency)
            if (expMinor != acc.balanceMinor) acc = acc.copy(balanceMinor = expMinor)
        }
        return acc
    }

    private fun syncBudgetAmounts(budget: Budget): Budget {
        val currency = budget.currencyCode
        return when {
            budget.amountLimit != 0.0 && budget.amountLimitMinor == 0L -> {
                budget.copy(amountLimitMinor = Money.fromDouble(budget.amountLimit, currency))
            }
            budget.amountLimit == 0.0 && budget.amountLimitMinor != 0L -> {
                budget.copy(amountLimit = Money.toDouble(budget.amountLimitMinor, currency))
            }
            budget.amountLimit != 0.0 && budget.amountLimitMinor != 0L -> {
                val expMinor = Money.fromDouble(budget.amountLimit, currency)
                if (expMinor != budget.amountLimitMinor) budget.copy(amountLimitMinor = expMinor) else budget
            }
            else -> budget
        }
    }

    private fun syncSavingsGoalAmounts(goal: SavingsGoal): SavingsGoal {
        val currency = goal.currencyCode
        var g = goal
        if (g.targetAmount != 0.0 && g.targetAmountMinor == 0L) {
            g = g.copy(targetAmountMinor = Money.fromDouble(g.targetAmount, currency))
        } else if (g.targetAmount == 0.0 && g.targetAmountMinor != 0L) {
            g = g.copy(targetAmount = Money.toDouble(g.targetAmountMinor, currency))
        } else if (g.targetAmount != 0.0 && g.targetAmountMinor != 0L) {
            val exp = Money.fromDouble(g.targetAmount, currency)
            if (exp != g.targetAmountMinor) g = g.copy(targetAmountMinor = exp)
        }

        if (g.currentAmount != 0.0 && g.currentAmountMinor == 0L) {
            g = g.copy(currentAmountMinor = Money.fromDouble(g.currentAmount, currency))
        } else if (g.currentAmount == 0.0 && g.currentAmountMinor != 0L) {
            g = g.copy(currentAmount = Money.toDouble(g.currentAmountMinor, currency))
        } else if (g.currentAmount != 0.0 && g.currentAmountMinor != 0L) {
            val exp = Money.fromDouble(g.currentAmount, currency)
            if (exp != g.currentAmountMinor) g = g.copy(currentAmountMinor = exp)
        }

        if (g.contributionAmount != 0.0 && g.contributionAmountMinor == 0L) {
            g = g.copy(contributionAmountMinor = Money.fromDouble(g.contributionAmount, currency))
        } else if (g.contributionAmount == 0.0 && g.contributionAmountMinor != 0L) {
            g = g.copy(contributionAmount = Money.toDouble(g.contributionAmountMinor, currency))
        } else if (g.contributionAmount != 0.0 && g.contributionAmountMinor != 0L) {
            val exp = Money.fromDouble(g.contributionAmount, currency)
            if (exp != g.contributionAmountMinor) g = g.copy(contributionAmountMinor = exp)
        }
        return g
    }

    suspend fun insertReminder(reminder: ReminderEntity) = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = dao.deleteReminder(reminder)
    suspend fun getReminderById(id: Long) = dao.getReminderById(id)

    suspend fun clearAllData() = dao.clearAllData()

    suspend fun restoreAllData(
        expenses: List<Expense>,
        accounts: List<Account>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        reminders: List<ReminderEntity>
    ) {
        dao.clearAllData()
        accounts.forEach { insertAccount(it) }
        expenses.forEach { insertExpense(it) }
        budgets.forEach { insertBudget(it) }
        goals.forEach { insertSavingsGoal(it) }
        reminders.forEach { insertReminder(it) }
    }
}
