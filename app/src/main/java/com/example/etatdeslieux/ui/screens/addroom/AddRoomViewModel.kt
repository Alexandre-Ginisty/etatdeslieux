package com.example.etatdeslieux.ui.screens.addroom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.repository.RoomRepository
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.EtatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var name by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    var floor by mutableStateOf("")
        private set

    var creator by mutableStateOf("")
        private set

    var etatType by mutableStateOf(EtatType.ENTREE)
        private set

    fun updateName(newName: String) {
        name = newName
    }

    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    fun updateFloor(newFloor: String) {
        floor = newFloor
    }

    fun updateCreator(newCreator: String) {
        creator = newCreator
    }

    fun updateEtatType(newEtatType: EtatType) {
        etatType = newEtatType
    }

    fun createRoom(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                // Vérifier que tous les champs obligatoires sont remplis
                when {
                    name.isBlank() -> {
                        _error.value = "Le nom de la pièce est obligatoire"
                        return@launch
                    }
                    description.isBlank() -> {
                        _error.value = "La description est obligatoire"
                        return@launch
                    }
                    floor.isBlank() -> {
                        _error.value = "L'étage est obligatoire"
                        return@launch
                    }
                    creator.isBlank() -> {
                        _error.value = "Le nom du créateur est obligatoire"
                        return@launch
                    }
                }
                
                val room = Room(
                    name = name,
                    description = description,
                    size = 0f, // Valeur par défaut pour la taille
                    floor = floor.toIntOrNull() ?: 0,
                    creator = creator,
                    etatType = etatType.name,
                    etatNumber = 1
                )
                val newRoomId = roomRepository.insertRoom(room)
                onSuccess(newRoomId)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la création : ${e.message}"
            }
        }
    }
}
