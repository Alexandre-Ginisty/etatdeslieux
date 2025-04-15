package com.example.etatdeslieux.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.repository.RoomGroupRepository
import com.example.etatdeslieux.data.repository.RoomRepository
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Options de tri disponibles
enum class SortOption {
    NAME_ASC,    // Nom (A-Z)
    NAME_DESC,   // Nom (Z-A)
    DATE_ASC,    // Date (Plus ancien)
    DATE_DESC    // Date (Plus récent)
}

data class HomeUiState(
    val rooms: List<Room> = emptyList(),
    val roomGroups: List<RoomGroup> = emptyList(),
    val selectedGroup: RoomGroup? = null,
    val isLoading: Boolean = true,
    val isExpanded: Boolean = false,
    val error: String? = null,
    val sortOption: SortOption = SortOption.DATE_DESC
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val roomGroupRepository: RoomGroupRepository
) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    private val _roomGroups = MutableStateFlow<List<RoomGroup>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)

    val uiState: StateFlow<HomeUiState> = combine(
        _rooms,
        _roomGroups,
        _isLoading,
        _error,
        _sortOption
    ) { rooms, groups, isLoading, error, sortOption ->
        val sortedRooms = sortRooms(rooms, sortOption)
        val sortedGroups = sortGroups(groups, sortOption)
        
        HomeUiState(
            rooms = sortedRooms,
            roomGroups = sortedGroups,
            isLoading = isLoading,
            error = error,
            sortOption = sortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        loadData()
    }

    // Fonction pour trier les pièces selon l'option de tri sélectionnée
    private fun sortRooms(rooms: List<Room>, sortOption: SortOption): List<Room> {
        return when (sortOption) {
            SortOption.NAME_ASC -> rooms.sortedBy { it.name }
            SortOption.NAME_DESC -> rooms.sortedByDescending { it.name }
            SortOption.DATE_ASC -> rooms.sortedBy { it.createdAt }
            SortOption.DATE_DESC -> rooms.sortedByDescending { it.createdAt }
        }
    }

    // Fonction pour trier les groupes selon l'option de tri sélectionnée
    private fun sortGroups(groups: List<RoomGroup>, sortOption: SortOption): List<RoomGroup> {
        return when (sortOption) {
            SortOption.NAME_ASC -> groups.sortedBy { it.name }
            SortOption.NAME_DESC -> groups.sortedByDescending { it.name }
            // Pour les dates, on garde l'ordre par défaut car RoomGroup n'a pas de champ createdAt
            SortOption.DATE_ASC -> groups
            SortOption.DATE_DESC -> groups
        }
    }

    // Fonction pour changer l'option de tri
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                roomRepository.getAllRooms()
                    .catch { e -> 
                        _error.value = e.message
                        _isLoading.value = false
                    }
                    .collect { rooms -> 
                        _rooms.value = rooms
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            try {
                roomGroupRepository.getAllRoomGroups()
                    .catch { e -> _error.value = e.message }
                    .collect { groups ->
                        _roomGroups.value = groups
                    }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            try {
                roomRepository.deleteRoom(room.id)
                _rooms.value = _rooms.value.filter { it.id != room.id }
                
                // Mettre à jour les groupes
                _roomGroups.value = _roomGroups.value.map { group ->
                    if (room.id in group.roomIds) {
                        val updatedGroup = group.copy(roomIds = group.roomIds - room.id)
                        // Mettre à jour le groupe dans le repository
                        roomGroupRepository.updateRoomGroup(updatedGroup)
                        updatedGroup
                    } else {
                        group
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la suppression de la pièce"
            }
        }
    }

    fun toggleGroupExpanded(groupId: Long) {
        viewModelScope.launch {
            try {
                val group = _roomGroups.value.find { it.id == groupId } ?: return@launch
                val updatedGroup = group.copy(isExpanded = !group.isExpanded)
                roomGroupRepository.updateRoomGroup(updatedGroup)
                _roomGroups.value = _roomGroups.value.map {
                    if (it.id == groupId) updatedGroup else it
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createRoomGroup(name: String, roomIds: Set<Long>) {
        viewModelScope.launch {
            try {
                val newGroup = RoomGroup(
                    name = name,
                    roomIds = roomIds,
                    isExpanded = true
                )
                val id = roomGroupRepository.insertRoomGroup(newGroup)
                _roomGroups.value = _roomGroups.value + newGroup.copy(id = id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteGroup(group: RoomGroup, deleteRooms: Boolean) {
        viewModelScope.launch {
            try {
                if (deleteRooms) {
                    group.roomIds.forEach { roomId ->
                        roomRepository.deleteRoom(roomId)
                    }
                    _rooms.value = _rooms.value.filter { it.id !in group.roomIds }
                }
                roomGroupRepository.deleteRoomGroup(group)
                _roomGroups.value = _roomGroups.value.filter { it.id != group.id }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addRoomToGroup(roomId: Long, groupId: Long) {
        viewModelScope.launch {
            try {
                val group = _roomGroups.value.find { it.id == groupId }
                if (group != null) {
                    val updatedGroup = group.copy(roomIds = group.roomIds + roomId)
                    roomGroupRepository.updateRoomGroup(updatedGroup)
                    _roomGroups.value = _roomGroups.value.map {
                        if (it.id == groupId) updatedGroup else it
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun removeRoomFromGroup(roomId: Long, groupId: Long) {
        viewModelScope.launch {
            try {
                val group = _roomGroups.value.find { it.id == groupId }
                if (group != null) {
                    val updatedRoomIds = group.roomIds - roomId
                    
                    if (updatedRoomIds.isEmpty()) {
                        // Si c'est la dernière pièce, supprimer le groupe
                        roomGroupRepository.deleteRoomGroup(group)
                        _roomGroups.value = _roomGroups.value.filter { it.id != group.id }
                    } else {
                        // Sinon, mettre à jour le groupe avec la pièce retirée
                        val updatedGroup = group.copy(roomIds = updatedRoomIds)
                        roomGroupRepository.updateRoomGroup(updatedGroup)
                        _roomGroups.value = _roomGroups.value.map {
                            if (it.id == groupId) updatedGroup else it
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
