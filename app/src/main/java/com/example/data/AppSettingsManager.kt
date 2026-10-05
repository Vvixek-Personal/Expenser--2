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
        var rawPin = prefs.getString("app_pin", null)
        if (!rawPin.isNullOrBlank() && !rawPin.startsWith("v2:") && !rawPin.startsWith("v3:")) {
            val upgraded = PinSecurityUtils.hashPin(rawPin)
            prefs.edit().putString("app_pin", upgraded).commit()
            rawPin = upgraded
        }

        return AppSettingsState(
            hasAiConsent = prefs.getBoolean("ai_consent_enabled", false),
            aiConsentEnabled = prefs.getBoolean("ai_consent_enabled", false),
            appPin = rawPin,
            isAppLocked = !rawPin.isNullOrBlank(),
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
            isAnimationEnabled = prefs.getBoolean("animations_enabled", true),
            aiDailyRequestCount = prefs.getInt("ai_daily_request_count", 0),
            aiLastRequestDate = prefs.getString("ai_last_request_date", "") ?: ""
        )
    }

    fun reloadStateAfterWipe() {
        _state.value = loadInitialState()
    }

    fun dispatch(intent: AppSettingsIntent) {
        _state.value = when (intent) {
            is AppSettingsIntent.SetAiConsent -> {
                prefs.edit().putBoolean("ai_consent_enabled", intent.granted).apply()
                _state.value.copy(aiConsentEnabled = intent.granted, hasAiConsent = intent.granted)
            }
            is AppSettingsIntent.SetPin -> {
                if (intent.pin.isNullOrBlank()) {
                    prefs.edit().remove("app_pin").commit()
                    _state.value.copy(appPin = null, isAppLocked = false)
                } else {
                    val hashed = if (intent.pin.startsWith("v2:") || intent.pin.startsWith("v3:")) {
                        intent.pin
                    } else {
                        PinSecurityUtils.hashPin(intent.pin)
                    }
                    prefs.edit().putString("app_pin", hashed).commit()
                    _state.value.copy(appPin = hashed, isAppLocked = true)
                }
            }
            is AppSettingsIntent.IncrementAiRequestCount -> {
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                val newCount = if (_state.value.aiLastRequestDate == todayStr) {
                    _state.value.aiDailyRequestCount + 1
                } else {
                    1
                }
                prefs.edit()
                    .putInt("ai_daily_request_count", newCount)
                    .putString("ai_last_request_date", todayStr)
                    .apply()
                _state.value.copy(aiDailyRequestCount = newCount, aiLastRequestDate = todayStr)
            }
            is AppSettingsIntent.UpdateLanguage -> {
                prefs.edit().putString("language", intent.lang).apply()
                _state.value.copy(language = intent.lang)
            }
            is AppSettingsIntent.UpdateThemeMode -> {
                prefs.edit().putString("theme_mode", intent.mode).apply()
                _state.value.copy(themeMode = intent.mode)
            }
            is AppSettingsIntent.UpdateTheme -> {
                prefs.edit().putInt("theme_index", intent.index).putFloat("custom_theme_hue", intent.hue).apply()
                _state.value.copy(themeIndex = intent.index, customThemeHue = intent.hue)
            }
            is AppSettingsIntent.UpdateCurrency -> {
                prefs.edit().putString("currency_code", intent.code)
                    .putString("currency_symbol", intent.symbol)
                    .putString("currency_name", intent.name).apply()
                _state.value.copy(currencyCode = intent.code, currencySymbol = intent.symbol, currencyName = intent.name)
            }
            is AppSettingsIntent.UpdateMonthlyBudget -> {
                prefs.edit().putFloat("monthly_budget", intent.budget.toFloat()).apply()
                _state.value.copy(monthlyBudget = intent.budget)
            }
            is AppSettingsIntent.SetAutoLockDuration -> {
                prefs.edit().putString("auto_lock_duration", intent.duration).apply()
                _state.value.copy(autoLockDuration = intent.duration)
            }
            is AppSettingsIntent.UpdateBillReminderTiming -> {
                prefs.edit().putString("bill_reminder_timing", intent.days).apply()
                _state.value.copy(billReminderTiming = intent.days)
            }
            is AppSettingsIntent.UpdateAnimationEnabled -> {
                prefs.edit().putBoolean("animations_enabled", intent.enabled).apply()
                _state.value.copy(isAnimationEnabled = intent.enabled)
            }
            is AppSettingsIntent.SetAnimationsEnabled -> {
                prefs.edit().putBoolean("animations_enabled", intent.enabled).apply()
                _state.value.copy(isAnimationEnabled = intent.enabled)
            }
            is AppSettingsIntent.SetUserName -> {
                prefs.edit().putString("user_name", intent.name).apply()
                _state.value.copy(userName = intent.name)
            }
            is AppSettingsIntent.SetUserDob -> {
                prefs.edit().putString("user_dob", intent.dob).apply()
                _state.value.copy(userDob = intent.dob)
            }
            is AppSettingsIntent.ReloadStateAfterWipe -> {
                loadInitialState()
            }
            else -> _state.value
        }
    }
}
