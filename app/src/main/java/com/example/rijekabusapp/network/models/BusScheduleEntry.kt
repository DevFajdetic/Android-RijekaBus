package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName

data class BusScheduleEntry(
    @SerializedName("Id")
    val id: Int,
    @SerializedName("PolazakId")
    val polazakId: String,
    @SerializedName("StanicaId")
    val stanicaId: Int,
    @SerializedName("LinVarId")
    val linVarId: String,
    @SerializedName("Polazak")
    val polazak: String,
    @SerializedName("RedniBrojStanice")
    val redniBrojStanice: Int,
    @SerializedName("BrojLinije")
    val brojLinije: String,
    @SerializedName("Smjer")
    val smjer: String,
    @SerializedName("Varijanta")
    val varijanta: String,
    @SerializedName("NazivVarijanteLinije")
    val nazivVarijanteLinije: String,
    @SerializedName("PodrucjePrometa")
    val podrucjePrometa: String,
    @SerializedName("GpsX")
    val gpsX: Double,
    @SerializedName("GpsY")
    val gpsY: Double,
    @SerializedName("Naziv")
    val naziv: String
) 