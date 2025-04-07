package com.example.etatdeslieux.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.RoomDao
import com.example.etatdeslieux.data.RoomGroupDao
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import com.example.etatdeslieux.repository.RoomGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val roomDao: RoomDao,
    private val roomGroupDao: RoomGroupDao,
    private val roomGroupRepository: RoomGroupRepository
) : ViewModel() {

    private val _selectedRoom = MutableStateFlow<Room?>(null)
    val selectedRoom: StateFlow<Room?> = _selectedRoom.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val rooms: StateFlow<List<Room>> = roomDao.getAllRooms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val groups: StateFlow<List<RoomGroup>> = roomGroupDao.getAllGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun selectRoom(room: Room) {
        _selectedRoom.value = room
    }

    fun clearSelectedRoom() {
        _selectedRoom.value = null
    }

    fun setTargetGroupForNewRoom(groupId: Long) {
        roomGroupRepository.setTargetGroupForNewRoom(groupId)
    }

    fun clearTargetGroupForNewRoom() {
        roomGroupRepository.clearTargetGroupForNewRoom()
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            try {
                roomDao.deleteRoom(room)
                // Supprimer la référence du room de tous les groupes qui le contiennent
                groups.value.forEach { group ->
                    if (room.id in group.roomIds) {
                        val updatedRoomIds = group.roomIds.filter { it != room.id }
                        roomGroupDao.updateGroup(group.copy(roomIds = updatedRoomIds))
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression : ${e.message}"
            }
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch {
            try {
                roomDao.updateRoom(room)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour : ${e.message}"
            }
        }
    }

    fun createGroup(name: String, roomIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            try {
                val group = RoomGroup(name = name, roomIds = roomIds)
                roomGroupDao.insertGroup(group)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la création du groupe : ${e.message}"
            }
        }
    }

    fun updateGroup(group: RoomGroup) {
        viewModelScope.launch {
            try {
                roomGroupDao.updateGroup(group)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour du groupe : ${e.message}"
            }
        }
    }

    fun toggleGroupExpansion(groupId: Long) {
        viewModelScope.launch {
            try {
                val group = groups.value.find { it.id == groupId }
                if (group != null) {
                    roomGroupDao.updateGroup(group.copy(isExpanded = !group.isExpanded))
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la mise à jour du groupe : ${e.message}"
            }
        }
    }

    fun deleteGroup(group: RoomGroup) {
        viewModelScope.launch {
            try {
                roomGroupDao.deleteGroup(group)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression du groupe : ${e.message}"
            }
        }
    }

    fun addRoomToGroup(groupId: Long, roomId: Long) {
        viewModelScope.launch {
            try {
                val group = groups.value.find { it.id == groupId }
                if (group != null) {
                    val updatedRoomIds = group.roomIds + roomId
                    roomGroupDao.updateGroup(group.copy(roomIds = updatedRoomIds))
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'ajout de la pièce au groupe : ${e.message}"
            }
        }
    }

    fun addExistingRoomToGroup(groupId: Long, roomId: Long) {
        viewModelScope.launch {
            try {
                // Vérifier si la pièce n'est pas déjà dans le groupe
                val group = groups.value.find { it.id == groupId }
                if (group != null && roomId !in group.roomIds) {
                    val updatedRoomIds = group.roomIds + roomId
                    roomGroupDao.updateGroup(group.copy(roomIds = updatedRoomIds))
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'ajout de la pièce au groupe : ${e.message}"
            }
        }
    }
}
