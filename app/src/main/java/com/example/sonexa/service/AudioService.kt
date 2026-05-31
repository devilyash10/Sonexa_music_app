package com.example.sonexa.service

//import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class AudioService : MediaSessionService() {
    companion object {
        var activeAudioSessionId: Int = 0
    }
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    // 1. Called when the service is first created
    override fun onCreate() {
        super.onCreate()

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // When ExoPlayer says "I am ready to play music"...
                if (playbackState == Player.STATE_READY) {
                    // ...the hardware audio track is guaranteed to exist. Grab the ID!
                    activeAudioSessionId = player.audioSessionId
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player).build()
    }

    // 2. The system calls this to figure out which session this service is running
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // 3. Called when the service is destroyed (e.g., user force-closes the app)
    override fun onDestroy() {
        mediaSession?.run {
            player.release() // VERY IMPORTANT: Free up system resources!
            release()
        }
        super.onDestroy()
    }
}