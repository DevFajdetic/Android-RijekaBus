package com.example.rijekabusapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rijekabusapp.database.converters.StepListConverter
import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.database.models.FavoriteStation
import com.example.rijekabusapp.database.models.UserStats

@Database(
    entities = [FavoriteLine::class, FavoriteStation::class, FavoriteRoute::class, UserStats::class],
    version = 3,
)
@TypeConverters(StepListConverter::class)
abstract class AutotrolejDatabase : RoomDatabase() {
    abstract fun autotrolejDao(): AutotrolejDao

    companion object {
        private var instance: AutotrolejDatabase? = null

        fun getDatabase(context: Context): AutotrolejDatabase? {
            if (instance == null) {
                instance = buildDatabase(context)
            }
            return instance
        }

        private fun buildDatabase(context: Context): AutotrolejDatabase =
            Room.databaseBuilder(context, AutotrolejDatabase::class.java, "AutotrolejDatabase")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration() // Handle version upgrades
                .build()
    }
}
