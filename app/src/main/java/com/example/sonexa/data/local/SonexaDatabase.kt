package com.example.sonexa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Bump the version number if you ever change the Entity structure later
@Database(entities = [FavoriteSongEntity::class], version = 1, exportSchema = false)
abstract class SonexaDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: SonexaDatabase? = null

        fun getDatabase(context: Context): SonexaDatabase {
            // Only create one instance of the database to prevent memory leaks (Singleton pattern)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonexaDatabase::class.java,
                    "sonexa_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}