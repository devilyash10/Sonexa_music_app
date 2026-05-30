package com.example.sonexa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Added the two new entities and bumped version to 2
@Database(
    entities = [
        FavoriteSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SavedSongEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SonexaDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    // 2. Added the new Playlist DAO
    abstract fun playlistDao(): PlaylistDao

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
                    // 3. THIS LINE IS MANDATORY TO PREVENT CRASHES ON VERSION BUMP
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}