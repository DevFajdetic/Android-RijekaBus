package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.network.models.StationImage
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class StationDetailsViewModel(application: Application) : AndroidViewModel(application) {
    val imagesList = MutableLiveData<ArrayList<StationImage>>()
    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun isStationFavorite(id: Int): Boolean {
        return repository.isStationFavorite(id)
    }

    fun insertFavoriteStation(station: Station) {
        repository.insertFavoriteStation(
            station.convertToFavoriteStation(repository.countFavoriteStations() + 1),
        )
    }

    fun deleteFavoriteStation(station: Station) {
        repository.deleteFavoriteStation(station.convertToFavoriteStation(null))
    }

    fun getStationImages(id: Int) {
        viewModelScope.launch {
            try {
                val tmp = ArrayList(Network().getMyApiService().getStationImages(id))
                if (tmp.size > 0) {
                    imagesList.value = tmp
                }
            } catch (e: Exception) {
                Log.d("novo", e.toString())
            }
        }
    }

    fun addImageForStation(requestBody: RequestBody) {
        viewModelScope.launch {
            Network().getMyApiService().addImageForStation(requestBody)
        }
    }

    fun deleteStationImage(imageId: Int) {
        viewModelScope.launch {
            Network().getMyApiService().deleteImage(imageId.toString())
        }
    }

    fun deleteAllStationImages(imageList: ArrayList<StationImage>) {
        viewModelScope.launch {
            imageList.forEach {
                Network().getMyApiService().deleteImage(it.id!!.toString())
            }
        }
    }
}
