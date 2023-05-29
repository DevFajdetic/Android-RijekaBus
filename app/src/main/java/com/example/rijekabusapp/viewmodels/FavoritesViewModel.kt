package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.database.models.FavoriteStation
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    val favoriteLines = MutableLiveData<ArrayList<FavoriteLine>>()
    val favoriteStations = MutableLiveData<ArrayList<FavoriteStation>>()

    // val stationImages = MutableLiveData<ArrayList<ArrayList<StationImage>>>()

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getFavoriteStationsAndImages() {
        viewModelScope.launch {
            val stations = ArrayList<FavoriteStation>()
            val sortedStations = repository.getFavoriteStations().sortedBy { it.position }
            sortedStations.forEach {
                stations.add(it)
            }
            /*
            val asyncTasks = stations.map { station ->
                async {
                    try {
                        Network().getBusService().getPlayerImages(station.id).data
                    } catch (e: Exception) {
                        arrayListOf(StationImage(0, "", "", null))
                    }
                }
            }
            val response = asyncTasks.awaitAll()
            */

            favoriteStations.value = stations
            /*
            if (response.isNotEmpty()) {
                playerImages.value = response as ArrayList<ArrayList<PlayerImage>>
            }
            */
        }
    }

    fun deleteFavoriteStation(station: Station) {
        repository.deleteFavoriteStation(station.convertToFavoriteStation(null))
    }

    fun updateFavoriteStation(station: Station, position: Int) {
        viewModelScope.launch {
            repository.updateFavoriteStation(station.convertToFavoriteStation(position))
        }
    }

    fun getFavoriteLines() {
        viewModelScope.launch {
            val lines = ArrayList<FavoriteLine>()
            val sortedLines = repository.getFavoriteLines().sortedBy { it.position }
            sortedLines.forEach {
                lines.add(it)
            }
            favoriteLines.value = lines
        }
    }

    fun deleteFavoriteLine(line: Line) {
        repository.deleteFavoriteLine(line.convertToFavoriteLine(null))
    }

    fun favoriteLinesUpdate() {
        favoriteLines.value?.forEachIndexed { index, line ->
            line.position = index
            repository.updateFavoriteLine(line)
        }
    }

    fun favoriteStationsUpdate() {
        favoriteStations.value?.forEachIndexed() { index, station ->
            station.position = index
            repository.updateFavoriteStation(station)
        }
    }
}
