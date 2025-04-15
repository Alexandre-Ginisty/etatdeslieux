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
    val sortOption: SortOption = SortOption.DATE_DESC,
    val searchQuery: String = "",
    val searchResults: List<Room> = emptyList(),
    val currentSearchIndex: Int = -1,
    val totalSearchResults: Int = 0
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
    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<Room>>(emptyList())
    private val _currentSearchIndex = MutableStateFlow(-1)
    private val _totalSearchResults = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        _rooms,
        _roomGroups,
        _isLoading,
        _error,
        _sortOption,
        _searchQuery,
        _searchResults,
        _currentSearchIndex,
        _totalSearchResults
    ) { array ->
        // Utilisation de l'opérateur safe cast pour éviter les avertissements
        val rooms = array[0] as? List<Room> ?: emptyList()
        val groups = array[1] as? List<RoomGroup> ?: emptyList()
        val isLoading = array[2] as? Boolean ?: true
        val error = array[3] as? String?
        val sortOption = array[4] as? SortOption ?: SortOption.DATE_DESC
        val searchQuery = array[5] as? String ?: ""
        val searchResults = array[6] as? List<Room> ?: emptyList()
        val currentSearchIndex = array[7] as? Int ?: -1
        val totalSearchResults = array[8] as? Int ?: 0
        
        val sortedRooms = sortRooms(rooms, sortOption)
        val sortedGroups = sortGroups(groups, sortOption)
        
        HomeUiState(
            rooms = sortedRooms,
            roomGroups = sortedGroups,
            isLoading = isLoading,
            error = error,
            sortOption = sortOption,
            searchQuery = searchQuery,
            searchResults = searchResults,
            currentSearchIndex = currentSearchIndex,
            totalSearchResults = totalSearchResults
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

    /**
     * Met à jour la requête de recherche et filtre les résultats
     * @param query La requête de recherche
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _currentSearchIndex.value = -1
            _totalSearchResults.value = 0
            return
        }
        
        // Filtrer les pièces qui correspondent à la requête
        val results = _rooms.value.filter { room ->
            room.name.contains(query, ignoreCase = true) || 
            (room.description.isNotBlank() && room.description.contains(query, ignoreCase = true))
        }
        
        _searchResults.value = results
        _totalSearchResults.value = results.size
        _currentSearchIndex.value = if (results.isNotEmpty()) 0 else -1
    }
    
    /**
     * Navigue vers le résultat de recherche suivant
     */
    fun nextSearchResult() {
        if (_searchResults.value.isEmpty()) return
        
        _currentSearchIndex.value = (_currentSearchIndex.value + 1) % _searchResults.value.size
    }
    
    /**
     * Navigue vers le résultat de recherche précédent
     */
    fun previousSearchResult() {
        if (_searchResults.value.isEmpty()) return
        
        _currentSearchIndex.value = if (_currentSearchIndex.value <= 0) 
            _searchResults.value.size - 1 
        else 
            _currentSearchIndex.value - 1
    }
    
    /**
     * Réinitialise la recherche
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _currentSearchIndex.value = -1
        _totalSearchResults.value = 0
    }
    
    /**
     * Obtient la pièce actuellement sélectionnée dans les résultats de recherche
     */
    fun getCurrentSearchRoom(): Room? {
        if (_currentSearchIndex.value < 0 || _searchResults.value.isEmpty()) return null
        return _searchResults.value.getOrNull(_currentSearchIndex.value)
    }

    /**
     * Lance une recherche avec la requête actuelle
     */
    fun searchRooms() {
        updateSearchQuery(_searchQuery.value)
    }

    fun clearError() {
        _error.value = null
    }
}
