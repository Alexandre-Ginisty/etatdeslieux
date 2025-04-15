package com.example.etatdeslieux.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.etatdeslieux.model.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE roomId = :roomId ORDER BY id DESC")
    fun getPhotosByRoom(roomId: Long): Flow<List<Photo>>

    @Insert
    suspend fun insert(photo: Photo): Long

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("DELETE FROM photos WHERE roomId = :roomId")
    suspend fun deleteAllPhotosFromRoom(roomId: Long)
}
