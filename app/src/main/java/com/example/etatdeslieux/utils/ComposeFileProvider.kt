package com.example.etatdeslieux.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.etatdeslieux.R
import java.io.File
import java.io.IOException

class ComposeFileProvider : FileProvider(R.xml.file_paths) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val file = File.createTempFile(
                "photo_${System.currentTimeMillis()}_",
                ".jpg",
                directory
            )
            return getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }
}