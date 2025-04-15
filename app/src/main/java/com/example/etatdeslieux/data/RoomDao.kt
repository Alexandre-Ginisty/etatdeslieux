package com.example.etatdeslieux.data

import androidx.room.*
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY id DESC")
    fun getAllRooms(): Flow<List<Room>>

    @Query("SELECT * FROM room_groups ORDER BY id DESC")
    fun getAllGroups(): Flow<List<RoomGroup>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun getRoomById(id: Long): Flow<Room?>

    @Query("SELECT * FROM room_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): RoomGroup?

    @Insert
    suspend fun insertRoom(room: Room): Long

    @Insert
    suspend fun insertGroup(group: RoomGroup): Long

    @Update
    suspend fun updateRoom(room: Room)

    @Update
    suspend fun updateGroup(group: RoomGroup)

    @Delete
    suspend fun deleteRoom(room: Room)

    @Delete
    suspend fun deleteGroup(group: RoomGroup)

    @Query("SELECT * FROM rooms WHERE id IN (:roomIds)")
    fun getRoomsInGroup(roomIds: List<Long>): Flow<List<Room>>
}
