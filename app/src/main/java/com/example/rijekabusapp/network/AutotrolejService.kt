package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.response.BusesLocationsResponse
import com.example.rijekabusapp.network.response.LinesResponse
import retrofit2.http.GET

interface AutotrolejService {
    @GET("?autotrolej")
    suspend fun getBusesLocations(): BusesLocationsResponse

    @GET("linije")
    suspend fun getBusLines(): LinesResponse
}
