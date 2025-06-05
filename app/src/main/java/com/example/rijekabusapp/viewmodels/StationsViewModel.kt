package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.Station
import kotlinx.coroutines.launch

class StationsViewModel(application: Application) : AndroidViewModel(application) {
    val stationsList = MutableLiveData<ArrayList<Station>>()
    private val _favoriteStations = MutableLiveData<ArrayList<Station>>()
    val favoriteStations: LiveData<ArrayList<Station>> = _favoriteStations
    private val tag = "StationsViewModel"

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getStationsList() {
        viewModelScope.launch {
            try {
                stationsList.value = Network().getBusService().getAllStations()
            } catch (e: Exception) {
                Log.e(tag, "Error getting stations list: ${e.message}", e)
                // Return empty list instead of crashing
                stationsList.value = ArrayList()
            }
        }
    }

    fun insertFavoriteStation(station: Station) {
        try {
            repository.insertFavoriteStation(
                station.convertToFavoriteStation(repository.countFavoriteStations() + 1),
            )
        } catch (e: Exception) {
            Log.e(tag, "Error inserting favorite station: ${e.message}", e)
        }
    }

    fun deleteFavoriteStation(station: Station) {
        try {
            repository.deleteFavoriteStation(station.convertToFavoriteStation(null))
        } catch (e: Exception) {
            Log.e(tag, "Error deleting favorite station: ${e.message}", e)
        }
    }

    fun getFavoriteStations() {
        try {
            val stations = ArrayList<Station>()
            repository.getFavoriteStations().forEach {
                stations.add(it.convertToStation())
            }
            _favoriteStations.value = stations
        } catch (e: Exception) {
            Log.e(tag, "Error getting favorite stations: ${e.message}", e)
            _favoriteStations.value = ArrayList()
        }
    }
}
