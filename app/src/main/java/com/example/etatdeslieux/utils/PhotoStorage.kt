package com.example.etatdeslieux.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

@Singleton
class PhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val photoDir = File(context.filesDir, "photos").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    fun savePhoto(uri: Uri): String? {
        val fileName = "${UUID.randomUUID()}.jpg"
        val destFile = File(photoDir, fileName)

        return try {
            Log.d("PhotoStorage", "Starting photo save process for uri: $uri")
            Log.d("PhotoStorage", "Saving to: ${destFile.absolutePath}")

            context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedInputStream(input).use { bufferedInput ->
                    FileOutputStream(destFile).use { output ->
                        BufferedOutputStream(output).use { bufferedOutput ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (bufferedInput.read(buffer).also { bytesRead = it } != -1) {
                                bufferedOutput.write(buffer, 0, bytesRead)
                            }
                            bufferedOutput.flush()
                        }
                    }
                }
            } ?: throw IOException("Failed to open input stream")

            if (!destFile.exists() || destFile.length() == 0L) {
                throw IOException("Failed to save image: file doesn't exist or is empty")
            }

            Log.d("PhotoStorage", "Successfully saved photo: ${destFile.absolutePath}, size: ${destFile.length()}")

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile
            ).toString()

        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error saving photo", e)
            destFile.delete()
            null
        }
    }

    fun getPhotoFile(uriString: String): File? {
        return try {
            val uri = Uri.parse(uriString)
            val fileName = uri.lastPathSegment ?: return null
            val file = File(photoDir, fileName)

            Log.d("PhotoStorage", "Getting photo file for URI: $uriString")
            Log.d("PhotoStorage", "Looking in: ${file.absolutePath}")
            Log.d("PhotoStorage", "File exists: ${file.exists()}")

            if (file.exists()) {
                val tempFile = File(context.cacheDir, "temp_${fileName}")
                FileInputStream(file).use { input ->
                    BufferedInputStream(input).use { bufferedInput ->
                        FileOutputStream(tempFile).use { output ->
                            bufferedInput.copyTo(output)
                        }
                    }
                }
                tempFile
            } else null
        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error getting photo file", e)
            null
        }
    }

    fun deletePhoto(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error deleting photo", e)
        }
    }

    fun createTempPhotoUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "photo_${timeStamp}_${System.currentTimeMillis()}"
        val directory = File(context.cacheDir, "images").apply {
            if (!exists()) mkdirs()
        }
        val photoFile = File.createTempFile(imageFileName, ".jpg", directory)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }
}