package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName

data class BusLocation(
    @SerializedName("Autobus")
    val busId: String,
    @SerializedName("GpsX")
    val gpsX: Double,
    @SerializedName("GpsY")
    val gpsY: Double,
    @SerializedName("PolazakId")
    val startId: Int,
    @SerializedName("StanicaId")
    val nextStationId: Int,
    @SerializedName("Vrijeme")
    val provideTime: String,
)
