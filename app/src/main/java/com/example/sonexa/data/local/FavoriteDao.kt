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
    // We return a Flow so the UI instantly updates when a new favorite is added!
    @Query("SELECT songId FROM favorite_songs ORDER BY addedAt DESC")
    fun getAllFavoriteSongIds(): Flow<List<Long>>

    // 4. Check if a specific song is a favorite (returns true/false)
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :id)")
    fun isFavorite(id: Long): Flow<Boolean>
}