package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao, private val database: FinanceDatabase) {
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val allAccounts: Flow<List<Account>> = dao.getAllAccounts()
    val allBudgets: Flow<List<Budget>> = dao.getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoal>> = dao.getAllSavingsGoals()
    val allRecurringRules: Flow<List<RecurringRule>> = dao.getAllRecurringRules()
    val allReminders: Flow<List<ReminderEntity>> = dao.getAllReminders()

    suspend fun insertExpense(expense: Expense) = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = dao.deleteExpenseById(id)
    suspend fun deleteAllExpenses() = dao.deleteAllExpenses()

    suspend fun insertAccount(account: Account) = dao.insertAccount(account)
    suspend fun updateAccount(account: Account) = dao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)

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
        expenses.forEach { dao.insertExpense(it) }
        accounts.forEach { dao.insertAccount(it) }
        budgets.forEach { dao.insertBudget(it) }
        goals.forEach { dao.insertSavingsGoal(it) }
        reminders.forEach { dao.insertReminder(it) }
    }
}
