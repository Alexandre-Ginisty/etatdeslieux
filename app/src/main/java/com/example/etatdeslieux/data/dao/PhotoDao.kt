package com.example.etatdeslieux.data.dao

import androidx.room.*
import com.example.etatdeslieux.model.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE roomId = :roomId")
    fun getPhotosByRoomId(roomId: Long): Flow<List<Photo>>

    @Insert
    suspend fun insertPhoto(photo: Photo): Long

    @Update
    suspend fun updatePhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("DELETE FROM photos WHERE roomId = :roomId")
    suspend fun deletePhotosByRoomId(roomId: Long)
}
