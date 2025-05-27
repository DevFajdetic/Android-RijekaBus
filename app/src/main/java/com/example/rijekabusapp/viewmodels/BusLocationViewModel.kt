package com.example.rijekabusapp.viewmodels
import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class BusLocationViewModel(application: Application) : AndroidViewModel(application) {
    val busLocationsLiveData = MutableLiveData<List<BusLocation>>()

    fun getBusLocations() {
        viewModelScope.launch {
            busLocationsLiveData.value = Network().getAutotrolejService().getBusesLocations()
        }
    }

    val busLocationsLiveData2 = MutableLiveData<List<Re>>()
    private var webSocket: WebSocket? = null

    fun connectToWebSocket() {
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
                        val busLocations = parseBusLocationMessage(text)
                        busLocationsLiveData2.postValue(busLocations)
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
                        t.printStackTrace()
                    }
                },
            )
    }

    fun getBusLocations2() {
        viewModelScope.launch {
            busLocationsLiveData2.value = Network().getAutotrolejService().getBusesLocations2().res
        }
    }

    private fun parseBusLocationMessage(message: String): List<Re> {
        val locationsList = mutableListOf<Re>()
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
        return locationsList
    }

    fun disconnectWebSocket() {
        webSocket?.close(1000, "Closing connection")
    }

    fun updateData(
        gbr: Int?,
        voznjaBusId: String,
        voznjaId: String,
        lat: Double?,
        lon: Double?,
        busId: String,
    ) {
        if (gbr != null && lat != null && lon != null) {
            val busLocation = Re(gbr, lat, lon, voznjaBusId, voznjaId, busId)
            val currentList = busLocationsLiveData2.value.orEmpty().toMutableList()
            currentList.add(busLocation)
            busLocationsLiveData2.value = currentList
        }
    }
}
