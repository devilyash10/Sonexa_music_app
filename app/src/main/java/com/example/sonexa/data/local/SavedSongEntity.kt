package com.example.sonexa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_songs")
data class SavedSongEntity(
    @PrimaryKey val songId: Long, // Matches your Song.id
    val title: String,
    val artist: String,
    val mediaUri: String,
    val artworkUri: String
)