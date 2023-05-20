package com.example.rijekabusapp.network.models

import com.example.rijekabusapp.database.models.FavoriteStation
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Station(
    @SerializedName("GpsX")
    val gpsX: Double,
    @SerializedName("GpsY")
    val gpsY: Double,
    @SerializedName("Kratki")
    val shortName: String,
    @SerializedName("Naziv")
    val longName: String,
    @SerializedName("StanicaId")
    val id: Int
) : Serializable {
    fun convertToFavoriteStation(): FavoriteStation {
        return FavoriteStation(
            this.gpsX,
            this.gpsY,
            this.shortName,
            this.longName,
            this.id
        )
    }
}
