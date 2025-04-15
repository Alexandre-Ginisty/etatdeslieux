package com.example.etatdeslieux.ui.screens.room

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.repository.PhotoRepository
import com.example.etatdeslieux.data.repository.RoomRepository
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.utils.PhotoStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val photoRepository: PhotoRepository,
    private val photoStorage: PhotoStorage,
    @ApplicationContext private val context: Context,
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

    private val _currentPhotoUri = MutableStateFlow<Uri?>(null)
    val currentPhotoUri: StateFlow<Uri?> = _currentPhotoUri.asStateFlow()

    // État pour la photo en attente de commentaire
    private val _pendingPhotoUri = MutableStateFlow<Uri?>(null)
    val pendingPhotoUri: StateFlow<Uri?> = _pendingPhotoUri.asStateFlow()

    // État pour la photo dont le commentaire est en cours d'édition
    private val _photoToEdit = MutableStateFlow<Photo?>(null)
    val photoToEdit: StateFlow<Photo?> = _photoToEdit.asStateFlow()

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
                _currentPhotoUri.value = it
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
                
                // Essayer de récupérer l'URI depuis les préférences si elle n'est pas disponible
                var uri = _currentPhotoUri.value
                if (uri == null) {
                    withContext(Dispatchers.Main) {
                        val prefs = context.getSharedPreferences("photo_prefs", Context.MODE_PRIVATE)
                        val savedUri = prefs.getString("current_photo_uri_$roomId", null)
                        if (savedUri != null) {
                            uri = Uri.parse(savedUri)
                            _currentPhotoUri.value = uri
                            Log.d("RoomViewModel", "Retrieved URI from SharedPreferences: $uri")
                        }
                    }
                }
                
                // Vérifier à nouveau si l'URI est disponible
                if (uri == null) {
                    Log.e("RoomViewModel", "No photo URI available")
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            error = "Veuillez réessayer de prendre la photo",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                Log.d("RoomViewModel", "Handling photo capture: $uri")
                try {
                    // Vérifier que le fichier existe toujours
                    val file = uri!!.path?.let { File(it) }
                    if (uri!!.scheme == "file" && (file == null || !file.exists())) {
                        Log.e("RoomViewModel", "Photo file no longer exists: $uri")
                        _currentPhotoUri.value = null
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                error = "Le fichier photo n'existe plus, veuillez réessayer",
                                isLoading = false
                            )
                        }
                        return@launch
                    }
                    
                    // Au lieu d'ajouter la photo directement, on la met en attente pour le commentaire
                    _pendingPhotoUri.value = uri
                    
                    // Effacer l'URI des préférences partagées après un succès
                    withContext(Dispatchers.Main) {
                        val prefs = context.getSharedPreferences("photo_prefs", Context.MODE_PRIVATE)
                        prefs.edit().remove("current_photo_uri_$roomId").apply()
                    }
                    
                    _currentPhotoUri.value = null
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } catch (e: Exception) {
                    Log.e("RoomViewModel", "Failed to process photo", e)
                    _currentPhotoUri.value = null
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            error = "Échec du traitement de la photo: ${e.message}",
                            isLoading = false
                        )
                    }
                }
                
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Photo capture failed", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = "Échec de capture photo: ${e.message}",
                        isLoading = false
                    )
                }
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
                _uiState.value = _uiState.value.copy(error = "Échec de mise à jour: ${e.message}")
            }
        }
    }

    suspend fun addPhoto(uri: Uri) {
        try {
            Log.d("RoomViewModel", "Saving photo: $uri")
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            
            // Vérifier que l'URI est valide avant de continuer
            if (uri.toString().isEmpty()) {
                throw IOException("URI de photo invalide")
            }
            
            val permanentPath = photoStorage.savePhoto(uri) ?: throw IOException("Failed to save photo")
            
            val photo = Photo(
                roomId = roomId,
                uri = permanentPath,
                timestamp = System.currentTimeMillis(),
                comment = ""
            )
            
            photoRepository.insertPhoto(photo)
            Log.d("RoomViewModel", "Photo saved successfully: $permanentPath")
            
            // Recharger les photos après l'ajout
            val updatedPhotos = photoRepository.getPhotosByRoomId(roomId).first()
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    photos = updatedPhotos,
                    isLoading = false
                )
            }
            
        } catch (e: Exception) {
            Log.e("RoomViewModel", "Failed to add photo", e)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    error = "Échec d'ajout de photo: ${e.message}",
                    isLoading = false
                )
            }
            throw IOException("Échec d'ajout de photo: ${e.message}")
        }
    }

    /**
     * Ajoute une photo avec un commentaire
     * @param uri URI de la photo
     * @param comment Commentaire associé à la photo
     */
    fun addPhotoWithComment(uri: Uri, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("RoomViewModel", "Adding photo with comment: $uri, comment: $comment")
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                
                // Vérifier que l'URI est valide avant de continuer
                if (uri.toString().isEmpty()) {
                    throw IOException("URI de photo invalide")
                }
                
                val permanentPath = photoStorage.savePhoto(uri) ?: throw IOException("Failed to save photo")
                
                val photo = Photo(
                    roomId = roomId,
                    uri = permanentPath,
                    timestamp = System.currentTimeMillis(),
                    comment = comment
                )
                
                photoRepository.insertPhoto(photo)
                Log.d("RoomViewModel", "Photo saved successfully with comment: $permanentPath")
                
                // Recharger les photos après l'ajout
                val updatedPhotos = photoRepository.getPhotosByRoomId(roomId).first()
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        photos = updatedPhotos,
                        isLoading = false
                    )
                    // Réinitialiser l'URI en attente
                    _pendingPhotoUri.value = null
                }
                
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Failed to add photo with comment", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = "Échec d'ajout de photo: ${e.message}",
                        isLoading = false
                    )
                    // Réinitialiser l'URI en attente même en cas d'erreur
                    _pendingPhotoUri.value = null
                }
            }
        }
    }

    /**
     * Ajoute un commentaire à la photo en attente
     * @param comment Le commentaire à ajouter
     */
    fun addPhotoComment(comment: String) {
        val uri = _pendingPhotoUri.value ?: return
        addPhotoWithComment(uri, comment)
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

    // Méthode pour récupérer l'ID de la pièce
    fun getRoomId(): Long {
        return roomId
    }

    // Méthode pour restaurer l'URI de la photo depuis les préférences partagées
    fun restorePhotoUri(uri: Uri) {
        _currentPhotoUri.value = uri
        Log.d("RoomViewModel", "Restored photo URI: $uri")
    }

    /**
     * Télécharge une photo dans le dossier Downloads
     * @param photo La photo à télécharger
     * @return L'URI de la photo téléchargée ou null en cas d'échec
     */
    fun downloadPhoto(photo: Photo): Uri? {
        return try {
            val uri = photoStorage.exportPhotoToDownloads(photo.uri)
            if (uri != null) {
                Log.d("RoomViewModel", "Photo downloaded successfully: $uri")
            } else {
                Log.e("RoomViewModel", "Failed to download photo")
                _uiState.value = _uiState.value.copy(
                    error = "Échec du téléchargement de la photo"
                )
            }
            uri
        } catch (e: Exception) {
            Log.e("RoomViewModel", "Error downloading photo", e)
            _uiState.value = _uiState.value.copy(
                error = "Erreur lors du téléchargement: ${e.message}"
            )
            null
        }
    }

    /**
     * Génère un PDF de l'état des lieux
     * @return Le fichier PDF généré ou null en cas d'erreur
     */
    fun generatePdf(): File? {
        val room = _uiState.value.room ?: return null
        val photos = _uiState.value.photos

        _uiState.value = _uiState.value.copy(isLoading = true)

        return try {
            val pdfGenerator = com.example.etatdeslieux.utils.PdfGenerator(context)
            val pdfFile = pdfGenerator.generateRoomPdf(room, photos)
            
            if (pdfFile != null) {
                Log.d("RoomViewModel", "PDF généré avec succès: ${pdfFile.absolutePath}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "PDF généré avec succès"
                )
            } else {
                Log.e("RoomViewModel", "Échec de la génération du PDF")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Échec de la génération du PDF"
                )
            }
            
            pdfFile
        } catch (e: Exception) {
            Log.e("RoomViewModel", "Erreur lors de la génération du PDF", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Erreur lors de la génération du PDF: ${e.message}"
            )
            null
        }
    }

    /**
     * Définit l'URI de la photo en cours
     * @param uri L'URI de la photo
     */
    fun setPhotoUri(uri: Uri) {
        _currentPhotoUri.value = uri
        
        // Sauvegarder l'URI dans les préférences partagées
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("photo_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("current_photo_uri_$roomId", uri.toString()).apply()
            Log.d("RoomViewModel", "Saved URI to SharedPreferences: $uri")
        }
    }

    /**
     * Définit la photo dont le commentaire est en cours d'édition
     * @param photo La photo à éditer
     */
    fun setPhotoToEdit(photo: Photo) {
        _photoToEdit.value = photo
    }
    
    /**
     * Met à jour le commentaire d'une photo
     * @param comment Le nouveau commentaire
     */
    fun updatePhotoComment(comment: String) {
        val photo = _photoToEdit.value ?: return
        val updatedPhoto = photo.copy(comment = comment)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                photoRepository.updatePhoto(updatedPhoto)
                
                // Recharger les photos après la mise à jour
                val updatedPhotos = photoRepository.getPhotosByRoomId(roomId).first()
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        photos = updatedPhotos,
                        isLoading = false
                    )
                    // Réinitialiser la photo en cours d'édition
                    _photoToEdit.value = null
                }
                
                Log.d("RoomViewModel", "Photo comment updated successfully")
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Failed to update photo comment", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = "Échec de mise à jour du commentaire: ${e.message}",
                        isLoading = false
                    )
                    // Réinitialiser la photo en cours d'édition même en cas d'erreur
                    _photoToEdit.value = null
                }
            }
        }
    }
}