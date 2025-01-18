package com.example.emp_music_player_app

import android.app.Application
import com.example.emp_music_player_app.data.database.MusicDao
import com.example.emp_music_player_app.data.database.MusicDatabase

class MusicPlayerApplication : Application() {
    private lateinit var database: MusicDatabase
    private lateinit var musicDao: MusicDao

    override fun onCreate() {
        super.onCreate()

        database = MusicDatabase.getInstance(applicationContext)
        musicDao = database.musicDao()
    }

    fun getDatabase(): MusicDatabase {
        return database
    }

    fun getMusicDao(): MusicDao {
        return musicDao
    }
}