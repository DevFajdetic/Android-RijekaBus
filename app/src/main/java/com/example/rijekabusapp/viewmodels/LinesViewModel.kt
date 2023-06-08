package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.network.jsons.BusData
import com.example.rijekabusapp.network.models.Line
import com.google.gson.Gson
import kotlinx.coroutines.launch

class LinesViewModel(application: Application) : AndroidViewModel(application) {
    val linesList = MutableLiveData<ArrayList<Line>>()
    private val _favoriteLines = MutableLiveData<ArrayList<Line>>()
    val favoriteLines: LiveData<ArrayList<Line>> = _favoriteLines

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getLinesList() {
        viewModelScope.launch {
            val jsonString = BusData.buseviJson
            val gson = Gson()
            val jsonArray = gson.fromJson(jsonString, Array<Line>::class.java)
            linesList.value = ArrayList(jsonArray.asList())
        }
    }

    fun insertFavoriteLine(line: Line) {
        repository.insertFavoriteLine(
            line.convertToFavoriteLine(repository.countFavoriteLines() + 1)
        )
    }

    fun deleteFavoriteLine(line: Line) {
        repository.deleteFavoriteLine(line.convertToFavoriteLine(null))
    }

    fun getFavoriteLines() {
        val lines = ArrayList<Line>()
        repository.getFavoriteLines().forEach {
            lines.add(it.convertToLine())
        }
        _favoriteLines.value = lines
    }
}
