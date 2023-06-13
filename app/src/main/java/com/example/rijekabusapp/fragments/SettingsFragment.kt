package com.example.rijekabusapp.fragments

import android.app.Dialog
import android.content.Context
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
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.rijekabusapp.MainActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.FragmentSettingsBinding
import com.example.rijekabusapp.viewmodels.SettingsViewModel
import java.util.*

private const val PREF_SELECTED_LANGUAGE = "selected_language"
private const val PREF_THEME_MODE = "theme_mode"

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.actvLanguages.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.drop_down_item,
                resources.getStringArray(R.array.SpinnerLanguages)
            )
        )

        binding.ivBack.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_settingsFragment_to_exploreFragment)
        }

        binding.actvDateFormat.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.drop_down_item,
                resources.getStringArray(R.array.SpinnerDateFormats)
            )
        )

        binding.switchTheme.setOnCheckedChangeListener { compoundButton: CompoundButton,
            checked: Boolean ->
            if (checked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.clearButton.setOnClickListener {
            showCustomDialog(getString(R.string.clear_favorites_list))
        }

        binding.actvLanguages.setOnItemClickListener { parent, view, position, id ->
            val selectedLanguage = parent.getItemAtPosition(position).toString()
            applyLanguage(selectedLanguage)
        }

        return binding.root
    }

    private fun showCustomDialog(title: String) {
        val dialog = Dialog(requireContext()).apply {
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
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyLanguage(language: String) {
        savePreference(PREF_SELECTED_LANGUAGE, language)

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

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun savePreference(key: String, value: String) {
        val sharedPreferences = requireContext()
            .getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
}
