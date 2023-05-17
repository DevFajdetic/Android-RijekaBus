package com.example.rijekabusapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rijekabusapp.network.models.Station
import java.io.Serializable

@Entity(tableName = "favoriteStations")
data class FavoriteStation(
    val gpsX: Double,
    val gpsY: Double,
    val shortName: String,
    val longName: String,
    @PrimaryKey
    val id: Int
) : Serializable {

    fun convertToStation(): Station {
        return Station(
            this.gpsX,
            this.gpsY,
            this.shortName,
            this.longName,
            this.id
        )
    }
}
