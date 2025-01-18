package com.example.emp_music_player_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.emp_music_player_app.ui.MusicPlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MediaReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val serviceIntent = Intent(context, MediaService::class.java).apply {
            action = intent.action
        }

        when (intent.action) {
            MediaService.ACTION_PLAY -> {
                Log.d("MediaReceiver", "Received play action")
                context.startService(serviceIntent)
            }
            MediaService.ACTION_PAUSE -> {
                Log.d("MediaReceiver", "Received pause action")
                context.startService(serviceIntent)
            }
            MediaService.ACTION_NEXT -> {
                Log.d("MediaReceiver", "Received next action")
                scope.launch {
                    MusicPlayerViewModel.instance?.let { viewModel ->
                        viewModel.nextSong(context)
//                        viewModel.currentSong.value?.let { newSong ->
//                            val nextIntent = Intent(context, MediaService::class.java).apply {
//                                action = MediaService.ACTION_PLAY
////                                putExtra("songUri", newSong.uri)
////                                putExtra("songTitle", newSong.title)
////                                putExtra("songDuration", newSong.duration)
//                            }
//                            context.startService(nextIntent)
//                        }
                    }
                }
            }
            MediaService.ACTION_PREVIOUS -> {
                Log.d("MediaReceiver", "Received previous action")
                scope.launch {
                    MusicPlayerViewModel.instance?.let { viewModel ->
                        viewModel.previousSong(context)
//                        viewModel.currentSong.value?.let { newSong ->
//                            val prevIntent = Intent(context, MediaService::class.java).apply {
//                                action = MediaService.ACTION_PLAY
////                                putExtra("songUri", newSong.uri)
////                                putExtra("songTitle", newSong.title)
////                                putExtra("songDuration", newSong.duration)
//                            }
//                            context.startService(prevIntent)
//                        }
                    }
                }
            }
        }
    }
}