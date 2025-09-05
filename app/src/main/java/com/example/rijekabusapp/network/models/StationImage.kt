package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StationImage(
    @SerializedName("station_id")
    val stationId: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("image_caption")
    val imageCaption: String,
    @SerializedName("id")
    val id: Int?,
) : Serializable
