package com.example.emp_music_player_app

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.IntState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

typealias ArtistAlbumSongMap = Map<String, Map<String, List<Song>>>

class MusicPlayerViewModel : ViewModel() {
    private var _songIndex = mutableIntStateOf(0)
    val songIndex: IntState = _songIndex

    private var _isPlaying = mutableStateOf(false)
    val isPlaying = _isPlaying

    private var _organizedSongs = mutableStateOf<ArtistAlbumSongMap>(emptyMap())
    val organizedSongs: State<ArtistAlbumSongMap> = _organizedSongs

    private var _songs = mutableStateOf<List<Song>>(emptyList())
    val songs: State<List<Song>> = _songs

    private var mediaPlayer: MediaPlayer? = null

    fun playMusic(context: Context, songUri: Uri) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, songUri)
                prepare()
            }
        }
        mediaPlayer?.start()
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
    }

    fun switchMusic(context: Context, songUri: Uri) {
        mediaPlayer?.setDataSource(context, songUri)
        mediaPlayer?.prepare()
    }

    fun nextSong() {
        _songIndex.intValue = (_songIndex.intValue + 1) % _songs.value.size
    }

    fun previousSong() {
        _songIndex.intValue = (songIndex.intValue - 1 + _songs.value.size) % _songs.value.size
    }

    fun toggleIsPlaying() {
        _isPlaying.value = !_isPlaying.value
    }

    fun loadSongs(songs: List<Song>) {
        _songs.value = songs
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}