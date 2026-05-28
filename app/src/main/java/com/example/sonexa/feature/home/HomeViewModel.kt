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

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository(application)

    // 1. Initialize our new remote control
    private val audioController = AudioController(application)

    // 1. INITIALIZE THE DATABASE DAO
    private val favoriteDao = SonexaDatabase.getDatabase(application).favoriteDao()
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    // 2. NEW STATE: A list of all liked song IDs.
    // Because Room returns a Flow, this will automatically update the UI whenever the database changes!
    val favoriteSongIds: StateFlow<List<Long>> = favoriteDao.getAllFavoriteSongIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 1. NEW: Hold the current text the user is typing
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 2. NEW: Automatically filter the songs!
    // This watches BOTH _songs and _searchQuery. If either changes, it instantly recalculates.
    val filteredSongs: StateFlow<List<Song>> = combine(_songs, _searchQuery) { songList, query ->
        if (query.isBlank()) {
            songList // If search is empty, show all songs
        } else {
            songList.filter {
                // Search by both Title and Artist!
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 3. NEW: A function for the UI to call when the user types
    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // 1. NEW STATE: The ViewModel now tracks the current song globally
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    // 2. NEW STATES: Shuffle and Repeat
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    // --- NEW REAL PLAYBACK STATES ---
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    init {
        // Start polling the ExoPlayer state as soon as the ViewModel is created
        updatePlaybackState()
    }
    private fun updatePlaybackState() {
        viewModelScope.launch {
            while (true) {
                _isPlaying.value = audioController.isPlaying()
                _currentPosition.value = audioController.getCurrentPosition()
                _totalDuration.value = audioController.getDuration()

                // 3. NEW POLLING LOGIC: Sync the UI exactly with ExoPlayer's internal queue
                _isShuffleEnabled.value = audioController.isShuffleEnabled()
                _repeatMode.value = audioController.getRepeatMode()

                val mediaId = audioController.getCurrentMediaId()
                if (mediaId != null) {
                    // Find the song in our list that matches the ID currently playing
                    _currentSong.value = _songs.value.find { it.id.toString() == mediaId }
                }

                delay(500L)
            }
        }
    }

    fun loadLocalAudioFiles() {
        viewModelScope.launch {
            _songs.value = repository.getAudioFiles()
        }
    }

    // 2. Expose playback methods to the UI
    fun playSong(song: Song) {
        val currentSongs = _songs.value
        val startIndex = currentSongs.indexOf(song)
        if (startIndex != -1) {
            audioController.playQueue(currentSongs, startIndex)
        }
    }

    // 5. ADD QUEUE CONTROL METHODS
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

    fun pauseSong() {
        audioController.pause()
    }

    fun resumeSong() {
        audioController.resume()
    }
    // NEW: Allow the user to drag the progress bar
    fun seekTo(position: Long) = audioController.seekTo(position)

    fun skipToNext() = audioController.skipToNext()
    fun skipToPrevious() = audioController.skipToPrevious()

    // 3. NEW FUNCTIONS: The UI will call this when the user clicks the Heart icon
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isCurrentlyFavorite = favoriteSongIds.value.contains(song.id)
            val entity = FavoriteSongEntity(songId = song.id)

            if (isCurrentlyFavorite) {
                // If it's already liked, remove it
                favoriteDao.deleteFavorite(entity)
            } else {
                // If it's not liked, add it
                favoriteDao.insertFavorite(entity)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 3. Clean up the connection when the ViewModel dies
        audioController.release()
    }
    // NEW: Kickstart a shuffled playlist from the Home Screen
    fun shuffleAndPlayAll() {
        val currentList = _songs.value
        if (currentList.isNotEmpty()) {
            // 1. Force ExoPlayer's shuffle mode ON
            if (!_isShuffleEnabled.value) {
                toggleShuffle()
            }
            // 2. Pick a random song to start the queue
            playSong(currentList.random())
        }
    }
}