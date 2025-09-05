package com.example.rijekabusapp.network.models

import java.io.Serializable

data class BusStop(
    val name: String,
    val location: Location,
) : Serializable
