package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Polyline(
    @SerializedName("points")
    val points: String,
) : Serializable
