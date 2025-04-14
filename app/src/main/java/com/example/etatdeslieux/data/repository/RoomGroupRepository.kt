package com.example.etatdeslieux.data.repository

import com.example.etatdeslieux.data.dao.RoomGroupDao
import com.example.etatdeslieux.model.RoomGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomGroupRepository @Inject constructor(
    private val roomGroupDao: RoomGroupDao
) {
    fun getAllRoomGroups(): Flow<List<RoomGroup>> = roomGroupDao.getAllRoomGroups()

    fun getRoomGroupById(groupId: Long): Flow<RoomGroup?> = roomGroupDao.getRoomGroupById(groupId)

    suspend fun insertRoomGroup(roomGroup: RoomGroup): Long = roomGroupDao.insertRoomGroup(roomGroup)

    suspend fun updateRoomGroup(roomGroup: RoomGroup) = roomGroupDao.updateRoomGroup(roomGroup)

    suspend fun deleteRoomGroup(roomGroup: RoomGroup) = roomGroupDao.deleteRoomGroup(roomGroup)
}
