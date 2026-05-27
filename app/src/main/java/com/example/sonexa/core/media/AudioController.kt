package com.example.sonexa.core.media

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sonexa.model.Song
import com.example.sonexa.service.AudioService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class AudioController(context: Context) {

    private var mediaControllerFuture: ListenableFuture<MediaController>
    private var mediaController: MediaController? = null

    init {
        // 1. Create a token that points to our AudioService
        val sessionToken = SessionToken(context, ComponentName(context, AudioService::class.java))

        // 2. Request a connection to the service asynchronously
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        // 3. When the connection is successful, store the controller
        mediaControllerFuture.addListener(
            { mediaController = mediaControllerFuture.get() },
            MoreExecutors.directExecutor()
        )
    }

    // --- PLAYBACK COMMANDS ---

    fun playSong(song: Song) {
        // Production Trick: We add metadata here so the Android Lock Screen
        // knows the title and artist of the song playing!
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.mediaUri)
            .setMediaMetadata(mediaMetadata)
            .build()

        mediaController?.let { controller ->
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    // --- NEW GETTERS AND SEEKER ---

    fun isPlaying(): Boolean {
        return mediaController?.isPlaying ?: false
    }

    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return mediaController?.duration?.coerceAtLeast(0L) ?: 0L
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    // Clean up to prevent memory leaks when the app is completely closed
    fun release() {
        MediaController.releaseFuture(mediaControllerFuture)
    }
}