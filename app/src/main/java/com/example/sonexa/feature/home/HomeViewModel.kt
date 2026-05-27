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

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository(application)

    // 1. Initialize our new remote control
    private val audioController = AudioController(application)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

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
                // Update our UI states with the real data from ExoPlayer
                _isPlaying.value = audioController.isPlaying()
                _currentPosition.value = audioController.getCurrentPosition()
                _totalDuration.value = audioController.getDuration()

                delay(500L) // Wait half a second, then check again!
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
        audioController.playSong(song)
    }

    fun pauseSong() {
        audioController.pause()
    }

    fun resumeSong() {
        audioController.resume()
    }
    // NEW: Allow the user to drag the progress bar
    fun seekTo(position: Long) = audioController.seekTo(position)

    override fun onCleared() {
        super.onCleared()
        // 3. Clean up the connection when the ViewModel dies
        audioController.release()
    }
}