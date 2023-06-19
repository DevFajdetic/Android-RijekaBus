package com.example.rijekabusapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL_BUS = "https://busri.alwaysdata.net/maps/"
const val BASE_URL_AUTOTROLEJ = "https://winter-star-9de5.kombajn.workers.dev/"
const val BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/"
const val BASE_URL_DIRECTIONS = "https://maps.googleapis.com/maps/api/"

class Network {

    private val busService: BusService
    private val weatherService: WeatherService
    private val autotrolejService: AutotrolejService
    private val directionsService: DirectionsService

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor)

        val retrofitBus = Retrofit.Builder().baseUrl(BASE_URL_BUS)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                httpClient
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
            ).build()

        val retrofitAutotrolej = Retrofit.Builder().baseUrl(BASE_URL_AUTOTROLEJ)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                httpClient
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
            ).build()

        val retrofitWeather =
            Retrofit.Builder().baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build()
                ).build()

        val retrofitDirections =
            Retrofit.Builder().baseUrl(BASE_URL_DIRECTIONS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build()
                ).build()

        busService = retrofitBus.create(BusService::class.java)
        autotrolejService = retrofitAutotrolej.create(AutotrolejService::class.java)
        weatherService = retrofitWeather.create(WeatherService::class.java)
        directionsService = retrofitDirections.create(DirectionsService::class.java)
    }

    fun getBusService(): BusService = busService
    fun getAutotrolejService(): AutotrolejService = autotrolejService

    fun getWeatherService(): WeatherService = weatherService
    fun getDirectionsService(): DirectionsService = directionsService
}
