package com.example.rijekabusapp.helpers

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Helper class to manage language settings across the app
 */
object LanguageHelper {
    
    /**
     * Apply the saved language preference to the given context
     */
    fun applyLanguage(context: Context) {
        val savedLanguage = getStringFromPreferences(PREF_SELECTED_LANGUAGE, "English", context)
        setAppLanguage(context, savedLanguage)
    }
    
    /**
     * Set the app language based on the selected language
     */
    fun setAppLanguage(context: Context, language: String) {
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
    
    /**
     * Create a configuration context with the saved language
     */
    fun createConfigurationContext(context: Context): Context {
        val savedLanguage = getStringFromPreferences(PREF_SELECTED_LANGUAGE, "English", context)
        val locale = when (savedLanguage) {
            "Croatian" -> Locale("hr")
            "English" -> Locale("en")
            else -> Locale("en")
        }
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
} 