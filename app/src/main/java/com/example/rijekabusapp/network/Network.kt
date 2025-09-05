package com.example.rijekabusapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL_BUS = "https://busri.alwaysdata.net/maps/"
const val BASE_URL_AUTOTROLEJ = "https://api.autotrolej.hr/api/open/v1/voznired/"
const val BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/"
const val BASE_URL_DIRECTIONS = "https://maps.googleapis.com/maps/api/"
const val BASE_URL_MY_API = "http://192.168.1.27:8080/"
const val BASE_URL_OSRM = "https://router.project-osrm.org/"

class Network {
    private val busService: BusService
    private val weatherService: WeatherService
    private val autotrolejService: AutotrolejService
    private val directionsService: DirectionsService
    private val myApiService: MyApiService

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor)

        val retrofitBus =
            Retrofit.Builder().baseUrl(BASE_URL_BUS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build(),
                ).build()

        val retrofitAutotrolej =
            Retrofit.Builder().baseUrl(BASE_URL_AUTOTROLEJ)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build(),
                ).build()

        val retrofitWeather =
            Retrofit.Builder().baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build(),
                ).build()

        val retrofitDirections =
            Retrofit.Builder().baseUrl(BASE_URL_DIRECTIONS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build(),
                ).build()

        val retrofitMyApi =
            Retrofit.Builder().baseUrl(BASE_URL_MY_API)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build(),
                ).build()

        busService = retrofitBus.create(BusService::class.java)
        autotrolejService = retrofitAutotrolej.create(AutotrolejService::class.java)
        weatherService = retrofitWeather.create(WeatherService::class.java)
        directionsService = retrofitDirections.create(DirectionsService::class.java)
        myApiService = retrofitMyApi.create(MyApiService::class.java)
    }

    fun getBusService(): BusService = busService

    fun getAutotrolejService(): AutotrolejService = autotrolejService

    fun getWeatherService(): WeatherService = weatherService

    fun getDirectionsService(): DirectionsService = directionsService

    fun getMyApiService(): MyApiService = myApiService
}
