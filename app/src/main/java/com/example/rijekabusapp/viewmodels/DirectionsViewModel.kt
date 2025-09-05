package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.BuildConfig
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.database.models.UserStats
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.response.DirectionsResponse
import kotlinx.coroutines.launch

class DirectionsViewModel(application: Application) : AndroidViewModel(application) {
    val directionsLiveData = MutableLiveData<DirectionsResponse?>()
    private val _favoriteRoutes = MutableLiveData<ArrayList<FavoriteRoute>>()
    val favoriteRoutes: LiveData<ArrayList<FavoriteRoute>> = _favoriteRoutes
    
    private val _userStats = MutableLiveData<UserStats>()
    val userStats: LiveData<UserStats> = _userStats
    
    private val tag = "DirectionsViewModel"

    val apiKey = BuildConfig.MAPS_API_KEY

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
                directionsLiveData.value = null
            }
        }
    }

    fun saveRouteInformation(route: FavoriteRoute) {
        viewModelScope.launch {
            try {
                repository.insertFavoriteRoute(route)
                // Update user stats after adding a new route
                updateUserStatsFromRoutes()
            } catch (e: Exception) {
                Log.e(tag, "Error saving route information: ${e.message}", e)
            }
        }
    }

    fun getFavoriteRoutes() {
        viewModelScope.launch {
            try {
                val routes = ArrayList<FavoriteRoute>()
                repository.getFavoriteRoutes().forEach {
                    routes.add(it)
                }
                _favoriteRoutes.value = routes
                
                // Update user stats after loading routes
                updateUserStatsFromRoutes()
            } catch (e: Exception) {
                Log.e(tag, "Error getting favorite routes: ${e.message}", e)
                _favoriteRoutes.value = ArrayList()
            }
        }
    }
    
    fun getUserStats(userId: String = "default_user") {
        viewModelScope.launch {
            try {
                val stats = repository.getUserStats(userId) ?: createDefaultUserStats(userId)
                _userStats.value = stats
            } catch (e: Exception) {
                Log.e(tag, "Error getting user stats: ${e.message}", e)
                _userStats.value = createDefaultUserStats(userId)
            }
        }
    }
    
    private fun createDefaultUserStats(userId: String): UserStats {
        return UserStats(
            userId = userId,
            totalDistance = 0.0,
            totalTime = 0,
            totalTrips = 0,
            experiencePoints = 0,
            level = 1
        )
    }
    
    private fun updateUserStatsFromRoutes() {
        viewModelScope.launch {
            try {
                val routes = repository.getFavoriteRoutes()
                if (routes.isEmpty()) return@launch
                
                var totalDistance = 0.0
                var totalTime = 0
                val totalTrips = routes.size
                
                // Calculate total distance and time from routes
                routes.forEach { route ->
                    // Extract distance value (e.g., "5 km" -> 5.0)
                    val distanceStr = route.distance.split(" ")[0]
                    try {
                        val distance = distanceStr.toDouble()
                        totalDistance += distance
                    } catch (e: NumberFormatException) {
                        // Handle parsing error
                    }
                    
                    // Extract time value (e.g., "15 mins" -> 15)
                    val timeStr = route.time.split(" ")[0]
                    try {
                        val time = timeStr.toInt()
                        totalTime += time
                    } catch (e: NumberFormatException) {
                        // Handle parsing error
                    }
                }
                
                // Calculate experience points and level
                val experiencePoints = (totalDistance * 10 + totalTrips * 20).toInt()
                val level = (experiencePoints / 100) + 1
                
                // Update or create user stats
                val userId = "default_user" // Use a default user ID or get from preferences
                val existingStats = repository.getUserStats(userId)
                
                val updatedStats = UserStats(
                    userId = userId,
                    totalDistance = totalDistance,
                    totalTime = totalTime,
                    totalTrips = totalTrips,
                    experiencePoints = experiencePoints,
                    level = level
                )
                
                repository.insertUserStats(updatedStats)
                _userStats.value = updatedStats
            } catch (e: Exception) {
                Log.e(tag, "Error updating user stats: ${e.message}", e)
            }
        }
    }
}
