package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName

data class TransitDetails(
    @SerializedName("line") val line: BusLine?,
    @SerializedName("headsign") val headSign: String?,
    @SerializedName("departure_stop") val departureStop: BusStop,
    @SerializedName("arrival_stop") val arrivalStop: BusStop,
    @SerializedName("arrival_time") val arrivalTime: MapTime,
    @SerializedName("departure_time") val departureTime: MapTime,
    @SerializedName("num_stops") val stops: Int
)
