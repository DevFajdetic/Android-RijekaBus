package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName

data class BusLocation(
    @SerializedName("gbr")
    val gbr: Int,
    @SerializedName("voznjaBusId")
    val voznjaBusId: String,
    @SerializedName("voznjaId")
    val voznjaId: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("busId")
    val busId: String,
    @SerializedName("brojLinije")
    val brojLinije: String? = null,
    @SerializedName("smjer")
    val smjer: String? = null,
    @SerializedName("varijanta")
    val varijanta: String? = null,
    @SerializedName("nazivVarijanteLinije")
    val nazivVarijanteLinije: String? = null,
    @SerializedName("nextStationId")
    val nextStationId: Int? = null,
    @SerializedName("NextStationName")
    val nextStationName: String? = null,
    @SerializedName("distanceToNext")
    val distanceToNext: String? = null
)
