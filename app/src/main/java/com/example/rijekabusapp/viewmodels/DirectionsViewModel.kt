package com.example.rijekabusapp.viewmodels

import android.app.Application
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

    val apiKey = "AIzaSyCYRCaIRT_p72odDx2jgj38Ls4DF-h8ODI"

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
            val response = Network().getDirectionsService().getDirection(
                destination, origin, apiKey, language, mode, units
            )
            directionsLiveData.value = response
        }
    }

    fun saveRouteInformation(route: FavoriteRoute) {
        viewModelScope.launch {
            repository.insertFavoriteRoute(route)
        }
    }

    fun getFavoriteRoutes() {
        val routes = ArrayList<FavoriteRoute>()
        repository.getFavoriteRoutes().forEach {
            routes.add(it)
        }
        _favoriteRoutes.value = routes
    }
}
