package com.example.etatdeslieux.ui.screens.room

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.PhotoDao
import com.example.etatdeslieux.data.RoomDao
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*

@HiltViewModel
class RoomDetailViewModel @Inject constructor(
    private val roomDao: RoomDao,
    private val photoDao: PhotoDao,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: Long = checkNotNull(savedStateHandle["roomId"])

    val room: StateFlow<Room?> = roomDao.getRoomById(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val photos: StateFlow<List<Photo>> = photoDao.getPhotosByRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addPhoto(uri: Uri, comment: String) {
        viewModelScope.launch {
            try {
                // Copier l'image dans le stockage interne de l'application
                val fileName = "photo_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, fileName)
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                // Créer et sauvegarder la photo
                val photo = Photo(
                    roomId = roomId,
                    uri = file.absolutePath,
                    comment = comment
                )
                photoDao.insert(photo)
            } catch (e: Exception) {
                // Gérer l'erreur
                e.printStackTrace()
            }
        }
    }
}
