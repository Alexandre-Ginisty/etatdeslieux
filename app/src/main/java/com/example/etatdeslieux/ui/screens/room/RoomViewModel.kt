package com.example.etatdeslieux.ui.screens.room

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.PhotoDao
import com.example.etatdeslieux.data.RoomDao
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.utils.PhotoStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomDao: RoomDao,
    private val photoDao: PhotoDao,
    private val photoStorage: PhotoStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: Long = savedStateHandle.get<Long>("roomId") ?: 0L

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val room = roomDao.getRoomById(roomId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val photos = roomDao.getRoomById(roomId).flatMapLatest { room ->
        if (room != null) {
            photoDao.getPhotosByRoom(roomId)
        } else {
            flow { emit(emptyList()) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        if (roomId == 0L) {
            _error.value = "ID de salle invalide"
        }
    }

    fun addPhoto(uri: Uri, comment: String) {
        if (roomId == 0L) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val storedPath = photoStorage.savePhoto(uri)
                if (storedPath != null) {
                    val photo = Photo(
                        roomId = roomId,
                        uri = storedPath,
                        comment = comment
                    )
                    photoDao.insert(photo)
                } else {
                    _error.value = "Erreur lors de la sauvegarde de la photo"
                }
            }
        }
    }

    fun updatePhoto(photo: Photo) {
        if (roomId == 0L) return
        viewModelScope.launch {
            photoDao.update(photo)
        }
    }

    fun deletePhoto(photo: Photo) {
        if (roomId == 0L) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                photoStorage.deletePhoto(photo.uri)
                photoDao.delete(photo)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Nettoyer les ressources si nécessaire
    }
}
