package com.example.sonexa.feature.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonexa.data.remote.MusicRepository
import com.example.sonexa.data.remote.OnlineSongDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnlineViewModel : ViewModel() {

    private val repository = MusicRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<OnlineSongDto>>(emptyList())
    val searchResults: StateFlow<List<OnlineSongDto>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            _isLoading.value = true
            // "English Top Songs" is a great default query for JioSaavn to return a solid home list
            _searchResults.value = repository.searchOnlineSongs("English Top Songs")
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce to prevent API rate limiting
            _isLoading.value = true
            _searchResults.value = repository.searchOnlineSongs(newQuery)
            _isLoading.value = false
        }
    }
}