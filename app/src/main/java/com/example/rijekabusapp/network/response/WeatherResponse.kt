package com.example.rijekabusapp.network.response

data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val visibility: Float,
    val wind: Wind
)

data class Weather(
    val description: String,
    val icon: String
)

data class Main(
    val temp: Float,
    val pressure: Float,
    val humidity: Float,
)

data class Wind(
    val speed: Float
)
