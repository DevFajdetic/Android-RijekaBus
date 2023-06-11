package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.response.BusesLocationsResponse
import retrofit2.http.GET

interface AutotrolejService {
    @GET("?autotrolej")
    suspend fun getBusesLocations(): BusesLocationsResponse
}