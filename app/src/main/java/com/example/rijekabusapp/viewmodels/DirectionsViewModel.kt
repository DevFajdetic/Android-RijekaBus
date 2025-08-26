package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.response.DirectionsResponse
import kotlinx.coroutines.launch

class DirectionsViewModel(application: Application) : AndroidViewModel(application) {
    val directionsLiveData = MutableLiveData<DirectionsResponse>()
    private val _favoriteRoutes = MutableLiveData<ArrayList<FavoriteRoute>>()
    val favoriteRoutes: LiveData<ArrayList<FavoriteRoute>> = _favoriteRoutes
    private val tag = "DirectionsViewModel"

    val apiKey = "INSERT_KEY_HERE"

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getDirections(
        destination: String,
        origin: String,
        language: String,
        mode: String,
        units: String,
    ) {
        viewModelScope.launch {
            try {
                val response =
                    Network().getDirectionsService().getDirection(
                        destination,
                        origin,
                        apiKey,
                        language,
                        mode,
                        units,
                    )
                directionsLiveData.value = response
            } catch (e: Exception) {
                Log.e(tag, "Error getting directions: ${e.message}", e)
                // Create an empty response to avoid null issues
                directionsLiveData.value =
                    Network().getDirectionsService().getDirection(
                        destination,
                        origin,
                        apiKey,
                        language,
                        mode,
                        units,
                    )
            }
        }
    }

    fun saveRouteInformation(route: FavoriteRoute) {
        viewModelScope.launch {
            try {
                repository.insertFavoriteRoute(route)
            } catch (e: Exception) {
                Log.e(tag, "Error saving route information: ${e.message}", e)
            }
        }
    }

    fun getFavoriteRoutes() {
        try {
            val routes = ArrayList<FavoriteRoute>()
            repository.getFavoriteRoutes().forEach {
                routes.add(it)
            }
            _favoriteRoutes.value = routes
        } catch (e: Exception) {
            Log.e(tag, "Error getting favorite routes: ${e.message}", e)
            _favoriteRoutes.value = ArrayList()
        }
    }
}
