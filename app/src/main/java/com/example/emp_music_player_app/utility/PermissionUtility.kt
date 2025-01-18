package com.example.emp_music_player_app.utility

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class PermissionUtility(private val context: Context) {
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun hasPermissions(): Boolean {
        val ok = requiredPermissions.all {
            val granted = ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            Log.d("MusicPlayer", "Permission $it granted: $granted")
            granted
        }
        Log.d("MusicPlayer", "Permissions granted: $ok")
        return ok
    }

//    fun checkAndRequestPermissions(permissionCallback: (Boolean) -> Unit) {
//        if (hasPermissions()) {
//            loadMusic()
//        } else {
//            permissionLauncher.launch(requiredPermissions)
//        }
//    }
}