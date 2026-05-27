package com.example.sonexa.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonexa.data.repository.AudioRepository
import com.example.sonexa.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// We use AndroidViewModel because we need the Application context to pass to our Repository
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository(application)

    // This holds our UI state. It starts as an empty list.
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    fun loadLocalAudioFiles() {
        // viewModelScope ensures this coroutine is cancelled if the ViewModel is destroyed
        viewModelScope.launch {
            val localSongs = repository.getAudioFiles()
            _songs.value = localSongs
        }
    }
}