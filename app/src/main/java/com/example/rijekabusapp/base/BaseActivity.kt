package com.example.rijekabusapp.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rijekabusapp.helpers.PREF_SELECTED_LANGUAGE
import com.example.rijekabusapp.helpers.getStringFromPreferences
import java.util.Locale

/**
 * Base activity that handles language settings consistently across the app.
 * All activities should extend this class to ensure proper language handling.
 */
open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language settings before calling super.onCreate
        // to ensure proper resource loading
        applyLanguageFromPreferences()
        super.onCreate(savedInstanceState)
    }
    
    override fun onResume() {
        super.onResume()
        // Reapply language settings when activity is resumed
        applyLanguageFromPreferences()
    }
    
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = getStringFromPreferences(PREF_SELECTED_LANGUAGE, "English", newBase)
        val locale = when (savedLanguage) {
            "Croatian" -> Locale("hr")
            "English" -> Locale("en")
            else -> Locale("en")
        }
        
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    
    protected fun applyLanguageFromPreferences() {
        val savedLanguage = getStringFromPreferences(PREF_SELECTED_LANGUAGE, "English", this)
        setAppLanguage(this, savedLanguage)
    }
    
    private fun setAppLanguage(context: Context, language: String) {
        val locale = when (language) {
            "Croatian" -> Locale("hr")
            "English" -> Locale("en")
            else -> Locale("en") // Default to English
        }
        
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
} 