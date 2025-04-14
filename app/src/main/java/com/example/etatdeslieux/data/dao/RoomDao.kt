package com.example.etatdeslieux.data.dao

import androidx.room.*
import com.example.etatdeslieux.model.Room
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms")
    fun getAllRooms(): Flow<List<Room>>

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    fun getRoomById(roomId: Long): Flow<Room?>

    @Insert
    suspend fun insertRoom(room: Room): Long

    @Update
    suspend fun updateRoom(room: Room)

    @Query("DELETE FROM rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: Long)
}
