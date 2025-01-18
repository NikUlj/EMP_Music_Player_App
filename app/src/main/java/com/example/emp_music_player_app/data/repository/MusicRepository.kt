package com.example.emp_music_player_app.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.emp_music_player_app.data.ImageStorage
import com.example.emp_music_player_app.data.database.Album
import com.example.emp_music_player_app.data.database.Artist
import com.example.emp_music_player_app.data.database.MusicDao
import com.example.emp_music_player_app.data.database.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao,
    private val coroutineScope: CoroutineScope,
    private val spotifyRepository: SpotifyRepository = SpotifyRepository(),
    private val imageStorage: ImageStorage = ImageStorage(context)
) {
    private val contentResolver = context.contentResolver


    private suspend fun fetchAndSaveArtistImage(artistName: String): String? {
        val imageUrl = spotifyRepository.getArtistImage(artistName)
        return if (imageUrl != null) {
            imageStorage.saveImageFromUrl(imageUrl, "artist")
        } else null
    }

    private suspend fun fetchAndSaveAlbumImage(albumName: String, artistName: String): String? {
        val imageUrl = spotifyRepository.getAlbumImage(albumName, artistName)
        return if (imageUrl != null) {
            imageStorage.saveImageFromUrl(imageUrl, "album")
        } else null
    }

    suspend fun getMusicFiles(): List<Song> {
        val contentResolver = this.contentResolver
        val songs = mutableListOf<Song>()

        try {
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.parse("file:///storage/emulated/0/Music/")
            context.sendBroadcast(intent)
            Thread.sleep(1000)
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error scanning media: ${e.message}")
        }

        val cols = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE
        )

        try {
            Log.d("MusicPlayer", "Starting music query...")

            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cols,
                null,
                null,
                "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
            )

            Log.d("MusicPlayer", "Cursor obtained. Count: ${cursor?.count ?: 0}")

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                while (c.moveToNext()) {
                    try {
                        val id = c.getLong(idCol)
                        val name = c.getString(nameCol).substringBeforeLast(".")
                        val artist = c.getString(artistCol) ?: "Unknown Artist"
                        val album = c.getString(albumCol) ?: "Unknown Album"
                        val duration = c.getLong(durationCol)
                        val path = c.getString(dataCol)
                        val mime = c.getString(mimeCol)

                        Log.d("MusicPlayer", """
                        Found audio file:
                        - ID: $id
                        - Name: $name
                        - Path: $path
                        - Artist: $artist
                        - Album: $album
                        - MIME Type: $mime
                        - Duration: $duration
                    """.trimIndent())

                        val artistId = getArtistId(artist) ?: addArtistToDB(artist)
                        val albumId = getAlbumId(album) ?: addAlbumToDB(album, artistId)

                        val songId = getSongId(name)
                        if (songId == null)
                            addSongToDB(song = name, duration = duration, uri = path, albumId = albumId)
                        else {
                            replaceSongToDB(songId = songId, song = name, duration = duration, uri = path, albumId = albumId)
                        }

//                        val uri = ContentUris.withAppendedId(
//                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                            id
//                        )

//                        songs.add(
//                            Song(
//                                sindId = id,
//                                title = name,
//                                artist = artist,
//                                album = album,
//                                duration = duration,
//                                uri = uri
//                            )
//                        )
                    } catch (e: Exception) {
                        Log.e("MusicPlayer", "Error processing song entry: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error querying music files: ${e.message}")
            e.printStackTrace()
        }

//        Log.d("MusicPlayer", "Final song list size: ${songs.size}")
        return songs
    }

    private suspend fun addArtistToDB(artist: String): Long {
        val imagePath = fetchAndSaveArtistImage(artist)
        return musicDao.insertArtist(Artist(name = artist, imagePath = imagePath))
    }

    private suspend fun addAlbumToDB(album: String, artistId: Long): Long {
        val artist = musicDao.getArtistById(artistId)
        val imagePath = fetchAndSaveAlbumImage(album, artist?.name ?: "Unknown Artist")
        return musicDao.insertAlbum(Album(name = album, artistId = artistId, imagePath = imagePath))
    }

    private suspend fun addSongToDB(song: String, duration: Long, uri: String, albumId: Long): Long {
            return musicDao.insertSong(Song(title = song, duration = duration, uri = uri, albumId = albumId))
    }

    private suspend fun replaceSongToDB(songId: Long, song: String, duration: Long, uri: String, albumId: Long) {
        musicDao.insertSong(Song(songId = songId,title = song, duration = duration, uri = uri, albumId = albumId))
    }

    private suspend fun getArtistId(artist: String): Long? {
        return withContext(Dispatchers.IO) {
            musicDao.getArtistIdByName(artist)
        }
    }

    private suspend fun getAlbumId(album: String): Long? {
        return withContext(Dispatchers.IO) {
            musicDao.getAlbumIdByName(album)
        }
    }

    private suspend fun getSongId(song: String): Long? {
        return withContext(Dispatchers.IO) {
            musicDao.getSongIdByTitle(song)
        }
    }

    fun getAllArtists(): Flow<List<Artist>> {
        return musicDao.getAllArtists()
    }

    fun getAlbumsByArtist(artistId: Long): Flow<List<Album>> {
        return musicDao.getAlbumsByArtist(artistId)
    }

    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return musicDao.getSongsByAlbum(albumId)
    }

    fun getAlbumById(albumId: Long): Flow<Album?> {
        return musicDao.getAlbumById(albumId)
    }
}