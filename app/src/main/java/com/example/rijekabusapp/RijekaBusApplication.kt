package com.example.rijekabusapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.helpers.PREF_SELECTED_LANGUAGE
import com.example.rijekabusapp.helpers.PREF_THEME_MODE
import com.example.rijekabusapp.helpers.getBoolFromPreferences
import com.example.rijekabusapp.helpers.getStringFromPreferences
import com.example.rijekabusapp.firebase.FirebaseAuthHelper
import com.example.rijekabusapp.firebase.FirebaseRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import java.util.Locale

class RijekaBusApplication : Application() {
    lateinit var firebaseRepository: FirebaseRepository
    lateinit var firebaseAuthHelper: FirebaseAuthHelper

    override fun onCreate() {
        super.onCreate()

        // Initialize language based on saved preference
        LanguageHelper.applyLanguage(this)
        
        // Initialize theme based on saved preference
        initializeTheme()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Firebase App Check with debug provider for development
        val firebaseAppCheck = FirebaseAppCheck.getInstance()


        // Use Play Integrity in production
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Initialize Firebase components
        firebaseRepository = FirebaseRepository()
        firebaseAuthHelper = FirebaseAuthHelper()
    }
    
    // Called when app is resumed and configuration might have changed
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply saved language to ensure consistency
        LanguageHelper.applyLanguage(this)
    }

    // Override attachBaseContext to set the language
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageHelper.createConfigurationContext(base))
    }

    private fun initializeTheme() {
        val isDarkMode = getBoolFromPreferences(PREF_THEME_MODE, false, this)
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
} 