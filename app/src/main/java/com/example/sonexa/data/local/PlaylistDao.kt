package com.example.sonexa.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    // 🚨 FIXED: Added ': Long' to prevent the KSP JVM Signature V crash
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongIntoPlaylist(crossRef: PlaylistSongCrossRef): Long

    // 🚨 FIXED: Added ': Int' to prevent the KSP JVM Signature V crash
    @Delete
    suspend fun removeSongFromPlaylist(crossRef: PlaylistSongCrossRef): Int

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>>
}