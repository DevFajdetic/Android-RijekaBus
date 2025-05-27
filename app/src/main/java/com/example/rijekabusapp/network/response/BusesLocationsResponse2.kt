package com.example.rijekabusapp.network.response

import com.google.gson.annotations.SerializedName

data class BusesLocationsResponse2(
    @SerializedName("err")
    val err: Boolean,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("res")
    val res: List<Re>,
)

data class Re(
    val gbr: Int,
    val lat: Double,
    val lon: Double,
    val voznjaBusId: String,
    val voznjaId: String,
    val busId: String,
)
