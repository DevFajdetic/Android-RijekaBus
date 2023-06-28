package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.BusLocation
import kotlinx.coroutines.launch

class BusLocationViewModel(application: Application) : AndroidViewModel(application) {
    val busLocationsLiveData = MutableLiveData<List<BusLocation>>()

    fun getBusLocations() {
        viewModelScope.launch {
            busLocationsLiveData.value = Network().getAutotrolejService().getBusesLocations()
        }
    }
}
