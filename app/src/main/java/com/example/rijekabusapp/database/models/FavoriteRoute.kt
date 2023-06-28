package com.example.rijekabusapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "favoriteRoutes")
data class FavoriteRoute(
    val origin: String,
    val depTime: String,
    val destination: String,
    val arrTime: String,
    val distance: String,
    val time: String,
    val busUsed: String,
    val date: String,
    @PrimaryKey
    val id: String,
) : Serializable
