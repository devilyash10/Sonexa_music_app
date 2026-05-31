package com.example.sonexa.service

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sonexa.data.local.SonexaDatabase
import com.example.sonexa.data.local.SongStatsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AudioService : MediaSessionService() {

    companion object {
        var activeAudioSessionId: Int = 0
    }

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    // Service-level lifecycle scope for database writes
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    activeAudioSessionId = player.audioSessionId
                }
            }

            // 🚨 TRACK TRANSITIONS: Fires whenever a new song starts playing
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                val currentMediaId = mediaItem?.mediaId?.toLongOrNull() ?: return

                serviceScope.launch {
                    val database = SonexaDatabase.getDatabase(this@AudioService)
                    val statsDao = database.songStatsDao()
                    val currentTimestamp = System.currentTimeMillis()

                    // Check if a entry already exists for this song
                    val existingStats = statsDao.getStatsForSong(currentMediaId)

                    if (existingStats == null) {
                        // First time playing this track
                        statsDao.insertOrUpdateStats(
                            SongStatsEntity(
                                mediaId = currentMediaId,
                                playCount = 1,
                                lastPlayedTimestamp = currentTimestamp
                            )
                        )
                    } else {
                        // Increment play count and update the timestamp
                        statsDao.recordPlay(currentMediaId, currentTimestamp)
                    }
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}