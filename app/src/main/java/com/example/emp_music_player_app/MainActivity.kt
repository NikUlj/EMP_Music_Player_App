package com.example.emp_music_player_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.emp_music_player_app.ui.theme.EMP_Music_Player_AppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MusicPlayerViewModel by viewModels()

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.all { it.value }) {
            loadMusic()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (hasPermissions()) {
            loadMusic()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun hasPermissions(): Boolean {
        val ok = requiredPermissions.all {
            val granted = ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            Log.d("MusicPlayer", "Permission $it granted: $granted")
            granted
        }
        Log.d("MusicPlayer", "Permissions granted: $ok")
        return ok
    }

    private fun loadMusic() {
        Log.d("MusicPlayer", "Loading music...")
        val songs = getMusicFiles()
        Log.d("MusicPlayer", "Found ${songs.size} songs")
        viewModel.loadSongs(songs)
        setContent {
            EMP_Music_Player_AppTheme {
                MusicPlayerUI(viewModel)
//                ArtistSelectScreen(
//                    viewModel,
//                    onArtistSelected = {Unit}
//                )
            }
        }
    }

    private fun getMusicFiles(): List<Song> {
        val contentResolver = this.contentResolver
        val songs = mutableListOf<Song>()

//        try {
//            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//            intent.data = Uri.parse("file:///storage/emulated/0/Music/")
//            context.sendBroadcast(intent)
//            Thread.sleep(1000)
//        } catch (e: Exception) {
//            Log.e("MusicPlayer", "Error scanning media: ${e.message}")
//        }

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

                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        songs.add(
                            Song(
                                id = id,
                                title = name,
                                artist = artist,
                                album = album,
                                duration = duration,
                                uri = uri
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("MusicPlayer", "Error processing song entry: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error querying music files: ${e.message}")
            e.printStackTrace()
        }

        Log.d("MusicPlayer", "Final song list size: ${songs.size}")
        return songs
    }
}

@Composable
fun MusicPlayerUI(viewModel: MusicPlayerViewModel) {
    val songIndex by viewModel.songIndex
    val isPlaying by viewModel.isPlaying
    val songs by viewModel.songs
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_album_placeholder),
            contentDescription = "Album art",
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp)
        )
        Text(
            text = songs[songIndex].title,
            style = MaterialTheme.typography.displaySmall,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = songs[songIndex].artist,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                viewModel.stopMusic()
                viewModel.previousSong()
                viewModel.switchMusic(context, songs[songIndex].uri)
                if (isPlaying) viewModel.playMusic(context, songs[songIndex].uri)
            }) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous song button",
                    Modifier.size(50.dp)
                )
            }
            IconButton(onClick = {
                if (isPlaying) {
                    viewModel.pauseMusic()
                } else {
                    viewModel.playMusic(context, songs[songIndex].uri)
                }
                viewModel.toggleIsPlaying()
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Pause/Play button",
                    Modifier.size(50.dp)
                )
            }
            IconButton(
                onClick = {
                    viewModel.stopMusic()
                    viewModel.nextSong()
                    viewModel.switchMusic(context, songs[songIndex].uri)
                    if (isPlaying) viewModel.playMusic(context, songs[songIndex].uri)
                }) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next song button",
                    Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
fun ArtistSelectScreen(
    viewModel: MusicPlayerViewModel,
    onArtistSelected: (String) -> Unit
) {
//    val artists = viewModel.getArtists()
    val artists = listOf(
        "Daft Punk",
        "Adele",
        "Taylor Swift",
        "Ed Sheeran",
        "Drake",
        "Billie Eilish",
        "The Weeknd",
        "Kendrick Lamar",
        "Post Malone",
        "Kanye West",
        "Rihanna",
        "Coldplay",
        "Beyoncé"
    )

    Scaffold { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(artists) { artist ->
                ArtistCard(
                    artistName = artist,
                    onClick = { onArtistSelected(artist) }
                )
            }
        }
    }
}

@Composable
fun ArtistCard(
    artistName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
            .size(150.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_album_placeholder),
                contentDescription = "Artist Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = artistName,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .background(Color.Transparent)
            )
        }
    }
}

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri
)