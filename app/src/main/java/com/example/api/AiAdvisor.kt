package com.example.api

sealed class AiResult {
    data class Success(val text: String) : AiResult()

    sealed class Failure(val userFriendlyMessage: String) : AiResult() {
        object Offline : Failure("You appear to be offline. Please check your internet connection.")
        object RateLimited : Failure("AI Advisor is busy right now. Please wait a moment before trying again.")
        object DailyLimitReached : Failure("Daily AI request limit reached. Please try again tomorrow.")
        object ConsentRequired : Failure("AI features are disabled. Please enable AI Assistant in Settings or accept consent to continue.")
        object Timeout : Failure("The request timed out. Please try asking again.")
        object QuotaExceeded : Failure("AI service temporarily unavailable due to high demand. Please try again later.")
        data class Unknown(val detail: String = "Unable to reach AI Financial Advisor right now. Please try again later.") : Failure(detail)
    }
}

data class FinancialTotalsContext(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netBalance: Double,
    val topExpenseCategory: String,
    val categoryNames: List<String>,
    val currencySymbol: String
)

interface AiAdvisor {
    suspend fun ask(prompt: String, context: FinancialTotalsContext): AiResult
}
