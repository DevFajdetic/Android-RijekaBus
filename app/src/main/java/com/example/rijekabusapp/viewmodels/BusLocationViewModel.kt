package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.BusLocation
import kotlinx.coroutines.launch

class BusLocationViewModel(application: Application) : AndroidViewModel(application) {
    val busLocationsLiveData = MutableLiveData<List<BusLocation>>()

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getBusLocations() {
        viewModelScope.launch {
            busLocationsLiveData.value = Network().getBusService().getAllBusesLocations()
        }
    }
}
