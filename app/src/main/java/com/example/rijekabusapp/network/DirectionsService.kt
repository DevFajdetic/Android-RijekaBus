package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.response.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsService {
    @GET("directions/json")
    suspend fun getDirection(
        @Query("destination") destination: String,
        @Query("origin") origin: String,
        @Query("key") apiKey: String,
        @Query("language") language: String,
        @Query("mode") mode: String,
        @Query("units") units: String,
    ): DirectionsResponse
}
