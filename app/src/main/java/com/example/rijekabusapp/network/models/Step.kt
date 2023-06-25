package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName

data class Step(
    @SerializedName("distance")
    val distance: Distance,
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("end_location")
    val endLocation: Double,
    @SerializedName("html_instructions")
    val htmlInstructions: String,
    @SerializedName("maneuver")
    val maneuver: String?,
    @SerializedName("polyline")
    val polyline: Polyline,
    @SerializedName("start_location")
    val startLocation: Double,
    @SerializedName("travel_mode")
    val travelMode: String
)
