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
import com.example.sonexa.service.AudioService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioController(private val context: Context) {

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    val audioSessionId: Int
        get() = AudioService.activeAudioSessionId

    init {
        initController()
    }

    private fun initController() {
        val sessionToken = SessionToken(context, ComponentName(context, AudioService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
        }, MoreExecutors.directExecutor())
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        // 🚨 FIX: Shift the heavy list mapping to a background CPU thread to prevent UI freezing
        CoroutineScope(Dispatchers.Default).launch {
            val mediaItems = songs.map { song ->
                MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(song.mediaUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setArtworkUri(Uri.parse(song.artworkUri))
                            .build()
                    )
                    .build()
            }

            // Jump back to the Main thread to interact with the UI/ExoPlayer components
            withContext(Dispatchers.Main) {
                // 🚨 FIX: Race Condition handling. If controller is ready, play. If not, wait for it!
                if (mediaController != null) {
                    mediaController?.setMediaItems(mediaItems, startIndex, 0)
                    mediaController?.prepare()
                    mediaController?.play()
                } else {
                    mediaControllerFuture?.addListener({
                        val controller = mediaControllerFuture?.get()
                        controller?.setMediaItems(mediaItems, startIndex, 0)
                        controller?.prepare()
                        controller?.play()
                    }, MoreExecutors.directExecutor())
                }
            }
        }
    }

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
    fun getCurrentMediaItem() = mediaController?.currentMediaItem

    fun release() {
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}