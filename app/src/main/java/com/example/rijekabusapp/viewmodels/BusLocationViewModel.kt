package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.network.BASE_URL_MY_API
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.BusLocation
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

class BusLocationViewModel(application: Application) : AndroidViewModel(application) {
    // LiveData for bus locations
    private val _busLocationsLiveData = MutableLiveData<List<BusLocation>>()
    val busLocationsLiveData: LiveData<List<BusLocation>> = _busLocationsLiveData

    private val tag = "BusLocationViewModel"

    // WebSocket connection
    private var webSocket: WebSocket? = null

    // Flag to track if WebSocket is connected
    private val _isWebSocketConnected = MutableLiveData<Boolean>(false)
    val isWebSocketConnected: LiveData<Boolean> = _isWebSocketConnected

    // Flag to track if initial data has been loaded
    private var initialDataLoaded = false

    // Store bus locations by GBR for quick updates
    private val busLocationsMap = ConcurrentHashMap<Int, BusLocation>()

    // LiveData for the map of bus locations
    private val _busLocationsMapLiveData = MutableLiveData<Map<Int, BusLocation>>(emptyMap())
    val busLocationsMapLiveData: LiveData<Map<Int, BusLocation>> = _busLocationsMapLiveData
    
    // Track the currently subscribed bus ID
    private val _currentBusId = MutableLiveData<String?>(null)
    val currentBusId: LiveData<String?> = _currentBusId
    
    // LiveData for the current bus location
    private val _currentBusLocation = MutableLiveData<BusLocation?>(null)
    val currentBusLocation: LiveData<BusLocation?> = _currentBusLocation

    // Get bus locations from REST API
    fun getBusLocations() {
        // Always fetch fresh data from the API
        fetchBusLocations()
    }

    // Manually refresh bus locations from the API
    fun refreshBusLocations() {
        Log.d(tag, "Manually refreshing bus locations")
        fetchBusLocations()
    }

    private fun fetchBusLocations() {
        viewModelScope.launch {
            try {
                Log.d(tag, "Fetching initial bus locations from /bus-locations endpoint")
                val response = Network().getMyApiService().getBusLocations()

                // Clear existing data to ensure fresh state
                busLocationsMap.clear()

                // Store new data
                response.forEach { busLocation ->
                    busLocationsMap[busLocation.gbr] = busLocation
                }

                // Update LiveData
                _busLocationsLiveData.value = busLocationsMap.values.toList()
                _busLocationsMapLiveData.value = busLocationsMap.toMap()
                
                // Update current bus location if we're subscribed to a specific bus
                updateCurrentBusLocation()

                initialDataLoaded = true
                Log.d(tag, "Successfully loaded ${response.size} bus locations")
            } catch (e: Exception) {
                Log.e(tag, "Error getting bus locations: ${e.message}", e)
                _busLocationsLiveData.value = emptyList()
            }
        }
    }
    
    // Subscribe to updates for a specific bus
    fun subscribeToSpecificBus(voznjaBusId: String) {
        if (_currentBusId.value != voznjaBusId) {
            _currentBusId.value = voznjaBusId
            Log.d(tag, "Subscribed to bus with voznjaBusId: $voznjaBusId")

            // Update current bus location immediately if we have it
            updateCurrentBusLocation()

            // Make sure WebSocket is connected
            if (!isConnected()) {
                connectToWebSocket()
            }
        }
    }
    
    // Unsubscribe from specific bus
    fun unsubscribeFromSpecificBus() {
        _currentBusId.value = null
        _currentBusLocation.value = null
        Log.d(tag, "Unsubscribed from specific bus")
    }
    
    // Update current bus location
    private fun updateCurrentBusLocation() {
        val busId = _currentBusId.value
        if (busId != null) {
            val busLocation = busLocationsMap.values.find { it.voznjaBusId == busId }
            _currentBusLocation.value = busLocation
            Log.d(tag, "Updated current bus location: ${busLocation?.gbr}")
        }
    }

