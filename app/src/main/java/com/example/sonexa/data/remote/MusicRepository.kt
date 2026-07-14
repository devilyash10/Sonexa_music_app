package com.example.sonexa.data.remote

import android.util.Log

class MusicRepository {
    private val apiService = MusicApiService.create()

    suspend fun searchOnlineSongs(query: String): List<OnlineSongDto> {
        return try {
            val response = apiService.searchOnlineSongs(query)

            // Map the complex JioSaavn nested JSON into our clean DTO
            response.data?.results?.map { jioSong ->

                // Extract the highest quality image (usually the last in the array)
                val bestImage = jioSong.image?.lastOrNull()?.link ?: ""

                // Extract the highest quality audio link (usually the last in the array)
                val bestAudio = jioSong.downloadUrl?.lastOrNull()?.link ?: ""

                OnlineSongDto(
                    trackId = jioSong.id.hashCode().toLong(), // Convert String ID to Long
                    trackName = jioSong.title,
                    artistName = jioSong.primaryArtists,
                    artworkUrl = bestImage,
                    previewUrl = bestAudio
                )
            } ?: emptyList()

        } catch (e: Exception) {
            Log.e("MusicRepository", "Network Error: ${e.message}")
            emptyList()
        }
    }
}