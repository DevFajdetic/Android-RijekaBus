package com.example.rijekabusapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.database.models.FavoriteStation

@Database(
    entities = [FavoriteLine::class, FavoriteStation::class],
    version = 1
)

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
                .allowMainThreadQueries().build()
    }

}