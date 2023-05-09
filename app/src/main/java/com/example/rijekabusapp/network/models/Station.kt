package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("GpsX")
    val gpsX: Double,
    @SerializedName("GpsY")
    val gpsY: Double,
    val shortName: String,
    val longName: String,
    @SerializedName("StanicaId")
    val id: Int
)