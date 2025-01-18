package com.example.emp_music_player_app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.emp_music_player_app.R
import com.example.emp_music_player_app.ui.MainActivity

class MediaService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var notificationManager: NotificationManagerCompat? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val binder = MediaServiceBinder()
    private var progressUpdateJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> = _currentPosition

    private var currentSongTitle: String = ""
    private var currentSongDuration: Long = 0
    private var currentSongUri: String = ""
    private var currentAlbumImagePath: String? = null

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "MediaService").apply {
            isActive = true

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
//                    sendBroadcast(Intent(ACTION_PLAY).setPackage(packageName))
                    resumeMusic()
                }

                override fun onPause() {
//                    sendBroadcast(Intent(ACTION_PAUSE).setPackage(packageName))
                    pauseMusic()
                }

                override fun onSkipToNext() {
                    sendBroadcast(Intent(ACTION_NEXT).setPackage(packageName))
                }

                override fun onSkipToPrevious() {
                    sendBroadcast(Intent(ACTION_PREVIOUS).setPackage(packageName))
                }

                override fun onSeekTo(pos: Long) {
                    mediaPlayer?.seekTo(pos.toInt())
                    updatePlaybackState(pos)
                    _currentPosition.value = pos.toFloat()
                }
            })
            isActive = true
        }
    }

//    private fun handlePlaybackAction(action: String, extras: Bundle? = null) {
//        when (action) {
//            ACTION_PLAY -> {
//                val uri = extras?.getString("songUri")
//                val title = extras?.getString("songTitle")
//                val duration = extras?.getLong("songDuration") ?: 0L
//
//                if (uri != null) {
//                    playMusic(uri, title, duration)
//                } else {
//                    resumeMusic()
//                }
//            }
//            ACTION_PAUSE -> pauseMusic()
//            ACTION_NEXT -> sendBroadcast(Intent(ACTION_NEXT).setPackage(packageName))
//            ACTION_PREVIOUS -> sendBroadcast(Intent(ACTION_PREVIOUS).setPackage(packageName))
//        }
//    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
//        intent?.action?.let { action ->
////            handlePlaybackAction(action, intent.extras)
//        }
        return START_NOT_STICKY
    }

    inner class MediaServiceBinder : Binder() {
        fun getService(): MediaService = this@MediaService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
         initializeMediaSession()
        createNotificationChannel()
        notificationManager = NotificationManagerCompat.from(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun playMusic(uri: String, title: String? = null, duration: Long = 0, albumImagePath: String? = null) {
        try {
            if (uri != currentSongUri || mediaPlayer == null) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(uri)
                    prepare()
                    setOnCompletionListener {
                        sendBroadcast(Intent(ACTION_NEXT).setPackage(packageName))
                    }
                }
                currentSongUri = uri
            }
            title?.let { currentSongTitle = it }
            if (duration > 0) currentSongDuration = duration
            currentAlbumImagePath = albumImagePath

            mediaPlayer?.start()
            _isPlaying.value = true
            updateMediaMetadata()
            startProgressUpdate()
            updatePlaybackState()
            startForegroundService()
        } catch (e: Exception) {
            Log.e("MediaService", "Error playing music: ${e.message}")
        }
    }

    private fun updateMediaMetadata() {
        Log.d("MediaService", "Song duration: $currentSongDuration")
        Log.d("MediaService", "Song title: $currentSongTitle")
//        currentSongTitle = "asdasdasdas"
//        currentSongDuration = 80000
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSongTitle)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentSongDuration)
                .build()
        )

    }

    private fun startProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = serviceScope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val position = player.currentPosition.toFloat()
                        _currentPosition.value = position
                        updatePlaybackState(position.toLong())
                    }
                }
                delay(1000)
            }
        }
    }

    fun updatePlaybackState(position: Long = mediaPlayer?.currentPosition?.toLong() ?: 0) {
        val state = if (_isPlaying.value) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }

        val playbackSpeed = if (_isPlaying.value) 1f else 0f

        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
            .setState(state, position, playbackSpeed)

        mediaSession.setPlaybackState(stateBuilder.build())
        updateNotification()
        Log.d("MediaService", "Playback position: $position, State: $state")
    }

    private fun createNotification(): Notification {
        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_album_placeholder)
            .setLargeIcon(if (currentAlbumImagePath != null) {
                BitmapFactory.decodeFile(currentAlbumImagePath)
            } else null)
            .setContentTitle(currentSongTitle)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(_isPlaying.value)
            .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS))
            .addAction(
                generateAction(
                    if (_isPlaying.value) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play,
                    if (_isPlaying.value) "Pause" else "Play",
                    if (_isPlaying.value) ACTION_PAUSE else ACTION_PLAY
                )
            )
            .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT))

        return builder.build()
    }

    private fun generateAction(icon: Int, title: String, intentAction: String): NotificationCompat.Action {
        val intent = Intent(intentAction).setPackage(packageName)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            when (intentAction) {
                ACTION_PREVIOUS -> 1
                ACTION_PLAY, ACTION_PAUSE -> 2
                ACTION_NEXT -> 3
                else -> 4
            },
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        progressUpdateJob?.cancel()
        updatePlaybackState()
    }

    private fun resumeMusic() {
        mediaPlayer?.start()
        _isPlaying.value = true
        startProgressUpdate()
        updatePlaybackState()
    }

    fun seekTo(position: Float) {
        mediaPlayer?.let { player ->
            val positionInt = position.toInt()
            player.seekTo(positionInt)
            _currentPosition.value = position
            updatePlaybackState(positionInt.toLong())
        }
    }

    private fun updateNotification() {
        if (checkNotificationPermission()) {
            try {
                notificationManager?.notify(NOTIFICATION_ID, createNotification())
            } catch (e: SecurityException) {
                Log.e("MediaService", "Permission denied: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressUpdateJob?.cancel()
        mediaSession.release()
        mediaPlayer?.release()
        mediaPlayer = null
        serviceJob.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "com.example.emp_music_player_app.PLAY"
        const val ACTION_PAUSE = "com.example.emp_music_player_app.PAUSE"
        const val ACTION_NEXT = "com.example.emp_music_player_app.NEXT"
        const val ACTION_PREVIOUS = "com.example.emp_music_player_app.PREVIOUS"
    }
}