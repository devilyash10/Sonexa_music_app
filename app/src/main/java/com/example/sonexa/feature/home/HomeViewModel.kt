package com.example.sonexa.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonexa.core.media.AudioController
import com.example.sonexa.data.repository.AudioRepository
import com.example.sonexa.model.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.Player
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.sonexa.data.local.FavoriteSongEntity
import com.example.sonexa.data.local.SonexaDatabase
import com.example.sonexa.data.local.PlaylistEntity
import com.example.sonexa.data.local.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.sonexa.core.util.LyricLine
import com.example.sonexa.core.util.LrcParser
import com.example.sonexa.data.local.SavedSongEntity
import com.example.sonexa.feature.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Dispatchers

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)
    private val repository = AudioRepository(application)
    private val audioController = AudioController(application)

    private val database = SonexaDatabase.getDatabase(application)
    private val favoriteDao = database.favoriteDao()
    private val playlistDao = database.playlistDao()
    private val songStatsDao = database.songStatsDao()

    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteSongIds: StateFlow<List<Long>> = favoriteDao.getAllFavoriteSongIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = favoriteDao.getFavoriteSongsWithData()
        .map { cachedList ->
            cachedList.map { cached ->
                Song(
                    id = cached.songId,
                    title = cached.title,
                    artist = cached.artist,
                    mediaUri = cached.mediaUri,
                    artworkUri = cached.artworkUri
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    private val _mostPlayed = MutableStateFlow<List<Song>>(emptyList())
    val mostPlayed: StateFlow<List<Song>> = _mostPlayed.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(_songs, _searchQuery) { songList, query ->
        if (query.isBlank()) {
            songList
        } else {
            songList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _currentLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val currentLyrics: StateFlow<List<LyricLine>> = _currentLyrics.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(-1)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    private fun loadMockLyrics() {
        val mockLrc = """
            [00:15.00]Yeah
            [00:16.50]I've been tryna call
            [00:19.00]I've been on my own for long enough
            [00:23.00]Maybe you can show me how to love, maybe
            [00:29.00]I'm going through withdrawals
            [00:32.00]You don't even have to do too much
            [00:35.50]You can turn me on with just a touch, baby
            [00:39.00]I look around and Sin City's cold and empty
            [00:43.00]No one's around to judge me
            [00:45.50]I can't see clearly when you're gone
            [00:50.00]I said, ooh, I'm blinded by the lights
            [00:55.50]No, I can't sleep until I feel your touch
        """.trimIndent()
        _currentLyrics.value = LrcParser.parse(mockLrc)
    }

    init {
        viewModelScope.launch {
            settingsManager.smartScanFlow.collectLatest {
                loadLocalAudioFiles()
            }
        }

        viewModelScope.launch {
            combine(_songs, songStatsDao.getAllSongStats()) { masterList, statsList ->
                Pair(masterList, statsList)
            }.collect { (masterList, statsList) ->
                val statsMap = statsList.associateBy { it.mediaId }
                _recentlyPlayed.value = masterList
                    .filter { statsMap.containsKey(it.id) && (statsMap[it.id]?.lastPlayedTimestamp ?: 0L) > 0L }
                    .sortedByDescending { statsMap[it.id]?.lastPlayedTimestamp ?: 0L }
                    .take(10)

                _mostPlayed.value = masterList
                    .filter { statsMap.containsKey(it.id) && (statsMap[it.id]?.playCount ?: 0) > 0 }
                    .sortedByDescending { statsMap[it.id]?.playCount ?: 0 }
                    .take(10)
            }
        }

        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        viewModelScope.launch {
            while (true) {
                val currentlyPlaying = audioController.isPlaying()
                _isPlaying.value = currentlyPlaying

                // UI scrubber updates correctly if the user seeks while paused!
                _currentPosition.value = audioController.getCurrentPosition()
                _totalDuration.value = audioController.getDuration()

                val currentPos = _currentPosition.value
                val lyrics = _currentLyrics.value
                if (lyrics.isNotEmpty()) {
                    val activeIndex = lyrics.indexOfLast { it.timeMs <= currentPos }
                    _activeLyricIndex.value = if (activeIndex != -1) activeIndex else 0
                }

                _isShuffleEnabled.value = audioController.isShuffleEnabled()
                _repeatMode.value = audioController.getRepeatMode()

                val mediaId = audioController.getCurrentMediaId()
                val mediaItem = audioController.getCurrentMediaItem()
                if (mediaId != null && mediaItem != null) {
                    val songId = mediaId.toLongOrNull() ?: 0L
                    val localSong = _songs.value.find { it.id == songId }
                    val activeSong = localSong ?: Song(
                        id = songId,
                        title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown Track",
                        artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                        mediaUri = mediaItem.localConfiguration?.uri?.toString() ?: "",
                        artworkUri = mediaItem.mediaMetadata.artworkUri?.toString() ?: ""
                    )
                    _currentSong.value = activeSong
                }

                // Sleep for 500ms if playing for a smooth UI, or 1000ms if paused to save battery
                delay(if (currentlyPlaying) 500L else 1000L)
            }
        }
    }

    fun loadLocalAudioFiles() {
        // 🚨 PERFORMANCE FIX 1: Move scanning to IO thread and utilize the Repository
        viewModelScope.launch(Dispatchers.IO) {
            val isSmartScanOn = settingsManager.smartScanFlow.first()
            val allSongs = repository.getAudioFiles()

            val filteredSongs = if (isSmartScanOn) {
                allSongs.filterNot { song ->
                    val path = song.mediaUri.lowercase()
                    path.contains("whatsapp") ||
                            path.contains("telegram") ||
                            path.contains("voice recorder") ||
                            path.contains("snapchat")
                }
            } else {
                allSongs
            }

            // Push the final clean list to the UI
            _songs.value = filteredSongs
        }
    }

    fun playSong(song: Song) {
        val currentSongs = _songs.value
        val startIndex = currentSongs.indexOf(song)
        if (startIndex != -1) {
            audioController.playQueue(currentSongs, startIndex)
        }
    }

    fun playOnlineSong(song: Song) {
        _currentSong.value = song
        val onlineQueue = listOf(song)
        audioController.playQueue(onlineQueue, 0)
    }

    fun playFromQueue(song: Song, queue: List<Song>) {
        _currentSong.value = song
        loadMockLyrics()
        val startIndex = queue.indexOfFirst { it.id == song.id }.takeIf { it != -1 } ?: 0
        audioController.playQueue(queue, startIndex)
    }

    fun toggleShuffle() {
        val newShuffleState = !_isShuffleEnabled.value
        audioController.toggleShuffle(newShuffleState)
    }

    fun cycleRepeatMode() {
        val newMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        audioController.setRepeatMode(newMode)
    }

    fun pauseSong() = audioController.pause()
    fun resumeSong() = audioController.resume()
    fun seekTo(position: Long) = audioController.seekTo(position)
    fun skipToNext() = audioController.skipToNext()
    fun skipToPrevious() = audioController.skipToPrevious()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isCurrentlyFavorite = favoriteSongIds.value.contains(song.id)
            val entity = FavoriteSongEntity(songId = song.id)
            if (isCurrentlyFavorite) {
                favoriteDao.deleteFavorite(entity)
            } else {
                val cachedSong = SavedSongEntity(song.id, song.title, song.artist, song.mediaUri, song.artworkUri)
                favoriteDao.cacheSong(cachedSong)
                favoriteDao.insertFavorite(entity)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioController.release()
    }

    fun shuffleAndPlayAll() {
        val currentList = _songs.value
        if (currentList.isNotEmpty()) {
            if (!_isShuffleEnabled.value) toggleShuffle()
            playSong(currentList.random())
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            val cachedSong = SavedSongEntity(song.id, song.title, song.artist, song.mediaUri, song.artworkUri)
            playlistDao.cacheSong(cachedSong)
            playlistDao.insertSongIntoPlaylist(
                PlaylistSongCrossRef(playlistId = playlistId, songId = song.id)
            )
        }
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylistWithData(playlistId).map { cachedList ->
            cachedList.map { cached ->
                Song(
                    id = cached.songId,
                    title = cached.title,
                    artist = cached.artist,
                    mediaUri = cached.mediaUri,
                    artworkUri = cached.artworkUri
                )
            }
        }
    }
}