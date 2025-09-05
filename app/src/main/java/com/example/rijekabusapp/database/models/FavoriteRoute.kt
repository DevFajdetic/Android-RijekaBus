package com.example.rijekabusapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.rijekabusapp.database.converters.StepListConverter
import com.example.rijekabusapp.network.models.Step
import java.io.Serializable

@Entity(tableName = "favoriteRoutes")
@TypeConverters(StepListConverter::class)
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
    val routeType: String = "MIXED", // Can be BUS, WALK, or MIXED
    val steps: List<Step> = emptyList(), // Store all steps of the route
) : Serializable
