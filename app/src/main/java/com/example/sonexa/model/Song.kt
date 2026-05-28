package com.example.sonexa.model


data class Song(
    val id: Long, // Changed to Long because MediaStore IDs can be large
    val title: String,
    val artist: String,
    val mediaUri: String, // The actual path to the .mp3 file
    val artworkUri: String // The actual path to the album art
)
//data class Song(
//    val id: Int,
//    val title: String,
//    val artist: String
//)