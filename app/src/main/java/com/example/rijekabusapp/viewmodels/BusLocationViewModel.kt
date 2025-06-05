package com.example.rijekabusapp.viewmodels
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.BusLocation
import com.example.rijekabusapp.network.response.Re
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.ConcurrentHashMap

class BusLocationViewModel(application: Application) : AndroidViewModel(application) {
    // Legacy LiveData for existing code
    val busLocationsLiveData = MutableLiveData<List<BusLocation>>()
    val busLocationsLiveData2 = MutableLiveData<List<Re>>()
    private val tag = "BusLocationViewModel"

    // WebSocket connection
    private var webSocket: WebSocket? = null

    // Store bus locations in a thread-safe map (key: gbr, value: Re)
    private val busLocationsMap = ConcurrentHashMap<Int, Re>()

    // LiveData to observe the map values
    private val _busLocationsMapLiveData = MutableLiveData<Map<Int, Re>>()
    val busLocationsMapLiveData: LiveData<Map<Int, Re>> = _busLocationsMapLiveData

    // Flag to track if WebSocket is connected
    private val _isWebSocketConnected = MutableLiveData<Boolean>(false)
    val isWebSocketConnected: LiveData<Boolean> = _isWebSocketConnected

    // Flag to track if initial data has been loaded
    private var initialDataLoaded = false

    // Legacy method for compatibility
    fun getBusLocations() {
        if (initialDataLoaded && !busLocationsMap.isEmpty()) {
            // Use stored data from WebSocket if available
            val busList = busLocationsMap.values.toList()
            busLocationsLiveData2.value = busList

            // Convert Re objects to BusLocation objects for legacy code
            viewModelScope.launch {
                try {
                    // If we need the old format data, still fetch it
                    busLocationsLiveData.value = Network().getAutotrolejService().getBusesLocations()
                } catch (e: Exception) {
                    Log.e(tag, "Error getting bus locations: ${e.message}", e)
                    busLocationsLiveData.value = emptyList()
                }
            }
        } else {
            // Fall back to API call if WebSocket data isn't available
            getBusLocations2()
            viewModelScope.launch {
                try {
                    busLocationsLiveData.value = Network().getAutotrolejService().getBusesLocations()
                } catch (e: Exception) {
                    Log.e(tag, "Error getting bus locations: ${e.message}", e)
                    busLocationsLiveData.value = emptyList()
                }
            }
        }
    }

    fun connectToWebSocket() {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url("ws://api.autotrolej.hr/api/Hub/location").build()

            webSocket =
                client.newWebSocket(
                    request,
                    object : WebSocketListener() {
                        override fun onMessage(
                            webSocket: WebSocket,
                            text: String,
                        ) {
                            try {
                                val busLocation = parseBusLocationMessage(text)
                                if (busLocation.isNotEmpty()) {
                                    val re = busLocation[0]
                                    // Store in map
                                    busLocationsMap[re.gbr] = re

                                    // Update LiveData with current map
                                    _busLocationsMapLiveData.postValue(busLocationsMap.toMap())

                                    // Also update legacy LiveData
                                    busLocationsLiveData2.postValue(busLocationsMap.values.toList())
                                }
                            } catch (e: Exception) {
                                Log.e(tag, "Error parsing WebSocket message: ${e.message}", e)
                            }
                        }

                        override fun onOpen(
                            webSocket: WebSocket,
                            response: okhttp3.Response,
                        ) {
                            _isWebSocketConnected.postValue(true)
                            Log.d(tag, "WebSocket connected")
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            bytes: ByteString,
                        ) {
                            onMessage(webSocket, bytes.utf8())
                        }

                        override fun onFailure(
                            webSocket: WebSocket,
                            t: Throwable,
                            response: okhttp3.Response?,
                        ) {
                            Log.e(tag, "WebSocket failure: ${t.message}", t)
                            _isWebSocketConnected.postValue(false)

                            // Try to reconnect after a delay
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(5000)
                                connectToWebSocket()
                            }
                        }

                        override fun onClosing(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            _isWebSocketConnected.postValue(false)
                            Log.d(tag, "WebSocket closing: $reason")
                        }

                        override fun onClosed(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            _isWebSocketConnected.postValue(false)
                            Log.d(tag, "WebSocket closed: $reason")
                        }
                    },
                )

            // Load initial data to populate the map
            if (!initialDataLoaded) {
                getBusLocations2()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error connecting to WebSocket: ${e.message}", e)
            _isWebSocketConnected.postValue(false)
        }
    }

    fun getBusLocations2() {
        viewModelScope.launch {
            try {
                val response = Network().getAutotrolejService().getBusesLocations2().res
                busLocationsLiveData2.value = response

                // Store in map
                response.forEach { re ->
                    busLocationsMap[re.gbr] = re
                }

                // Update LiveData with current map
                _busLocationsMapLiveData.value = busLocationsMap.toMap()

                initialDataLoaded = true
            } catch (e: Exception) {
                Log.e(tag, "Error getting bus locations 2: ${e.message}", e)
                busLocationsLiveData2.value = emptyList()
            }
        }
    }

    private fun parseBusLocationMessage(message: String): List<Re> {
        val locationsList = mutableListOf<Re>()
        try {
            // Split the message by semicolons and map it to the Re data class
            val data = message.split(";")
            if (data.size == 6) {
                val gbr = data[0].toIntOrNull()
                val voznjaBusId = data[1].toString()
                val voznjaId = data[2].toString()
                val lat = data[3].toDoubleOrNull()
                val lon = data[4].toDoubleOrNull()
                val busId = data[5].toString()

                if (gbr != null && lat != null && lon != null) {
                    locationsList.add(Re(gbr, lat, lon, voznjaBusId, voznjaId, busId))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing bus location message: ${e.message}", e)
        }
        return locationsList
    }

    fun disconnectWebSocket() {
        try {
            webSocket?.close(1000, "Closing connection")
            _isWebSocketConnected.value = false
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting WebSocket: ${e.message}", e)
        }
    }

    fun updateData(
        gbr: Int?,
        voznjaBusId: String,
        voznjaId: String,
        lat: Double?,
        lon: Double?,
        busId: String,
    ) {
        try {
            if (gbr != null && lat != null && lon != null) {
                val busLocation = Re(gbr, lat, lon, voznjaBusId, voznjaId, busId)

                // Store in map
                busLocationsMap[gbr] = busLocation

                // Update LiveData
                _busLocationsMapLiveData.value = busLocationsMap.toMap()

                // Also update legacy LiveData
                val currentList = busLocationsLiveData2.value.orEmpty().toMutableList()
                currentList.add(busLocation)
                busLocationsLiveData2.value = currentList
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating data: ${e.message}", e)
        }
    }

    // Get all bus locations
    fun getAllBusLocations(): List<Re> {
        return busLocationsMap.values.toList()
    }

    // Get a specific bus location by gbr
    fun getBusLocationByGbr(gbr: Int): Re? {
        return busLocationsMap[gbr]
    }

    // Clear stored bus locations
    fun clearBusLocations() {
        busLocationsMap.clear()
        _busLocationsMapLiveData.value = emptyMap()
        busLocationsLiveData2.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}
