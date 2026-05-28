package com.example.sonexa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    // We use the MediaStore song ID as our Primary Key!
    @PrimaryKey
    val songId: Long,

    // We save the timestamp so we can sort by "Recently Added to Favorites" later
    val addedAt: Long = System.currentTimeMillis()
)