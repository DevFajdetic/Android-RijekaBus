package com.example.rijekabusapp.viewmodels

import android.app.Application
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

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getStationsList() {
        viewModelScope.launch {
            stationsList.value = Network().getBusService().getAllStations()
        }
    }

    fun insertFavoriteStation(station: Station) {
        repository.insertFavoriteStation(
            station.convertToFavoriteStation(repository.countFavoriteStations() + 1),
        )
    }

    fun deleteFavoriteStation(station: Station) {
        repository.deleteFavoriteStation(station.convertToFavoriteStation(null))
    }

    fun getFavoriteStations() {
        val stations = ArrayList<Station>()
        repository.getFavoriteStations().forEach {
            stations.add(it.convertToStation())
        }
        _favoriteStations.value = stations
    }
}
