package com.example.sonexa.data.remote

import com.google.gson.annotations.SerializedName

// The main response wrapper from iTunes
data class ITunesResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<OnlineSongDto>
)

// The actual song data
data class OnlineSongDto(
    @SerializedName("trackId") val trackId: Long,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("artworkUrl100") val artworkUrl: String?, // 100x100 image
    @SerializedName("previewUrl") val previewUrl: String? // The actual streaming audio link!
)