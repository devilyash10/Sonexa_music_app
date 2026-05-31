package com.example.sonexa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongStatsDao {

    // Insert new stats, or replace if they already exist
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: SongStatsEntity)

    // Get a specific song's stats instantly
    @Query("SELECT * FROM song_stats WHERE mediaId = :mediaId")
    suspend fun getStatsForSong(mediaId: Long): SongStatsEntity?

    // Flow all stats so the UI updates automatically when play counts change
    @Query("SELECT * FROM song_stats")
    fun getAllSongStats(): Flow<List<SongStatsEntity>>

    // Increment play count and update timestamp
    @Query("UPDATE song_stats SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE mediaId = :mediaId")
    suspend fun recordPlay(mediaId: Long, timestamp: Long)

}