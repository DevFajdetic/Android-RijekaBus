package com.example.rijekabusapp.database

import androidx.room.*
import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.database.models.FavoriteStation

@Dao
interface AutotrolejDao {

    // Favorite Line
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteLine(line: FavoriteLine)

    @Query("SELECT * FROM favoritePlayers")
    fun getFavoriteLines(): List<FavoriteLine>

    @Query("SELECT * FROM favoritePlayers")
    suspend fun getFavoriteLinesAsync(): List<FavoriteLine>

    @Query("SELECT * FROM favoritePlayers WHERE id = :id")
    fun isLineFavorite(id: Int): Boolean

    @Delete
    fun deleteFavoriteLine(line: FavoriteLine)

    @Query("DELETE FROM favoritePlayers")
    fun deleteFavoriteLines()

    // Favorite Station
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteStation(station: FavoriteStation)

    @Query("SELECT * FROM favoriteTeams")
    fun getFavoriteStations(): List<FavoriteStation>

    @Query("SELECT * FROM favoriteTeams")
    suspend fun getFavoriteStationsAsync(): List<FavoriteStation>

    @Query("SELECT * FROM favoriteTeams WHERE id = :id")
    fun isStationFavorite(id: Int): Boolean

    @Delete
    fun deleteFavoriteStation(station: FavoriteStation)

    @Query("DELETE FROM favoriteTeams")
    fun deleteFavoriteStations()
}
