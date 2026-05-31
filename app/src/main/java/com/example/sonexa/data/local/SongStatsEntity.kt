package com.example.sonexa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_stats")
data class SongStatsEntity(
    @PrimaryKey
    val mediaId: Long, // This matches the MediaStore ID!
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
)