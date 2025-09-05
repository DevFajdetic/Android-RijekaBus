package com.example.rijekabusapp.fragments

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.rijekabusapp.MainActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.FragmentSettingsBinding
import com.example.rijekabusapp.helpers.PREF_HOUR
import com.example.rijekabusapp.helpers.PREF_METRIC
import com.example.rijekabusapp.helpers.PREF_SELECTED_DATE
import com.example.rijekabusapp.helpers.PREF_SELECTED_LANGUAGE
import com.example.rijekabusapp.helpers.PREF_THEME_MODE
import com.example.rijekabusapp.helpers.getBoolFromPreferences
import com.example.rijekabusapp.helpers.getStringFromPreferences
import com.example.rijekabusapp.helpers.savePreferenceBool
import com.example.rijekabusapp.helpers.savePreferenceString
import com.example.rijekabusapp.helpers.isDarkThemeEnabled
import com.example.rijekabusapp.helpers.showCustomSnackbar
import com.example.rijekabusapp.viewmodels.SettingsViewModel
import java.util.Locale

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.ivBack.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_settingsFragment_to_exploreFragment)
        }

        // Check if we need to sync theme with system settings
        syncThemeWithSystemIfNeeded()

        // SET DEFAULT VALUES
        setDefaultValues()

        // LANGUAGES
        setupLanguageSettings()

        // DATES
        setupDateSettings()

        // THEME
        setupThemeSettings()

        // UNITS
        setupUnitSettings()

        // CLOCK
        setupClockSettings()

        // Clear button
        binding.clearButton.setOnClickListener {
            showCustomDialog(getString(R.string.clear_favorites_list))
        }

        return binding.root
    }

    private fun setupLanguageSettings() {
        binding.actvLanguages.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.drop_down_item,
                resources.getStringArray(R.array.SpinnerLanguages),
            ),
        )
        
        // Set current value from preferences
        val currentLanguage = getStringFromPreferences(PREF_SELECTED_LANGUAGE, "English", requireContext())
        binding.actvLanguages.setText(currentLanguage, false)
        
        binding.actvLanguages.setOnItemClickListener { parent, _, position, _ ->
            val selectedLanguage = parent.getItemAtPosition(position).toString()
            if (selectedLanguage != currentLanguage) {
                applyLanguage(selectedLanguage)
            }
        }
    }
    
    private fun setupDateSettings() {
        binding.actvDateFormat.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.drop_down_item,
                resources.getStringArray(R.array.SpinnerDateFormats),
            ),
        )
        
        // Set current value from preferences
        val currentDateFormat = getStringFromPreferences(PREF_SELECTED_DATE, "dd/MM/yyyy", requireContext())
        binding.actvDateFormat.setText(currentDateFormat, false)
        
        binding.actvDateFormat.setOnItemClickListener { parent, _, position, _ ->
            val selectedDate = parent.getItemAtPosition(position).toString()
            savePreferenceString(PREF_SELECTED_DATE, selectedDate, requireContext())
            showCustomSnackbar(requireContext(), binding.root, getString(R.string.settings_saved))
        }
    }
    
    private fun setupThemeSettings() {
        val isDarkMode = getBoolFromPreferences(PREF_THEME_MODE, false, requireContext())
        binding.switchTheme.isChecked = isDarkMode
        
        binding.switchTheme.setOnCheckedChangeListener { _, checked ->
            savePreferenceBool(PREF_THEME_MODE, checked, requireContext())
            applyTheme(checked)
        }
    }
    
    private fun setupUnitSettings() {
        val isMetric = getBoolFromPreferences(PREF_METRIC, true, requireContext())
        binding.metric.isChecked = isMetric
        binding.imperial.isChecked = !isMetric
        
        binding.metric.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                savePreferenceBool(PREF_METRIC, true, requireContext())
                binding.imperial.isChecked = false
                showCustomSnackbar(requireContext(), binding.root, getString(R.string.settings_saved))
            }
        }
        
        binding.imperial.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                savePreferenceBool(PREF_METRIC, false, requireContext())
                binding.metric.isChecked = false
                showCustomSnackbar(requireContext(), binding.root, getString(R.string.settings_saved))
            }
        }
    }
    
    private fun setupClockSettings() {
        val is24Hour = getBoolFromPreferences(PREF_HOUR, true, requireContext())
        binding.hours24.isChecked = is24Hour
        binding.hours12.isChecked = !is24Hour
        
        binding.hours24.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                savePreferenceBool(PREF_HOUR, true, requireContext())
                binding.hours12.isChecked = false
                showCustomSnackbar(requireContext(), binding.root, getString(R.string.settings_saved))
            }
        }
        
        binding.hours12.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                savePreferenceBool(PREF_HOUR, false, requireContext())
                binding.hours24.isChecked = false
                showCustomSnackbar(requireContext(), binding.root, getString(R.string.settings_saved))
            }
        }
    }

    private fun syncThemeWithSystemIfNeeded() {
        // Get the current system theme state
        val systemDarkMode = requireContext().isDarkThemeEnabled()
        
        // Get the saved theme preference
        val savedThemePreference = getBoolFromPreferences(PREF_THEME_MODE, false, requireContext())
        
        // If they don't match, update the preference to match the system
        if (systemDarkMode != savedThemePreference) {
            savePreferenceBool(PREF_THEME_MODE, systemDarkMode, requireContext())
        }
    }

    private fun setDefaultValues() {
        val prefLang = getStringFromPreferences(PREF_SELECTED_LANGUAGE, "English", requireContext())
        binding.actvLanguages.setText(prefLang)

        val prefDate = getStringFromPreferences(PREF_SELECTED_DATE, "dd/MM/yyyy", requireContext())
        binding.actvDateFormat.setText(prefDate)

        val prefTheme = getBoolFromPreferences(PREF_THEME_MODE, false, requireContext())
        binding.switchTheme.isChecked = prefTheme

        val metricPref = getBoolFromPreferences(PREF_METRIC, true, requireContext())
        binding.metric.isChecked = metricPref
        binding.imperial.isChecked = !metricPref

        val hourPref = getBoolFromPreferences(PREF_HOUR, true, requireContext())
        binding.hours24.isChecked = hourPref
        binding.hours12.isChecked = !hourPref
    }

    private fun applyTheme(isDarkMode: Boolean) {
        val mode =
            if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

        // Apply theme to the entire app
        AppCompatDelegate.setDefaultNightMode(mode)

        // Force recreate the activity to apply theme changes
        requireActivity().recreate()
    }

    private fun showCustomDialog(title: String) {
        val dialog =
            Dialog(requireContext()).apply {
                this.requestWindowFeature(Window.FEATURE_NO_TITLE)
                this.setCancelable(true)
                this.setContentView(R.layout.custom_dialog)
                this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val btnOk = dialog.findViewById<Button>(R.id.btnDialogOk)
        val btnCancel = dialog.findViewById<Button>(R.id.btnDialogCancel)

        tvTitle.text = title
        btnOk.text = getString(R.string.clear)
        btnCancel.text = getString(R.string.cancel)

        btnOk.setOnClickListener {
            viewModel.clearFavoritesList()
            dialog.dismiss()
            showCustomSnackbar(requireContext(), binding.root, getString(R.string.favorites_cleared))
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyLanguage(language: String) {
        // Save the selected language to preferences
        savePreferenceString(PREF_SELECTED_LANGUAGE, language, requireContext())

        // Update the configuration with the selected language
        var locale = Locale(language)
        when (language) {
            "English" -> locale = Locale("en")
            "Croatian" -> locale = Locale("hr")
            else -> locale = Locale.getDefault()
        }
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Restart the app with the new language
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }
}
