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

    // 1. Saves the song snapshot to the cache
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun cacheSong(song: SavedSongEntity)

    // 2. Gets the full song data for a specific playlist
    // Upgrade your query to target only the saved_songs columns
    @androidx.room.Query("""
        SELECT saved_songs.* FROM saved_songs 
        INNER JOIN playlist_song_cross_ref 
        ON saved_songs.songId = playlist_song_cross_ref.songId 
        WHERE playlist_song_cross_ref.playlistId = :playlistId
    """)
    fun getSongsForPlaylistWithData(playlistId: Long): kotlinx.coroutines.flow.Flow<List<SavedSongEntity>>}