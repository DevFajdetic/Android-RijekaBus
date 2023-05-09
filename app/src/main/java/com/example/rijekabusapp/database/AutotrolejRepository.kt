package com.example.rijekabusapp.database

import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.database.models.FavoriteStation

class AutotrolejRepository(private val autotrolejDao: AutotrolejDao) {

    // Favorite Line
    fun getFavoriteLines(): List<FavoriteLine> {
        return autotrolejDao.getFavoriteLines()
    }

    suspend fun getFavoriteLinesAsync(): List<FavoriteLine> {
        return autotrolejDao.getFavoriteLinesAsync()
    }

    fun insertFavoriteLine(line: FavoriteLine) {
        autotrolejDao.insertFavoriteLine(line)
    }

    fun isLineFavorite(id: Int): Boolean {
        return autotrolejDao.isLineFavorite(id)
    }

    fun deleteFavoriteLine(line: FavoriteLine) {
        autotrolejDao.deleteFavoriteLine(line)
    }

    fun deleteFavoriteLines() {
        autotrolejDao.deleteFavoriteLines()
    }

    // Favorite Station
    fun getFavoriteStations(): List<FavoriteStation> {
        return autotrolejDao.getFavoriteStations()
    }

    suspend fun getFavoriteStationsAsync(): List<FavoriteStation> {
        return autotrolejDao.getFavoriteStationsAsync()
    }

    fun isStationFavorite(id: Int): Boolean {
        return autotrolejDao.isStationFavorite(id)
    }

    fun insertFavoriteStation(team: FavoriteStation) {
        autotrolejDao.insertFavoriteStation(team)
    }

    fun deleteFavoriteStation(team: FavoriteStation) {
        autotrolejDao.deleteFavoriteStation(team)
    }

    fun deleteFavoriteStations() {
        autotrolejDao.deleteFavoriteStations()
    }
}