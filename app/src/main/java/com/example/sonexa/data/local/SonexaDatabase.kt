package com.example.sonexa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavoriteSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SavedSongEntity::class,
        SongStatsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SonexaDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songStatsDao(): SongStatsDao

    companion object {
        @Volatile
        private var INSTANCE: SonexaDatabase? = null

        // 🚨 THE FIX: Define exactly what changes between Version 3 and Version 4
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Creates the new stats table without touching the existing playlists or favorites!
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `song_stats` (" +
                            "`mediaId` INTEGER NOT NULL, " +
                            "`playCount` INTEGER NOT NULL, " +
                            "`lastPlayedTimestamp` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`mediaId`))"
                )
            }
        }

        fun getDatabase(context: Context): SonexaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonexaDatabase::class.java,
                    "sonexa_database"
                )
                    // 🚨 THE FIX: Remove fallbackToDestructiveMigration() and use addMigrations()
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}