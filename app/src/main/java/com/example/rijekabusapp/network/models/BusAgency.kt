package com.example.rijekabusapp.network.models

import java.io.Serializable

data class BusAgency(
    val name: String,
    val phone: String,
    val website: String,
) : Serializable
