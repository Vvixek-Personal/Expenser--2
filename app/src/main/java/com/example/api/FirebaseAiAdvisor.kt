package com.example.api

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object AiModelConfig {
    // Verified Gemini model for Firebase AI Logic
    const val DEFAULT_MODEL_NAME = "gemini-2.5-flash"
    private const val REMOTE_CONFIG_MODEL_KEY = "gemini_model_name"

    fun getModelName(): String {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val remoteModel = remoteConfig.getString(REMOTE_CONFIG_MODEL_KEY)
            if (remoteModel.isNotBlank()) remoteModel else DEFAULT_MODEL_NAME
        } catch (_: Throwable) {
            DEFAULT_MODEL_NAME
        }
    }
}

class FirebaseAiAdvisor : AiAdvisor {

    companion object {
        private const val TAG = "AiAdvisor"
        const val MAX_PROMPT_LENGTH = 1000
        const val REQUEST_TIMEOUT_MS = 30_000L
    }

    override suspend fun ask(prompt: String, context: FinancialTotalsContext): AiResult = withContext(Dispatchers.IO) {
        val trimmed = prompt.trim()
        if (trimmed.length > MAX_PROMPT_LENGTH) {
            return@withContext AiResult.Failure.Unknown(
                "Your question exceeds the maximum length of $MAX_PROMPT_LENGTH characters. Please shorten it."
            )
        }

        try {
            val modelName = AiModelConfig.getModelName()
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = modelName,
                generationConfig = generationConfig {
                    temperature = 0.7f
                },
                systemInstruction = content {
                    text(
                        "You are a helpful, professional personal financial advisor in an expense tracking application. " +
                        "Provide practical, actionable money-management advice based solely on the user's aggregated financial totals. " +
                        "Keep answers structured, concise, and easy to read with bullet points. " +
                        "Never ask the user for sensitive credentials, passwords, or bank account numbers."
                    )
                }
            )

            val contextSummary = buildString {
                append("Current Financial Context (Monthly Aggregates):\n")
                append("- Total Income: ${context.currencySymbol}${String.format("%.2f", context.totalIncome)}\n")
                append("- Total Expenses: ${context.currencySymbol}${String.format("%.2f", context.totalExpenses)}\n")
                append("- Net Retained Balance: ${context.currencySymbol}${String.format("%.2f", context.netBalance)}\n")
                append("- Top Expense Category: ${context.topExpenseCategory}\n")
                if (context.categoryNames.isNotEmpty()) {
                    append("- Active Categories: ${context.categoryNames.joinToString(", ")}\n")
                }
            }

            val fullPrompt = "$contextSummary\n\nUser Question:\n$trimmed"

            val response = withTimeoutOrNull(REQUEST_TIMEOUT_MS) {
                generativeModel.generateContent(fullPrompt)
            } ?: return@withContext AiResult.Failure.Timeout

            val output = response.text
            if (output.isNullOrBlank()) {
                AiResult.Failure.Unknown("No response received from AI advisor. Please try again.")
            } else {
                AiResult.Success(output.trim())
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network host unreachable during AI query")
            AiResult.Failure.Offline
        } catch (e: ConnectException) {
            Log.w(TAG, "Connection failure during AI query")
            AiResult.Failure.Offline
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Network timeout during AI query")
            AiResult.Failure.Timeout
        } catch (e: Exception) {
            val message = e.message ?: ""
            Log.w(TAG, "AI Advisor request error: ${e.javaClass.simpleName}")
            when {
                message.contains("RESOURCE_EXHAUSTED", ignoreCase = true) || message.contains("429") -> {
                    AiResult.Failure.QuotaExceeded
                }
                message.contains("rate", ignoreCase = true) || message.contains("too many", ignoreCase = true) -> {
                    AiResult.Failure.RateLimited
                }
                message.contains("offline", ignoreCase = true) || message.contains("network", ignoreCase = true) -> {
                    AiResult.Failure.Offline
                }
                message.contains("AppCheck", ignoreCase = true) || message.contains("permission", ignoreCase = true) -> {
                    AiResult.Failure.Unknown("Security check failed. Please ensure the app is verified and try again.")
                }
                else -> {
                    AiResult.Failure.Unknown()
                }
            }
        }
    }
}
