package com.example

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity

open class BaseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("FinanceAppPrefs", Context.MODE_PRIVATE)
        val textSizeOption = prefs.getString("text_size_option", "Normal") ?: "Normal"
        
        val fontScale = when (textSizeOption) {
            "Small" -> 0.85f
            "Normal" -> 1.0f
            "Large" -> 1.35f
            "Extra Large" -> 1.5f
            else -> 1.0f
        }

        val config = Configuration(newBase.resources.configuration)
        config.fontScale = fontScale
        val contextWithNewConfig = newBase.createConfigurationContext(config)
        
        super.attachBaseContext(contextWithNewConfig)
    }
}
