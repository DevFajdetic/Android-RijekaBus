import android.util.Log
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient(private val viewModel: BusLocationViewModel) : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private val okHttpClient =
        OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

    private var isConnected = false
    private var shouldReconnect = true
    private val reconnectDelay = 3000L // 3 seconds delay before reconnecting

    // Coroutine scope for handling reconnection
    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect() {
        if (isConnected) return // Avoid multiple connections

        val request =
            Request.Builder()
                .url("ws://your-websocket-url") // Replace with your WebSocket URL
                .build()

        webSocket = okHttpClient.newWebSocket(request, this)
        isConnected = true
    }

    fun disconnect() {
        shouldReconnect = false // Stop reconnection attempts
        webSocket?.close(1000, "User initiated disconnect")
        webSocket = null
        isConnected = false
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect) return

        scope.launch {
            delay(reconnectDelay) // Wait before reconnecting
            if (!isConnected && shouldReconnect) {
                Log.d("WebSocket", "Attempting to reconnect...")
                connect()
            }
        }
    }

    override fun onOpen(
        webSocket: WebSocket,
        response: Response,
    ) {
        Log.d("WebSocket", "Connection opened")
        isConnected = true
    }

    override fun onMessage(
        webSocket: WebSocket,
        text: String,
    ) {
        // Parse the incoming message
        val data = text.split(";")
        val gbr = data[0].toIntOrNull()
        val voznjaBusId = data[1].toString()
        val voznjaId = data[2].toString()
        val lat = data[3].toDoubleOrNull()
        val lon = data[4].toDoubleOrNull()
        val busId = data[5].toString()

        // Update LiveData in the ViewModel
        viewModel.updateData(gbr, voznjaBusId, voznjaId, lat, lon, busId)
    }

    override fun onFailure(
        webSocket: WebSocket,
        t: Throwable,
        response: Response?,
    ) {
        Log.e("WebSocket", "Connection failed: ${t.message}")
        isConnected = false
        scheduleReconnect() // Attempt to reconnect
    }

    override fun onClosed(
        webSocket: WebSocket,
        code: Int,
        reason: String,
    ) {
        Log.d("WebSocket", "Connection closed: $reason")
        isConnected = false
        scheduleReconnect() // Attempt to reconnect
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String,
    ) {
        Log.d("WebSocket", "Connection closing: $reason")
        isConnected = false
        webSocket.close(1000, null)
    }
}
