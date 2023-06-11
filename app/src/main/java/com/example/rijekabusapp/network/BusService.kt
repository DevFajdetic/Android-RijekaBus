package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.response.ScheduleResponse
import com.example.rijekabusapp.network.response.StationsResponse
import retrofit2.http.GET

interface BusService {
    @GET("ATstanice.json")
    suspend fun getAllStations(): StationsResponse

    @GET("ATvoznired-tjedan.json")
    suspend fun getWeekSchedule(): ScheduleResponse

    @GET("ATvoznired-subota.json")
    suspend fun getSaturdaySchedule(): ScheduleResponse

    @GET("ATvoznired-nedjelja.json")
    suspend fun getSundaySchedule(): ScheduleResponse
}