    fun connectToWebSocket() {
        try {
            val client = OkHttpClient()
            val request =
                Request.Builder().url("${BASE_URL_MY_API.replace("http:", "ws:")}ws").build()

            webSocket = client.newWebSocket(
                request,
                object : WebSocketListener() {
                    override fun onMessage(
                        webSocket: WebSocket,
                        text: String,
                    ) {
                        try {
                            val busLocation = parseBusLocationMessage(text)
                            if (busLocation != null) {
                                updateBusLocation(busLocation)
                                
                                // Check if this is our subscribed bus
                                if (_currentBusId.value == busLocation.voznjaBusId) {
                                    _currentBusLocation.postValue(busLocation)
                                    Log.d(tag, "Received update for subscribed bus: ${busLocation}")
                                }
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
                            kotlinx.coroutines.delay(50000)
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
                }
            )
        } catch (e: Exception) {
            Log.e(tag, "Error connecting to WebSocket: ${e.message}", e)
            _isWebSocketConnected.postValue(false)
        }
    }

    private fun parseBusLocationMessage(message: String): BusLocation? {
        return try {
            val data = message.split("|")
            if (data.size >= 13) {
                val gbr = data[0].toIntOrNull()
                val voznjaBusId = data[1]
                val voznjaId = data[2]
                val lat = data[3].toDoubleOrNull()
                val lon = data[4].toDoubleOrNull()
                val busId = data[5]
                val brojLinije = if (data.size > 6 && data[6].isNotEmpty()) data[6] else null
                val smjer = if (data.size > 7 && data[7].isNotEmpty()) data[7] else null
                val varijanta = if (data.size > 8 && data[8].isNotEmpty()) data[8] else null
                val nazivVarijanteLinije =
                    if (data.size > 9 && data[9].isNotEmpty()) data[9] else null
                val nextStationId =
                    if (data.size > 10 && data[10].isNotEmpty()) data[10].toIntOrNull() else null
                val nextStationName =
                    if (data.size > 11 && data[11].isNotEmpty()) data[11] else null
                val distanceToNext = if (data.size > 12 && data[12].isNotEmpty()) data[12] else null

                if (gbr != null && lat != null && lon != null) {
                    BusLocation(
                        gbr = gbr,
                        voznjaBusId = voznjaBusId,
                        voznjaId = voznjaId,
                        lat = lat,
                        lon = lon,
                        busId = busId,
                        brojLinije = brojLinije,
                        smjer = smjer,
                        varijanta = varijanta,
                        nazivVarijanteLinije = nazivVarijanteLinije,
                        nextStationId = nextStationId,
                        nextStationName = nextStationName,
                        distanceToNext = distanceToNext
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing bus location message: ${e.message}", e)
            null
        }
    }

    private fun updateBusLocation(busLocation: BusLocation) {
        // Store in map
        busLocationsMap[busLocation.gbr] = busLocation

        // Update map LiveData
        _busLocationsMapLiveData.postValue(busLocationsMap.toMap())

        // Update list LiveData
        _busLocationsLiveData.postValue(busLocationsMap.values.toList())
    }

    fun disconnectWebSocket() {
        try {
            webSocket?.close(1000, "Closing connection")
            _isWebSocketConnected.value = false
            Log.d(tag, "WebSocket disconnected")
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting WebSocket: ${e.message}", e)
        }
    }

    // Check if WebSocket is currently connected
    fun isConnected(): Boolean {
        return _isWebSocketConnected.value == true
    }

    // Get a specific bus location by gbr
    fun getBusLocationByGbr(gbr: Int): BusLocation? {
        return busLocationsMap[gbr]
    }

    // Clear stored bus locations
    fun clearBusLocations() {
        busLocationsMap.clear()
        _busLocationsMapLiveData.value = emptyMap()
        _busLocationsLiveData.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}
