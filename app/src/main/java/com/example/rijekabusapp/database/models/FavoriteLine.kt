package com.example.rijekabusapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rijekabusapp.network.models.Line
import java.io.Serializable

@Entity(tableName = "favoriteLines")
data class FavoriteLine(
    val lineNumber: String,
    @PrimaryKey
    val id: Int,
    val linVarId: String,
    val name: String,
    val stationOrdinal: Int,
    val direction: String,
    val currentStationId: Int,
    val variant: String
) : Serializable {

    fun convertToLine(): Line {
        return Line(
            this.lineNumber,
            this.id,
            this.linVarId,
            this.name,
            this.stationOrdinal,
            this.direction,
            this.currentStationId,
            this.variant
        )
    }
}
