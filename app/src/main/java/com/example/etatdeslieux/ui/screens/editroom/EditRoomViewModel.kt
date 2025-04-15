package com.example.etatdeslieux.ui.screens.editroom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.repository.RoomRepository
import com.example.etatdeslieux.model.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: Long = checkNotNull(savedStateHandle["roomId"])
    val room = roomRepository.getRoomById(roomId)

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _floor = MutableStateFlow("")
    val floor = _floor.asStateFlow()

    private val _etatType = MutableStateFlow("")
    val etatType = _etatType.asStateFlow()

    init {
        viewModelScope.launch {
            room.collect { room ->
                room?.let {
                    _name.value = it.name
                    _description.value = it.description
                    _floor.value = it.floor.toString()
                    _etatType.value = it.etatType
                }
            }
        }
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateFloor(floor: String) {
        _floor.value = floor
    }

    fun updateEtatType(type: String) {
        _etatType.value = type
    }

    fun saveRoom(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentRoom = room.first()
            currentRoom?.let {
                val updatedRoom = it.copy(
                    name = name.value,
                    description = description.value,
                    floor = floor.value.toIntOrNull() ?: 0,
                    etatType = etatType.value
                )
                roomRepository.updateRoom(updatedRoom)
                onSuccess()
            }
        }
    }
}
