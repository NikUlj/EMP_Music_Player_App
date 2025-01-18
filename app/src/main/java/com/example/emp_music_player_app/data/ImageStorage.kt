package com.example.emp_music_player_app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

class ImageStorage(context: Context) {
    private val imageDir = File(context.filesDir, "artwork").apply {
        if (!exists()) mkdirs()
    }

    suspend fun saveImageFromUrl(imageUrl: String, prefix: String): String? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(imageUrl).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val filename = "$prefix-${UUID.randomUUID()}.jpg"
            val file = File(imageDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            return@withContext file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageStorage", "Error storing image: ${e.message}")
            return@withContext null
        }
    }

    fun deleteImage(filepath: String) {
        try {
            File(filepath).delete()
        } catch (e: Exception) {
            Log.e("ImageStorage", "Error deleting image: ${e.message}")
        }
    }
}