package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MapTime(
    @SerializedName("text") val text: String,
    @SerializedName("time_zone") val timeZone: String,
    @SerializedName("value") val value: Long,
) : Serializable
