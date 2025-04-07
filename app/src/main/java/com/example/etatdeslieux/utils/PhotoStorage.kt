package com.example.etatdeslieux.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
        return try {
            // Créer un nom de fichier unique
            val fileName = "${UUID.randomUUID()}.jpg"
            val destFile = File(photoDir, fileName)

            // Ouvrir le flux d'entrée depuis l'URI
            context.contentResolver.openInputStream(uri)?.use { input ->
                // Décoder l'image pour la redimensionner si nécessaire
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(input, null, options)
                
                // Calculer le facteur de redimensionnement
                val maxSize = 1920 // Taille maximale en pixels
                val scale = Math.min(
                    options.outWidth / maxSize,
                    options.outHeight / maxSize
                ).coerceAtLeast(1)
                
                // Réinitialiser le flux d'entrée
                context.contentResolver.openInputStream(uri)?.use { newInput ->
                    // Décoder l'image avec le facteur de redimensionnement
                    val finalOptions = BitmapFactory.Options().apply {
                        inSampleSize = scale
                    }
                    val bitmap = BitmapFactory.decodeStream(newInput, null, finalOptions)
                    
                    // Sauvegarder l'image redimensionnée
                    FileOutputStream(destFile).use { out ->
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    bitmap?.recycle()
                }
            }
            
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error saving photo", e)
            null
        }
    }

    fun getPhotoFile(path: String): File? {
        val file = File(path)
        return if (file.exists()) file else null
    }

    fun deletePhoto(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error deleting photo", e)
        }
    }
}
