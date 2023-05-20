package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    val favoriteLines = MutableLiveData<ArrayList<Line>>()
    val favoriteStations = MutableLiveData<ArrayList<Station>>()

    // val stationImages = MutableLiveData<ArrayList<ArrayList<StationImage>>>()

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getFavoriteStationsAndImages() {
        viewModelScope.launch {
            val stations = ArrayList<Station>()
            repository.getFavoriteStationsAsync().forEach {
                stations.add(it.convertToStation())
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
        repository.deleteFavoriteStation(station.convertToFavoriteStation())
    }

    fun getFavoriteLines() {
        viewModelScope.launch {
            val lines = ArrayList<Line>()
            repository.getFavoriteLines().forEach {
                lines.add(it.convertToLine())
            }
            favoriteLines.value = lines
        }
    }

    fun deleteFavoriteLine(line: Line) {
        repository.deleteFavoriteLine(line.convertToFavoriteLine())
    }
}
