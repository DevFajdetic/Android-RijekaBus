package com.example.rijekabusapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL_BUS = "http://e-usluge2.rijeka.hr/OpenData/"
const val BASE_URL_WEATHER = "https://www.metaweather.com/api/"

class Network {

    private val busService: BusService
    // private val weatherService: WeatherService

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor)

        val retrofitBus = Retrofit.Builder().baseUrl(BASE_URL_BUS)
            .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build()).build()

        /*val retrofitWeather =
            Retrofit.Builder().baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    httpClient
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS).build()
                ).build()
        */
        busService = retrofitBus.create(BusService::class.java)
        // weatherService = retrofitWeather.create(WeatherService::class.java)
    }

    fun getBusService(): BusService = busService

    // fun getWeatherService(): WeatherService = weatherService
}
