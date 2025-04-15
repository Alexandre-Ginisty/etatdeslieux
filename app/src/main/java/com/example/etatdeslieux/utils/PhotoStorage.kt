package com.example.etatdeslieux.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
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
import android.media.MediaScannerConnection
import java.io.FileNotFoundException

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
            Log.d("PhotoStorage", "URI scheme: ${uri.scheme}")
            Log.d("PhotoStorage", "Saving to: ${destFile.absolutePath}")

            // Vérifier que l'URI est valide
            if (uri.scheme == null) {
                throw IOException("Invalid URI scheme: null")
            }

            if (!uri.scheme.equals("content") && !uri.scheme.equals("file")) {
                throw IOException("Invalid URI scheme: ${uri.scheme}")
            }

            // Vérifier que le répertoire de destination existe
            if (!photoDir.exists() && !photoDir.mkdirs()) {
                throw IOException("Failed to create photo directory: ${photoDir.absolutePath}")
            }

            // Utiliser ContentResolver pour ouvrir l'URI
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    throw IOException("Failed to open input stream, null returned")
                }
                
                Log.d("PhotoStorage", "Successfully opened input stream")
                
                BufferedInputStream(inputStream).use { bufferedInput ->
                    FileOutputStream(destFile).use { output ->
                        BufferedOutputStream(output).use { bufferedOutput ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            
                            while (bufferedInput.read(buffer).also { bytesRead = it } != -1) {
                                bufferedOutput.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                            }
                            bufferedOutput.flush()
                            
                            Log.d("PhotoStorage", "Total bytes read: $totalBytesRead")
                        }
                    }
                }
                
                inputStream.close()
            } catch (e: SecurityException) {
                Log.e("PhotoStorage", "Security exception when opening URI", e)
                throw IOException("Permission denied to access the photo", e)
            } catch (e: FileNotFoundException) {
                Log.e("PhotoStorage", "File not found exception when opening URI", e)
                throw IOException("Photo file not found", e)
            } catch (e: Exception) {
                Log.e("PhotoStorage", "Unexpected error when processing photo", e)
                throw IOException("Error processing photo: ${e.message}", e)
            }

            // Vérifier que le fichier a bien été créé et n'est pas vide
            if (!destFile.exists()) {
                throw IOException("Failed to save image: file doesn't exist")
            }
            
            if (destFile.length() == 0L) {
                destFile.delete()
                throw IOException("Failed to save image: file is empty")
            }

            Log.d("PhotoStorage", "Successfully saved photo: ${destFile.absolutePath}, size: ${destFile.length()}")

            // Retourner l'URI du fichier sauvegardé
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile
            ).toString()

        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error saving photo", e)
            // Nettoyer en cas d'erreur
            if (destFile.exists()) {
                destFile.delete()
            }
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
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "photo_${timeStamp}_${System.currentTimeMillis()}"
            
            // S'assurer que le répertoire existe
            val directory = File(context.cacheDir, "images").apply {
                if (!exists() && !mkdirs()) {
                    Log.e("PhotoStorage", "Failed to create directory: $absolutePath")
                    throw IOException("Failed to create directory: $absolutePath")
                }
            }
            
            // Créer le fichier temporaire
            val photoFile = File.createTempFile(imageFileName, ".jpg", directory)
            
            // S'assurer que le fichier est créé et accessible en écriture
            if (!photoFile.exists()) {
                photoFile.createNewFile()
            }
            
            if (!photoFile.canWrite()) {
                photoFile.setWritable(true)
                Log.d("PhotoStorage", "Set file writable: ${photoFile.canWrite()}")
            }
            
            // Créer l'URI avec FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            
            Log.d("PhotoStorage", "Created temp photo URI: $uri")
            Log.d("PhotoStorage", "File exists: ${photoFile.exists()}")
            Log.d("PhotoStorage", "File path: ${photoFile.absolutePath}")
            Log.d("PhotoStorage", "File can write: ${photoFile.canWrite()}")
            
            return uri
        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error creating temp photo URI", e)
            throw e
        }
    }

    /**
     * Exporte une photo vers le dossier de téléchargements public
     * @param photoUri URI de la photo à exporter
     * @return URI de la photo exportée ou null en cas d'échec
     */
    fun exportPhotoToDownloads(photoUri: String): Uri? {
        return try {
            val sourceUri = Uri.parse(photoUri)
            val fileName = "EtatDesLieux_${System.currentTimeMillis()}.jpg"
            
            // Créer le dossier de destination dans Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val destFile = File(downloadsDir, fileName)
            Log.d("PhotoStorage", "Exporting photo to: ${destFile.absolutePath}")
            
            // Copier le fichier
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BufferedInputStream(input).use { bufferedInput ->
                    FileOutputStream(destFile).use { output ->
                        BufferedOutputStream(output).use { bufferedOutput ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            
                            while (bufferedInput.read(buffer).also { bytesRead = it } != -1) {
                                bufferedOutput.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                            }
                            bufferedOutput.flush()
                            
                            Log.d("PhotoStorage", "Total bytes exported: $totalBytesRead")
                        }
                    }
                }
            } ?: throw IOException("Failed to open input stream")
            
            // Notifier la galerie qu'un nouveau fichier a été ajouté
            MediaScannerConnection.scanFile(
                context,
                arrayOf(destFile.absolutePath),
                arrayOf("image/jpeg"),
                null
            )
            
            Log.d("PhotoStorage", "Photo successfully exported to Downloads")
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error exporting photo", e)
            null
        }
    }
}