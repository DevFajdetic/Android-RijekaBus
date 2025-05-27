package com.example.rijekabusapp.network.models

import java.io.Serializable

data class StationImage(
    val stationId: Int,
    val imageUrl: String,
    val imageCaption: String,
    val id: Int?,
) : Serializable
