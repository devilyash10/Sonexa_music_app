package com.example.sonexa.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    // We MUST return Long here to prevent the KSP 'V' crash
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteSong: FavoriteSongEntity): Long

    // We MUST return Int here to prevent the KSP 'V' crash
    @Delete
    suspend fun deleteFavorite(favoriteSong: FavoriteSongEntity): Int

    // 3. Get a real-time list of ALL favorite song IDs
    @Query("SELECT songId FROM favorite_songs ORDER BY addedAt DESC")
    fun getAllFavoriteSongIds(): Flow<List<Long>>

    // 4. Check if a specific song is a favorite (returns true/false)
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :id)")
    fun isFavorite(id: Long): Flow<Boolean>

    // 1. Saves the song snapshot to the cache
    // 🚨 APPLIED YOUR KSP FIX HERE: Added ': Long' return type!
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun cacheSong(song: SavedSongEntity): Long

    // 2. Gets the full song data for the favorites screen
    // 🚨 FIX: Updated the INNER JOIN to use 'favorite_songs' which matches your other queries!
    @Query("""
        SELECT saved_songs.* FROM saved_songs 
        INNER JOIN favorite_songs 
        ON saved_songs.songId = favorite_songs.songId
    """)
    fun getFavoriteSongsWithData(): Flow<List<SavedSongEntity>>
}