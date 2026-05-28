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

    fun playQueue(songs: List<Song>, startIndex: Int) {
        val mediaItems = songs.map { song ->
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .build()

            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.mediaUri)
                .setMediaMetadata(mediaMetadata)
                .build()
        }

        mediaController?.let { controller ->
            // Give ExoPlayer the whole list, tell it where to start, and start at 0:00
            controller.setMediaItems(mediaItems, startIndex, 0L)
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
    // 2. ADD THESE NEW QUEUE METHODS AT THE BOTTOM
    fun toggleShuffle(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
    }

    fun setRepeatMode(repeatMode: Int) {
        // repeatMode uses Player.REPEAT_MODE_OFF, REPEAT_MODE_ALL, or REPEAT_MODE_ONE
        mediaController?.repeatMode = repeatMode
    }

    fun getCurrentMediaId(): String? {
        // This tells us exactly which song ExoPlayer is currently playing
        return mediaController?.currentMediaItem?.mediaId
    }

    fun isShuffleEnabled(): Boolean {
        return mediaController?.shuffleModeEnabled ?: false
    }

    fun getRepeatMode(): Int {
        return mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
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