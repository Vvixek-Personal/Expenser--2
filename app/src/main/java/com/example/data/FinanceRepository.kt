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

        var toInsert = expense.copy(accountId = targetAccountId)
        if (toInsert.amount == 0.0 && toInsert.amountMinor != 0L) {
            toInsert = toInsert.copy(amount = Money.toDouble(toInsert.amountMinor, toInsert.currencyCode))
        } else if (toInsert.amount != 0.0 && toInsert.amountMinor == 0L) {
            toInsert = toInsert.copy(amountMinor = Money.fromDouble(toInsert.amount, toInsert.currencyCode))
        }
        return dao.insertExpense(toInsert)
    }
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = dao.deleteExpenseById(id)
    suspend fun deleteAllExpenses() = dao.deleteAllExpenses()

    suspend fun insertAccount(account: Account): Long {
        var toInsert = account
        if (toInsert.openingBalance == 0.0 && toInsert.openingBalanceMinor != 0L) {
            toInsert = toInsert.copy(openingBalance = Money.toDouble(toInsert.openingBalanceMinor, toInsert.currencyCode))
        } else if (toInsert.openingBalance != 0.0 && toInsert.openingBalanceMinor == 0L) {
            toInsert = toInsert.copy(openingBalanceMinor = Money.fromDouble(toInsert.openingBalance, toInsert.currencyCode))
        }
        if (toInsert.balance == 0.0 && toInsert.balanceMinor != 0L) {
            toInsert = toInsert.copy(balance = Money.toDouble(toInsert.balanceMinor, toInsert.currencyCode))
        } else if (toInsert.balance != 0.0 && toInsert.balanceMinor == 0L) {
            toInsert = toInsert.copy(balanceMinor = Money.fromDouble(toInsert.balance, toInsert.currencyCode))
        }
        return dao.insertAccount(toInsert)
    }
    suspend fun updateAccount(account: Account) = dao.updateAccount(account)
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

    suspend fun insertBudget(budget: Budget) = dao.insertBudget(budget)
    suspend fun updateBudget(budget: Budget) = dao.updateBudget(budget)
    suspend fun deleteBudget(budget: Budget) = dao.deleteBudget(budget)

    suspend fun insertSavingsGoal(goal: SavingsGoal) = dao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoal) = dao.updateSavingsGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    suspend fun insertRecurringRule(rule: RecurringRule) = dao.insertRecurringRule(rule)
    suspend fun updateRecurringRule(rule: RecurringRule) = dao.updateRecurringRule(rule)
    suspend fun deleteRecurringRule(rule: RecurringRule) = dao.deleteRecurringRule(rule)
    suspend fun getRecurringRuleById(id: Long) = dao.getRecurringRuleById(id)

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
