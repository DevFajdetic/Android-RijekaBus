package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.database.models.FavoriteStation
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.network.models.StationImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    val favoriteLines = MutableLiveData<ArrayList<FavoriteLine>>()
    val favoriteStations = MutableLiveData<ArrayList<FavoriteStation>>()
    val stationImages = MutableLiveData<ArrayList<ArrayList<StationImage>>>()
    private val tag = "FavoritesViewModel"

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getFavoriteStationsAndImages() {
        viewModelScope.launch {
            try {
                val stations = ArrayList<FavoriteStation>()
                val sortedStations = repository.getFavoriteStationsAsync().sortedBy { it.position }
                sortedStations.forEach {
                    stations.add(it)
                }

                val asyncTasks =
                    stations.map { station ->
                        async {
                            try {
                                ArrayList(Network().getMyApiService().getStationImages(station.id, 0))
                            } catch (e: Exception) {
                                Log.e(tag, "Error getting station images for station ${station.id}: ${e.message}", e)
                                arrayListOf(StationImage(0, "", "", null))
                            }
                        }
                    }
                val response = asyncTasks.awaitAll()

                favoriteStations.value = stations
                if (response.isNotEmpty()) {
                    Log.d("novo2", "NIJE PRAZNO")
                    stationImages.value = response as ArrayList<ArrayList<StationImage>>
                }
            } catch (e: Exception) {
                Log.e(tag, "Error getting favorite stations and images: ${e.message}", e)
                favoriteStations.value = ArrayList()
                stationImages.value = ArrayList()
            }
        }
    }

    fun deleteFavoriteStation(station: Station) {
        try {
            repository.deleteFavoriteStation(station.convertToFavoriteStation(null))
        } catch (e: Exception) {
            Log.e(tag, "Error deleting favorite station: ${e.message}", e)
        }
    }

    fun updateFavoriteStation(
        station: Station,
        position: Int,
    ) {
        viewModelScope.launch {
            try {
                repository.updateFavoriteStation(station.convertToFavoriteStation(position))
            } catch (e: Exception) {
                Log.e(tag, "Error updating favorite station: ${e.message}", e)
            }
        }
    }

    fun getFavoriteLines() {
        viewModelScope.launch {
            try {
                val lines = ArrayList<FavoriteLine>()
                val sortedLines = repository.getFavoriteLines().sortedBy { it.position }
                sortedLines.forEach {
                    lines.add(it)
                }
                favoriteLines.value = lines
            } catch (e: Exception) {
                Log.e(tag, "Error getting favorite lines: ${e.message}", e)
                favoriteLines.value = ArrayList()
            }
        }
    }

    fun deleteFavoriteLine(line: Line) {
        try {
            repository.deleteFavoriteLine(line.convertToFavoriteLine(null))
        } catch (e: Exception) {
            Log.e(tag, "Error deleting favorite line: ${e.message}", e)
        }
    }

    fun favoriteLinesUpdate() {
        try {
            favoriteLines.value?.forEachIndexed { index, line ->
                line.position = index
                repository.updateFavoriteLine(line)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating favorite lines: ${e.message}", e)
        }
    }

    fun favoriteStationsUpdate() {
        try {
            favoriteStations.value?.forEachIndexed { index, station ->
                station.position = index
                repository.updateFavoriteStation(station)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating favorite stations: ${e.message}", e)
        }
    }
}
