package com.example.sonexa.data.local

import androidx.room.Entity

@Entity(
    tableName = "playlist_song_cross_ref",
    // Primary keys ensure a song can only be added to the SAME playlist once
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)