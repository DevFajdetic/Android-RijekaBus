package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.response.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") location: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
    ): WeatherResponse
}
