package com.example.rijekabusapp.network.response

import com.example.rijekabusapp.network.models.GeocodedWaypoint
import com.example.rijekabusapp.network.models.Route
import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("error_message")
    val errorMessage: String,
    @SerializedName("geocoded_waypoints")
    val geocodedWaypoints: List<GeocodedWaypoint>,
    @SerializedName("routes")
    val routes: List<Route>,
    @SerializedName("status")
    val status: String,
)
