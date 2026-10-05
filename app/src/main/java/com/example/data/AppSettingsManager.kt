package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettingsState(
    val hasAiConsent: Boolean = false,
    val appPin: String? = null,
    val isAnimationEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
    val themeIndex: Int = 0,
    val language: String = "English",
    val userName: String = "",
    val userDob: String = "",
    val dailyStreak: Int = 0,
    val lastStreakDate: Long = 0L,
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val currencyName: String = "Indian Rupee",
    val statsCurrencyCode: String = "INR",
    val statsCurrencySymbol: String = "₹",
    val statsCurrencyName: String = "Indian Rupee",
    val monthlyBudget: Double = 0.0,
    val isAppLocked: Boolean = false,
    val privacyModeEnabled: Boolean = false,
    val hideSensitiveAmounts: Boolean = false,
    val screenshotProtection: Boolean = false,
    val autoLockDuration: String = "NEVER",
    val biometricEnabled: Boolean = false,
    val pinLockoutUntil: Long = 0L,
    val customThemeHue: Float = 210f,
    val isFollowDeviceColors: Boolean = true,
    val fontFamily: String = "Default",
    val hapticsEnabled: Boolean = true,
    val highContrastMode: Boolean = false,
    val cardGlowAnimation: Boolean = true,
    val defaultPaymentMode: String = "CASH",
    val highAmountWarningEnabled: Boolean = false,
    val highAmountThreshold: Double = 10000.0,
    val billReminderTiming: String = "0",
    val billReminderPreferredTime: String = "09:00",
    val billAutoMarkPaid: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val lastBackupTimestamp: String = "Never",
    val syncMode: String = "LOCAL",
    val lastCloudSyncTimestamp: String = "Never",
    val cloudSyncStatus: String = "Not Synced",
    val aiDailyRequestCount: Int = 0,
    val aiLastRequestDate: String = "",
    val aiConsentEnabled: Boolean = false
)

sealed class AppSettingsIntent {
    data class SetAiConsent(val granted: Boolean) : AppSettingsIntent()
    data class SetPin(val pin: String?) : AppSettingsIntent()
    data class ResetPinLockout(val attempts: Int = 0) : AppSettingsIntent()
    data class SetAnimationsEnabled(val enabled: Boolean) : AppSettingsIntent()
    data class UpdateThemeMode(val mode: String) : AppSettingsIntent()
    data class UpdateTheme(val index: Int, val hue: Float) : AppSettingsIntent()
    data class UpdateLanguage(val lang: String) : AppSettingsIntent()
    data class SetUserName(val name: String) : AppSettingsIntent()
    data class SetUserDob(val dob: String) : AppSettingsIntent()
    data class UpdateCurrency(val code: String, val symbol: String, val name: String, val convertExisting: Boolean = false) : AppSettingsIntent()
    data class UpdateStatsCurrency(val code: String, val symbol: String, val name: String) : AppSettingsIntent()
    data class UpdateMonthlyBudget(val budget: Double) : AppSettingsIntent()
    data class SetScreenshotProtection(val enabled: Boolean) : AppSettingsIntent()
    data class UpdateFontFamily(val font: String) : AppSettingsIntent()
    data class UpdateHapticsEnabled(val enabled: Boolean) : AppSettingsIntent()
    data class UpdateHighContrastMode(val enabled: Boolean) : AppSettingsIntent()
    data class UpdateCardGlowAnimation(val enabled: Boolean) : AppSettingsIntent()
    data class SetAutoLockDuration(val duration: String) : AppSettingsIntent()
    data class UpdateDefaultPaymentMode(val mode: String) : AppSettingsIntent()
    data class UpdateHighAmountWarning(val enabled: Boolean, val threshold: Double) : AppSettingsIntent()
    data class UpdateBillReminderTiming(val days: String) : AppSettingsIntent()
    data class UpdateBillReminderPreferredTime(val time: String) : AppSettingsIntent()
    data class UpdateBillAutoMarkPaid(val enabled: Boolean) : AppSettingsIntent()
    data class UpdateSyncMode(val mode: String) : AppSettingsIntent()
    object IncrementAiRequestCount : AppSettingsIntent()
    object ReloadStateAfterWipe : AppSettingsIntent()
    data class UpdateAnimationEnabled(val enabled: Boolean) : AppSettingsIntent()
}

class AppSettingsManager private constructor(context: Context) {
    companion object {
        const val PREFS_NAME = "app_settings_prefs"
        @Volatile
        private var INSTANCE: AppSettingsManager? = null
        fun getInstance(context: Context): AppSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettingsManager(context).also { INSTANCE = it }
            }
        }

        fun resetInstanceForTesting() {
            INSTANCE = null
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<AppSettingsState> = _state.asStateFlow()

    private fun loadInitialState(): AppSettingsState {
        return AppSettingsState(
            language = prefs.getString("language", "English") ?: "English",
            themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM",
            themeIndex = prefs.getInt("theme_index", 0),
            customThemeHue = prefs.getFloat("custom_theme_hue", 210f),
            currencyCode = prefs.getString("currency_code", "INR") ?: "INR",
            currencySymbol = prefs.getString("currency_symbol", "₹") ?: "₹",
            currencyName = prefs.getString("currency_name", "Indian Rupee") ?: "Indian Rupee",
            monthlyBudget = prefs.getFloat("monthly_budget", 0f).toDouble(),
            autoLockDuration = prefs.getString("auto_lock_duration", "NEVER") ?: "NEVER",
            billReminderTiming = prefs.getString("bill_reminder_timing", "0") ?: "0",
            isAnimationEnabled = prefs.getBoolean("animations_enabled", true)
        )
    }

    fun reloadStateAfterWipe() {
        _state.value = loadInitialState()
    }

    fun dispatch(intent: AppSettingsIntent) {
        _state.value = when (intent) {
            is AppSettingsIntent.UpdateLanguage -> _state.value.copy(language = intent.lang)
            is AppSettingsIntent.UpdateThemeMode -> _state.value.copy(themeMode = intent.mode)
            is AppSettingsIntent.UpdateTheme -> _state.value.copy(themeIndex = intent.index, customThemeHue = intent.hue)
            is AppSettingsIntent.UpdateCurrency -> _state.value.copy(currencyCode = intent.code, currencySymbol = intent.symbol, currencyName = intent.name)
            is AppSettingsIntent.UpdateMonthlyBudget -> _state.value.copy(monthlyBudget = intent.budget)
            is AppSettingsIntent.SetAutoLockDuration -> _state.value.copy(autoLockDuration = intent.duration)
            is AppSettingsIntent.UpdateBillReminderTiming -> _state.value.copy(billReminderTiming = intent.days)
            is AppSettingsIntent.UpdateAnimationEnabled -> _state.value.copy(isAnimationEnabled = intent.enabled)
            else -> _state.value
        }
    }
}
