package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.response.BusesLocationsResponse
import com.example.rijekabusapp.network.response.LinesResponse
import com.example.rijekabusapp.network.response.ScheduleResponse
import com.example.rijekabusapp.network.response.StationsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BusService {
    @GET("ATlinije.json")
    suspend fun getAllLines(
        @Query("per_page") numberOfResults: Int,
        @Query("page") pageNumber: Int
    ): LinesResponse

    @GET("ATstanice.json")
    suspend fun getAllStations(): StationsResponse

    @GET("ATPoz.php?type=json")
    suspend fun getAllBusesLocations(): BusesLocationsResponse

    @GET("ATvoznired.json")
    suspend fun getTodaySchedule(): ScheduleResponse
}
