package com.example.etatdeslieux.ui.screens.addroom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.RoomDao
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.EtatType
import com.example.etatdeslieux.repository.RoomGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRoomViewModel @Inject constructor(
    private val roomDao: RoomDao,
    private val roomGroupRepository: RoomGroupRepository
) : ViewModel() {

    var isRoomAdded by mutableStateOf(false)
        private set

    var hasAttemptedToSubmit by mutableStateOf(false)
        private set

    fun createRoom(
        name: String,
        description: String,
        size: String,
        floor: String,
        creator: String,
        etatType: EtatType,
        onError: (String) -> Unit
    ) {
        hasAttemptedToSubmit = true

        // Validation simple
        if (name.isBlank()) {
            onError("Le nom de la pièce est requis")
            return
        }

        val sizeFloat = size.toFloatOrNull()
        if (sizeFloat == null || sizeFloat <= 0) {
            onError("La taille doit être un nombre positif")
            return
        }

        val floorInt = floor.toIntOrNull()
        if (floorInt == null) {
            onError("L'étage doit être un nombre valide")
            return
        }

        if (creator.isBlank()) {
            onError("Le créateur est requis")
            return
        }

        // Création de la pièce
        viewModelScope.launch {
            try {
                val room = Room(
                    name = name,
                    description = description,
                    size = sizeFloat,
                    floor = floorInt,
                    creator = creator,
                    etatType = etatType.name,
                    etatNumber = 1 // Sera mis à jour automatiquement par Room
                )
                val newRoomId = roomDao.insertRoom(room)
                
                // Si un groupe cible est défini, ajouter la pièce au groupe
                roomGroupRepository.getTargetGroupForNewRoom()?.let { groupId ->
                    roomGroupRepository.addRoomToGroup(groupId, newRoomId)
                    roomGroupRepository.clearTargetGroupForNewRoom()
                }
                
                isRoomAdded = true
            } catch (e: Exception) {
                onError("Erreur lors de la création de la pièce: ${e.message}")
            }
        }
    }
}
