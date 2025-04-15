package com.example.etatdeslieux.data.dao

import androidx.room.*
import com.example.etatdeslieux.model.RoomGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomGroupDao {
    @Query("SELECT * FROM room_groups")
    fun getAllRoomGroups(): Flow<List<RoomGroup>>

    @Query("SELECT * FROM room_groups WHERE id = :groupId")
    fun getRoomGroupById(groupId: Long): Flow<RoomGroup?>

    @Insert
    suspend fun insertRoomGroup(roomGroup: RoomGroup): Long

    @Update
    suspend fun updateRoomGroup(roomGroup: RoomGroup)

    @Delete
    suspend fun deleteRoomGroup(roomGroup: RoomGroup)
}
