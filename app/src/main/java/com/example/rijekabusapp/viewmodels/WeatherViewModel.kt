package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.response.WeatherResponse
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    val currentWeather = MutableLiveData<WeatherResponse>()
    private val apiKey = "5fccc012ba66eed8f23349a033b2377f"
    private val cityName = "Rijeka"

    fun getCurrentWeather(
        metric: String,
        lang: String,
    ) {
        viewModelScope.launch {
            currentWeather.value =
                Network().getWeatherService().getCurrentWeather(
                    cityName,
                    apiKey,
                    metric,
                    lang,
                )
        }
    }
}
