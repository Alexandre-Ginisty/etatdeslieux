package com.example.etatdeslieux.ui.screens.room

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.repository.PhotoRepository
import com.example.etatdeslieux.data.repository.RoomRepository
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.utils.PhotoStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.flow.flow

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val photoRepository: PhotoRepository,
    private val photoStorage: PhotoStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: Long = checkNotNull(savedStateHandle.get<Long>("roomId")) { "roomId is required" }

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    data class RoomUiState(
        val isLoading: Boolean = false,
        val room: Room? = null,
        val photos: List<Photo> = emptyList(),
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    private var currentPhotoUri: Uri? = null

    init {
        if (roomId != 0L) {
            loadRoomData()
        } else {
            _uiState.value = _uiState.value.copy(error = "ID de salle invalide")
        }
    }

    fun loadRoomData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            combine(
                roomRepository.getRoomById(roomId),
                photoRepository.getPhotosByRoomId(roomId)
            ) { room, photos ->
                RoomUiState(
                    room = room,
                    photos = photos,
                    isLoading = false
                )
            }.catch { e ->
                Log.e("RoomViewModel", "Error loading room data", e)
                _uiState.value = _uiState.value.copy(
                    error = "Erreur de chargement: ${e.message}",
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun preparePhotoCapture(): Uri? {
        return try {
            _uiState.value = _uiState.value.copy(isLoading = true)
            photoStorage.createTempPhotoUri().also { 
                currentPhotoUri = it
                Log.d("RoomViewModel", "Prepared photo URI: $it")
            }
        } catch (e: Exception) {
            Log.e("RoomViewModel", "Error preparing photo capture", e)
            _uiState.value = _uiState.value.copy(error = "Erreur de préparation photo: ${e.message}")
            null
        }
    }

    fun handlePhotoCapture() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val uri = currentPhotoUri
                if (uri == null) {
                    Log.e("RoomViewModel", "No photo URI available")
                    _uiState.value = _uiState.value.copy(
                        error = "Veuillez réessayer de prendre la photo",
                        isLoading = false
                    )
                    return@launch
                }
                
                Log.d("RoomViewModel", "Handling photo capture: $uri")
                addPhoto(uri)
                currentPhotoUri = null
                _uiState.value = _uiState.value.copy(isLoading = false)
                
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Photo capture failed", e)
                _uiState.value = _uiState.value.copy(
                    error = "Échec de capture photo: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    fun updateRoom(room: Room) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                roomRepository.updateRoom(room)
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Failed to update room", e)
                _uiState.value = _uiState.value.copy(
                    error = "Échec de mise à jour: ${e.message}"
                )
            }
        }
    }

    suspend fun addPhoto(uri: Uri) {
        try {
            Log.d("RoomViewModel", "Saving photo: $uri")
            _uiState.value = _uiState.value.copy(isLoading = true)
            val permanentPath = photoStorage.savePhoto(uri) ?: throw IOException("Failed to save photo")
            
            val photo = Photo(
                roomId = roomId,
                uri = permanentPath,
                timestamp = System.currentTimeMillis(),
                comment = ""
            )
            
            photoRepository.insertPhoto(photo)
            Log.d("RoomViewModel", "Photo saved successfully: $permanentPath")
            
        } catch (e: Exception) {
            Log.e("RoomViewModel", "Failed to add photo", e)
            throw IOException("Failed to save photo: ${e.message}", e)
        }
    }

    fun updatePhoto(photo: Photo) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                photoRepository.updatePhoto(photo)
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Failed to update photo", e)
                _uiState.value = _uiState.value.copy(error = "Échec de mise à jour: ${e.message}")
            }
        }

    }


fun deletePhoto(photo: Photo) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true)

            photoStorage.deletePhoto(photo.uri)
            photoRepository.deletePhoto(photo)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Échec de suppression: ${e.message}"
            )
        } finally {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}

    fun deleteRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                photoRepository.deletePhotosByRoomId(roomId)
                roomRepository.deleteRoom(roomId)
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Failed to delete room", e)
                _uiState.value = _uiState.value.copy(error = "Échec de suppression: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun saveRoom(room: Room) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                room.createdAt = LocalDateTime.now()
                roomRepository.insertRoom(room)
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Failed to save room", e)
                _uiState.value = _uiState.value.copy(
                    error = "Échec de sauvegarde: ${e.message}"
                )
            }
        }
    }
}