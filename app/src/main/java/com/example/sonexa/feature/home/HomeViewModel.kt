package com.example.sonexa.feature.home

import android.app.Application
import android.content.ContentUris
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
import android.net.Uri
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.sonexa.core.util.LyricLine
import com.example.sonexa.core.util.LrcParser
import com.example.sonexa.data.local.SavedSongEntity
import com.example.sonexa.feature.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)
    private val repository = AudioRepository(application)

    // 1. Initialize our new remote control
    private val audioController = AudioController(application)

    // 1. INITIALIZE THE DATABASE DAO
    private val favoriteDao = SonexaDatabase.getDatabase(application).favoriteDao()

    // INITIALIZE PLAYLIST DAO
    private val playlistDao = SonexaDatabase.getDatabase(application).playlistDao()

    // NEW STATE: List of all custom playlists (Updates automatically!)
    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    init {
        // 🚨 OBSERVE THE FLOW: Whenever the user flips the toggle, this auto-triggers!
        viewModelScope.launch {
            settingsManager.smartScanFlow.collectLatest { isSmartScanOn ->
                loadLocalAudioFiles(isSmartScanOn)
            }
        }
    }
    // 2. NEW STATE: A list of all liked song IDs.
    // Because Room returns a Flow, this will automatically update the UI whenever the database changes!
    val favoriteSongIds: StateFlow<List<Long>> = favoriteDao.getAllFavoriteSongIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    // NEW STATE: Grabs the full song data for the Favorites screen!
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
        // Start polling the ExoPlayer state as soon as the ViewModel is created
        updatePlaybackState()
    }
    private fun updatePlaybackState() {
        viewModelScope.launch {
            while (true) {
                _isPlaying.value = audioController.isPlaying()
                _currentPosition.value = audioController.getCurrentPosition()
                _totalDuration.value = audioController.getDuration()

                //THE LYRICS SYNC ENGINE
                val currentPos = _currentPosition.value
                val lyrics = _currentLyrics.value
                if (lyrics.isNotEmpty()) {
                    // Find the last lyric line whose timestamp is BEFORE or EQUAL to our current position
                    val activeIndex = lyrics.indexOfLast { it.timeMs <= currentPos }
                    _activeLyricIndex.value = if (activeIndex != -1) activeIndex else 0
                }

                // 3. NEW POLLING LOGIC: Sync the UI exactly with ExoPlayer's internal queue
                _isShuffleEnabled.value = audioController.isShuffleEnabled()
                _repeatMode.value = audioController.getRepeatMode()

                val mediaId = audioController.getCurrentMediaId()
                val mediaItem = audioController.getCurrentMediaItem() // Reads the raw ExoPlayer data

                if (mediaId != null && mediaItem != null) {
                    val songId = mediaId.toLongOrNull() ?: 0L

                    // 1. Try to find the song in your local storage
                    val localSong = _songs.value.find { it.id == songId }

                    // 2. If it's NOT local, reconstruct it from the ExoPlayer web stream!
                    val activeSong = localSong ?: Song(
                        id = songId,
                        title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown Track",
                        artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                        mediaUri = mediaItem.localConfiguration?.uri?.toString() ?: "",
                        artworkUri = mediaItem.mediaMetadata.artworkUri?.toString() ?: ""
                    )

                    // 3. Update the UI safely so it never goes black!
                    _currentSong.value = activeSong
                }

                delay(500L)
            }
        }
    }
    fun loadLocalAudioFiles() {
        viewModelScope.launch {
            // Read the current setting instantly and pass it to the private engine
            val isSmartScanOn = settingsManager.smartScanFlow.first()
            loadLocalAudioFiles(isSmartScanOn) // Calls the private function below!
        }
    }
    private fun loadLocalAudioFiles(isSmartScanOn: Boolean) {
        try {
            val audioList = mutableListOf<Song>()
            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.RELATIVE_PATH, // Safe for Scoped Storage on Android 11-16
                MediaStore.Audio.Media.ALBUM_ID
            )

            // Baseline check for valid music items
            val selectionClause = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

            val cursor = getApplication<Application>().contentResolver.query(
                collection, projection, selectionClause, null, "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val duration = it.getLong(durationColumn)
                    val relativePath = it.getString(pathColumn) ?: ""

                    if (isSmartScanOn) {
                        val isJunk = relativePath.contains("WhatsApp", ignoreCase = true) ||
                                relativePath.contains("Telegram", ignoreCase = true) ||
                                relativePath.contains("Voice Recorder", ignoreCase = true) ||
                                relativePath.contains("Snapchat", ignoreCase = true)
                        if (isJunk) continue
                    }

                    // 🚨 NEW: Fetch the Album Art ID!
                    val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val albumId = it.getLong(albumIdColumn)

                    // 🚨 NEW: Create the Image Link!
                    val artworkUri = android.net.Uri.parse("content://media/external/audio/albumart")
                        .buildUpon()
                        .appendPath(albumId.toString())
                        .build()
                        .toString()

                    val mediaUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    audioList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            mediaUri = mediaUri.toString(),
                            artworkUri = artworkUri // 🚨 Pass it into the Song here!
                        )
                    )
                }
            }
            _songs.value = audioList

        } catch (e: Exception) {
            e.printStackTrace()
            _songs.value = emptyList()
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
    // 🚨 FIX 2: A dedicated player function for web streams
    fun playOnlineSong(song: Song) {
        // 1. Force the reactive state flow to update so the player view loads the metadata instantly
        _currentSong.value = song

        // 2. Wrap the isolated streaming track in a single-element playback collection
        val onlineQueue = listOf(song)

        // 3. Dispatch directly to your existing background audio architecture
        // Your audioController engine will treat the streaming web link exactly like a local file link!
        audioController.playQueue(onlineQueue, 0)
    }
    fun playFromQueue(song: Song, queue: List<Song>) {
        // 1. Force UI update
        _currentSong.value = song
        loadMockLyrics()

        // 2. Find exactly where this song is in the provided list
        val startIndex = queue.indexOfFirst { it.id == song.id }.takeIf { it != -1 } ?: 0

        // 3. Send the FULL queue to ExoPlayer!
        audioController.playQueue(queue, startIndex)
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
                favoriteDao.deleteFavorite(entity)
            } else {
                // 🚨 NEW: Cache the song data FIRST, then save it as a favorite!
                val cachedSong = SavedSongEntity(song.id, song.title, song.artist, song.mediaUri, song.artworkUri)
                favoriteDao.cacheSong(cachedSong)
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

    // --- PLAYLIST CONTROLS ---
    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) { // 🚨 Note: Changed parameter from songId: Long to song: Song
        viewModelScope.launch {
            // 🚨 NEW: Cache the song data FIRST
            val cachedSong = SavedSongEntity(song.id, song.title, song.artist, song.mediaUri, song.artworkUri)
            playlistDao.cacheSong(cachedSong)

            playlistDao.insertSongIntoPlaylist(
                PlaylistSongCrossRef(playlistId = playlistId, songId = song.id)
            )
        }
    }

    // NEW: Fetches the actual Song objects for a specific playlist
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        // We read the SQL Join from the cache, and map it back to your standard UI Song object!
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