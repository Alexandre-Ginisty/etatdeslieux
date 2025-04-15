package com.example.etatdeslieux.data

import androidx.room.*
import com.example.etatdeslieux.model.RoomGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomGroupDao {
    @Query("SELECT * FROM room_groups")
    fun getAllGroups(): Flow<List<RoomGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: RoomGroup)

    @Update
    suspend fun updateGroup(group: RoomGroup)

    @Delete
    suspend fun deleteGroup(group: RoomGroup)

    @Query("SELECT * FROM room_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): RoomGroup?
}
