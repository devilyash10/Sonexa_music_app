package com.example.sonexa.core.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sonexa.model.Song
import com.example.sonexa.service.AudioService // Importing your existing service!
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class AudioController(private val context: Context) {

    // 1. We replace ExoPlayer with a MediaController (The Remote Control)
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    init {
        initController()
    }

    private fun initController() {
        // 2. We connect the Remote Control to the AudioService
        val sessionToken = SessionToken(context, ComponentName(context, AudioService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        // Wait for the connection to succeed
        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
        }, MoreExecutors.directExecutor())
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        // 3. We convert our Song data into ExoPlayer MediaItems AND Metadata
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.mediaUri)
                // THIS METADATA BUILDS THE LOCK SCREEN NOTIFICATION!
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(Uri.parse(song.artworkUri))
                        .build()
                )
                .build()
        }

        mediaController?.setMediaItems(mediaItems, startIndex, 0)
        mediaController?.prepare()
        mediaController?.play()
    }

    // All these methods now safely send commands to the background service!
    fun pause() = mediaController?.pause()
    fun resume() = mediaController?.play()
    fun skipToNext() = mediaController?.seekToNextMediaItem()
    fun skipToPrevious() = mediaController?.seekToPreviousMediaItem()
    fun seekTo(position: Long) = mediaController?.seekTo(position)
    fun toggleShuffle(enabled: Boolean) { mediaController?.shuffleModeEnabled = enabled }
    fun setRepeatMode(mode: Int) { mediaController?.repeatMode = mode }

    fun isPlaying() = mediaController?.isPlaying ?: false
    fun getCurrentPosition() = mediaController?.currentPosition ?: 0L
    fun getDuration() = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
    fun isShuffleEnabled() = mediaController?.shuffleModeEnabled ?: false
    fun getRepeatMode() = mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
    fun getCurrentMediaId() = mediaController?.currentMediaItem?.mediaId

    fun release() {
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}