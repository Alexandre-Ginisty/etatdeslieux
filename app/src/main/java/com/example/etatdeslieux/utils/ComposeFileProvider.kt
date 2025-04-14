package com.example.etatdeslieux.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.etatdeslieux.R
import java.io.File
import java.io.IOException
import android.util.Log

class ComposeFileProvider : FileProvider(R.xml.file_paths) {
    companion object {
        fun getImageUri(context: Context): Uri {
            try {
                // Créer le répertoire temporaire s'il n'existe pas
                val directory = File(context.cacheDir, "images").apply {
                    if (!exists()) {
                        if (!mkdirs()) {
                            throw IOException("Failed to create directory: $absolutePath")
                        }
                    }
                }

                // Créer le fichier temporaire
                val file = File.createTempFile(
                    "photo_${System.currentTimeMillis()}_",
                    ".jpg",
                    directory
                ).apply {
                    // S'assurer que le fichier est accessible en écriture
                    if (!canWrite()) {
                        setWritable(true)
                    }
                    // Supprimer le fichier à la fermeture de l'application
                    deleteOnExit()
                }

                Log.d("ComposeFileProvider", "Created temp file: ${file.absolutePath}")
                Log.d("ComposeFileProvider", "File exists: ${file.exists()}")
                Log.d("ComposeFileProvider", "File is writable: ${file.canWrite()}")

                // Créer l'URI pour le fichier
                return getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                ).also { uri ->
                    Log.d("ComposeFileProvider", "Created URI: $uri")
                }
            } catch (e: Exception) {
                Log.e("ComposeFileProvider", "Error creating image URI", e)
                throw e
            }
        }
    }
}