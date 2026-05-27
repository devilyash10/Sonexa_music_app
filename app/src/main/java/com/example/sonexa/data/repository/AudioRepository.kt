package com.example.sonexa.data.repository

import android.content.Context
import android.provider.MediaStore
import com.example.sonexa.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioRepository(private val context: Context) {

    // We use Dispatchers.IO because reading from the database is a heavy task
    // and should not freeze the UI thread.
    suspend fun getAudioFiles(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        // 1. Define which columns we want to fetch from the database
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA // This gives us the actual file path
        )

        // 2. Filter: Only get actual music files (no voice notes or ringtones)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        // 3. Sort: Alphabetical by title
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        // 4. Run the query
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            // Find the exact index of each column
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            // Loop through the results and build our Song objects
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                // Some audio files might have missing metadata, so we provide fallbacks
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "<Unknown>"
                val mediaUri = cursor.getString(dataColumn)

                songs.add(Song(id = id, title = title, artist = artist, mediaUri = mediaUri))
            }
        }

        return@withContext songs
    }
}