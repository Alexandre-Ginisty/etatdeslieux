package com.example.etatdeslieux.repository

import com.example.etatdeslieux.data.RoomGroupDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomGroupRepository @Inject constructor(
    private val roomGroupDao: RoomGroupDao
) {
    private var _targetGroupForNewRoom: Long? = null

    fun getTargetGroupForNewRoom(): Long? = _targetGroupForNewRoom

    fun setTargetGroupForNewRoom(groupId: Long?) {
        _targetGroupForNewRoom = groupId
    }

    fun clearTargetGroupForNewRoom() {
        _targetGroupForNewRoom = null
    }

    suspend fun addRoomToGroup(groupId: Long, roomId: Long) {
        val group = roomGroupDao.getGroupById(groupId)
        if (group != null) {
            val updatedRoomIds = group.roomIds + roomId
            roomGroupDao.updateGroup(group.copy(roomIds = updatedRoomIds))
        }
    }
}
