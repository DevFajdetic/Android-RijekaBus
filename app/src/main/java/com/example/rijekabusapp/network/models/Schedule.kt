package com.example.rijekabusapp.network.models


import com.google.gson.annotations.SerializedName

data class Schedule(
    @SerializedName("BrojLinije")
    val lineNumber: String,
    @SerializedName("GpsX")
    val gpsX: Double,
    @SerializedName("GpsY")
    val gpsY: Double,
    @SerializedName("Id")
    val id: Int,
    @SerializedName("LinVarId")
    val linVarId: String,
    @SerializedName("Naziv")
    val name: String,
    @SerializedName("NazivVarijanteLinije")
    val variantLineName: String,
    @SerializedName("PodrucjePrometa")
    val trafficArea: String,
    @SerializedName("Polazak")
    val startTime: String,
    @SerializedName("PolazakId")
    val startId: String,
    @SerializedName("RedniBrojStanice")
    val stationOrdial: Int,
    @SerializedName("Smjer")
    val direction: String,
    @SerializedName("StanicaId")
    val stationId: Int,
    @SerializedName("Varijanta")
    val variant: String
)