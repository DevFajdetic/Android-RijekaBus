package com.example.rijekabusapp.network.models


import com.google.gson.annotations.SerializedName

data class Line(
    @SerializedName("BrojLinije")
    val lineNumber: String,
    @SerializedName("Id")
    val id: Int,
    @SerializedName("LinVarId")
    val linVarId: String,
    @SerializedName("NazivVarijanteLinije")
    val name: String,
    @SerializedName("RedniBrojStanice")
    val stationOrdinal: Int,
    @SerializedName("Smjer")
    val direction: String,
    @SerializedName("StanicaId")
    val currentStationId: Int,
    @SerializedName("Varijanta")
    val variant: String
)