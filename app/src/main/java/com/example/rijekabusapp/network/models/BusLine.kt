package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName

data class BusLine(
    @SerializedName("name")
    val name: String,
    @SerializedName("short_name")
    val shortName: String,
    @SerializedName("line")
    val agencies: List<BusAgency>,
)
