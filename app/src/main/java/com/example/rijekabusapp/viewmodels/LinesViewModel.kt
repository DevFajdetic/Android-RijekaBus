package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.AutotrolejDatabase
import com.example.rijekabusapp.database.AutotrolejRepository
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.Line
import kotlinx.coroutines.launch

class LinesViewModel(application: Application) : AndroidViewModel(application) {
    val linesList = MutableLiveData<ArrayList<Line>>()
    val fullLinesList = MutableLiveData<ArrayList<Line>>()
    private val _favoriteLines = MutableLiveData<ArrayList<Line>>()
    val favoriteLines: LiveData<ArrayList<Line>> = _favoriteLines
    val comparator =
        Comparator<Line> { line1, line2 ->
            val isStr1Numeric = line1.lineNumber.length == 1 && line1.lineNumber[0].isDigit()
            val isStr2Numeric = line2.lineNumber.length == 1 && line2.lineNumber[0].isDigit()

            when {
                isStr1Numeric && isStr2Numeric -> line1.lineNumber.compareTo(line2.lineNumber)
                isStr1Numeric -> -1
                isStr2Numeric -> 1
                else -> line1.lineNumber.compareTo(line2.lineNumber)
            }
        }

    private val repository: AutotrolejRepository

    init {
        val autotrolejDao = AutotrolejDatabase.getDatabase(application)!!.autotrolejDao()
        repository = AutotrolejRepository(autotrolejDao)
    }

    fun getLinesList() {
        viewModelScope.launch {
            val response = Network().getBusService().getAllLines()
            fullLinesList.value = ArrayList(response)
            linesList.value = getDistinctedBusLines(response)
        }
    }

    fun getDistinctedBusLines(lines: List<Line>): ArrayList<Line> {
        val uniqueLines =
            lines.distinctBy {
                it.name + it.lineNumber + it.direction + it.linVarId
            }.sortedWith(comparator)
        return ArrayList(uniqueLines)
    }

    fun insertFavoriteLine(line: Line) {
        repository.insertFavoriteLine(
            line.convertToFavoriteLine(repository.countFavoriteLines() + 1),
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
