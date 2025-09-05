package com.example.rijekabusapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "userStats")
data class UserStats(
    @PrimaryKey
    val userId: String,
    val totalDistance: Double,
    val totalTime: Int,
    val totalTrips: Int,
    val experiencePoints: Int,
    val level: Int,
) : Serializable 