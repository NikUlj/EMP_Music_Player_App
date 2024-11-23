package com.example.emp_music_player_app

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.emp_music_player_app.ui.theme.EMP_Music_Player_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerUI()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MusicPlayerUI() {
    var songIndex by remember { mutableIntStateOf(0) }
    val songList = listOf(
        Song("Song title", "Artist", R.drawable.ic_album_placeholder)
    )
    val currentSong = songList[songIndex]

    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier

            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (currentSong.albumArtId != null) {
            Image(
                painter = painterResource(id = currentSong.albumArtId),
                contentDescription = "Album art",
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            )
        }
        Text(
            text = songList[songIndex].title,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(top = 16.dp)
        )
        Text(
            text = songList[songIndex].artist,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                    songIndex = (songIndex - 1 + songList.size) % songList.size
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous song button",
                    Modifier.size(50.dp)
                )
            }
            IconButton(onClick = {
                isPlaying = !isPlaying
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause button" else "Play button",
                    Modifier.size(50.dp)
                )
            }
            IconButton(onClick = {
                songIndex = (songIndex + 1) % songList.size
            }
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next song button",
                    Modifier.size(50.dp)
                )
            }
        }
    }
}

//fun getMusicFiles(contentResolver: ContentResolver): List<Song> {
//    val songList = mutableListOf<Song>()
//
//    val projection = arrayOf(
//        MediaStore.Audio.Media._ID,
//        MediaStore.Audio.Media.DISPLAY_NAME,
//        MediaStore.Audio.Media.ARTIST,
//        MediaStore.Audio.Media.ALBUM,
//        MediaStore.Audio.Media.DURATION
//    )
//
//    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
//
//    val cursor = contentResolver.query(
//        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//        projection,
//        selection,
//        null,
//        MediaStore.Audio.Media.DISPLAY_NAME
//    )
//
//    cursor?.use {
//        val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
//        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
//        val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
//        val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
//        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
//
//        while (it.moveToNext()) {
//            val id = it.getLong(idColumn)
//            val name = it.getString(nameColumn)
//            val artist = it.getString(artistColumn)
//            val album = it.getString(albumColumn)
//            val duration = it.getLong(durationColumn)
//
//            val contentUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
//                .appendPath(id.toString())
//                .build()
//
//            songList.add(
//                Song(
//                    id = id,
//                    title = name,
//                    artist = artist,
//                    album = album,
//                    duration = duration,
//                    uri = contentUri,
//                    albumArtId = null
//                )
//            )
//        }
//    }
//}

data class Song(
//    val id: Long,
    val title: String,
    val artist: String,
//    val album: String,
//    val duration: Long,
//    val uri: Uri,
    val albumArtId: Int?
)