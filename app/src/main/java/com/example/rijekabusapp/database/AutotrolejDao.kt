package com.example.rijekabusapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.database.models.FavoriteStation
import com.example.rijekabusapp.database.models.UserStats

@Dao
interface AutotrolejDao {
    // Favorite Line
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteLine(line: FavoriteLine)

    @Update
    fun updateFavoriteLine(line: FavoriteLine)

    @Query("SELECT * FROM favoriteLines")
    fun getFavoriteLines(): List<FavoriteLine>

    @Query("SELECT * FROM favoriteLines")
    suspend fun getFavoriteLinesAsync(): List<FavoriteLine>

    @Query("SELECT * FROM favoriteLines WHERE name = :name")
    fun isLineFavorite(name: String): Boolean

    @Delete
    fun deleteFavoriteLine(line: FavoriteLine)

    @Query("DELETE FROM favoriteLines")
    fun deleteFavoriteLines()

    @Query("SELECT COUNT(*) FROM favoriteLines")
    fun countFavoriteLines(): Int

    // Favorite Station
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteStation(station: FavoriteStation)

    @Update
    fun updateFavoriteStation(station: FavoriteStation)

    @Query("SELECT * FROM favoriteStations")
    fun getFavoriteStations(): List<FavoriteStation>

    @Query("SELECT * FROM favoriteStations")
    suspend fun getFavoriteStationsAsync(): List<FavoriteStation>

    @Query("SELECT * FROM favoriteStations WHERE id = :id")
    fun isStationFavorite(id: Int): Boolean

    @Delete
    fun deleteFavoriteStation(station: FavoriteStation)

    @Query("DELETE FROM favoriteStations")
    fun deleteFavoriteStations()

    @Query("SELECT COUNT(*) FROM favoriteStations")
    fun countFavoriteStations(): Int

    // Favorite Route
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteRoute(route: FavoriteRoute)

    @Update
    fun updateFavoriteRoute(route: FavoriteRoute)

    @Query("SELECT * FROM favoriteRoutes")
    fun getFavoriteRoutes(): List<FavoriteRoute>

    @Query("SELECT * FROM favoriteRoutes")
    suspend fun getFavoriteRoutesAsync(): List<FavoriteRoute>

    @Query("SELECT * FROM favoriteRoutes WHERE id = :id")
    fun isRouteFavorite(id: Int): Boolean

    @Delete
    fun deleteFavoriteRoute(route: FavoriteRoute)

    @Query("DELETE FROM favoriteRoutes")
    fun deleteFavoriteRoutes()

    @Query("SELECT COUNT(*) FROM favoriteRoutes")
    fun countFavoriteRoutes(): Int
    
    // User Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserStats(userStats: UserStats)
    
    @Update
    fun updateUserStats(userStats: UserStats)
    
    @Query("SELECT * FROM userStats WHERE userId = :userId")
    fun getUserStats(userId: String): UserStats?
    
    @Query("SELECT * FROM userStats LIMIT 1")
    fun getAnyUserStats(): UserStats?
    
    @Delete
    fun deleteUserStats(userStats: UserStats)
    
    @Query("DELETE FROM userStats")
    fun deleteAllUserStats()
}
