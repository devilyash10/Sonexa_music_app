package com.example.sonexa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SavedSongEntity::class,
        SongStatsEntity::class // 🚨 1. Added the new Stats Entity!
    ],
    version = 4, // 🚨 2. Bumped version to 4!
    exportSchema = false
)
abstract class SonexaDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songStatsDao(): SongStatsDao // 🚨 3. Added the new Stats DAO!

    companion object {
        @Volatile
        private var INSTANCE: SonexaDatabase? = null

        fun getDatabase(context: Context): SonexaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonexaDatabase::class.java,
                    "sonexa_database"
                )
                    .fallbackToDestructiveMigration() // Safely handles the version 4 upgrade
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}