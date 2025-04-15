package com.example.etatdeslieux.data.repository

import com.example.etatdeslieux.data.dao.RoomDao
import com.example.etatdeslieux.model.Room
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flow


@Singleton
class RoomRepository @Inject constructor(
    private val roomDao: RoomDao
) {
    fun getAllRooms(): Flow<List<Room>> = roomDao.getAllRooms()

    fun getRoomById(id: Long): Flow<Room?> = roomDao.getRoomById(id)

    
    suspend fun insertRoom(room: Room): Long = roomDao.insertRoom(room)

    suspend fun updateRoom(room: Room) = roomDao.updateRoom(room)

    suspend fun deleteRoom(roomId: Long) = roomDao.deleteRoom(roomId)
}
