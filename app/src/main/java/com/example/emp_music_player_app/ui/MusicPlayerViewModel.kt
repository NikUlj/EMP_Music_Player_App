package com.example.emp_music_player_app.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emp_music_player_app.MediaService
import com.example.emp_music_player_app.data.database.Album
import com.example.emp_music_player_app.data.database.Artist
import com.example.emp_music_player_app.data.repository.MusicRepository
import com.example.emp_music_player_app.data.database.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicPlayerViewModel(
    private val musicRepository: MusicRepository
) : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    private var mediaService: MediaService? = null
    private var serviceConnection: ServiceConnection? = null

    private val _isPlaying = mutableStateOf(false)
    val isPlaying = _isPlaying

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> = _currentPosition.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    private var _currentSongIndex = mutableStateOf(0)
    private var _currentAlbumSongs = mutableStateListOf<Song>()

    private var currentAlbumImagePath: String? = null

    private fun setCurrentAlbumImagePath(path: String?) {
        currentAlbumImagePath = path
    }

    init {
        instance = this
    }

    companion object {
        @Volatile
        var instance: MusicPlayerViewModel? = null
            private set
    }

    fun getCurrentAlbumImagePath(): String? = currentAlbumImagePath

    fun bindMediaService(context: Context) {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MediaService.MediaServiceBinder
                mediaService = binder.getService()

                viewModelScope.launch {
                    mediaService?.isPlaying?.collect { isPlaying ->
                        _isPlaying.value = isPlaying
                    }
                }

                viewModelScope.launch {
                    mediaService?.currentPosition?.collect { position ->
                        _currentPosition.value = position
                        mediaService?.updatePlaybackState(position.toLong())
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mediaService = null
            }
        }

        val intent = Intent(context, MediaService::class.java)
        context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    fun seekTo(position: Float) {
        mediaService?.seekTo(position)
        _currentPosition.value = position
    }

    fun setCurrentPlaylist(songs: List<Song>, initialSongIndex: Int = 0) {
        _currentPlaylist.value = songs.toList()
        _currentSongIndex.value = initialSongIndex
        _currentSong.value = songs.getOrNull(initialSongIndex)
        mediaService?.let { service ->
            if (_isPlaying.value) {
                service.pauseMusic()
            }
        }
        _isPlaying.value = false
        _currentPosition.value = 0f
    }

    fun playMusic(context: Context, uri: String, title: String, duration: Long, albumImagePath: String?) {
        _currentSong.value?.let {
            if (mediaService == null) {
                bindMediaService(context)
            }
            setCurrentAlbumImagePath(albumImagePath)
            mediaService?.playMusic(uri, title, duration, albumImagePath)
        }
    }

    fun pauseMusic(context: Context) {
        mediaService?.pauseMusic()
    }

    fun nextSong(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        _currentSongIndex.value = (_currentSongIndex.value + 1) % playlist.size
        _currentSong.value = playlist[_currentSongIndex.value]

        _currentSong.value?.let { song ->
            playMusic(context, song.uri, song.title, song.duration, currentAlbumImagePath)
        }
    }

    fun previousSong(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        _currentSongIndex.value = (_currentSongIndex.value - 1 + playlist.size) % playlist.size
        _currentSong.value = playlist[_currentSongIndex.value]

        _currentSong.value?.let { song ->
            playMusic(context, song.uri, song.title, song.duration, currentAlbumImagePath)
        }
    }

    fun toggleIsPlaying() {
        _isPlaying.value = !_isPlaying.value
    }

    fun getAllArtists(): Flow<List<Artist>> {
        return musicRepository.getAllArtists()
    }

    fun getMusicFiles() {
        viewModelScope.launch {
            musicRepository.getMusicFiles()
        }
    }

    fun getAlbumsByArtist(artistId: Long): Flow<List<Album>> {
        return musicRepository.getAlbumsByArtist(artistId)
    }

    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return musicRepository.getSongsByAlbum(albumId)
    }

    fun getAlbumById(albumId: Long): Flow<Album?> {
        return musicRepository.getAlbumById(albumId)
    }

    override fun onCleared() {
        super.onCleared()
        if (instance == this) {
            instance = null
        }
        serviceConnection?.let {
            mediaService?.stopSelf()
        }
    }
}