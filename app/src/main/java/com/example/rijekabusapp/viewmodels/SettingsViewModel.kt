package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AutotrolejRepository
    private val tag = "SettingsViewModel"

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun clearFavoritesList() {
        try {
            repository.deleteFavoriteLines()
            repository.deleteFavoriteStations()
        } catch (e: Exception) {
            Log.e(tag, "Error clearing favorites list: ${e.message}", e)
        }
    }
}
