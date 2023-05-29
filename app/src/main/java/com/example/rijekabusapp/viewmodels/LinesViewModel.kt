package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.paging.line.LinePagingSource
import kotlinx.coroutines.flow.*

class LinesViewModel(application: Application) : AndroidViewModel(application) {
    private var currentDirection = ""
    private val _favoriteLines = MutableLiveData<ArrayList<Line>>()
    val favoriteLines: LiveData<ArrayList<Line>> = _favoriteLines
    val linePagingSource = LinePagingSource(Network().getBusService(), currentDirection)
    var flow = Pager(PagingConfig(pageSize = 20)) {
        linePagingSource
    }.flow.cachedIn(viewModelScope)

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
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
        Log.d("viewmodel", "fl")
        val lines = ArrayList<Line>()
        repository.getFavoriteLines().forEach {
            lines.add(it.convertToLine())
        }
        _favoriteLines.value = lines
    }

    fun updateDirection(direction: String) {
        this.currentDirection = direction
        linePagingSource.updateDirection(direction)
        linePagingSource.invalidate()
    }
}
