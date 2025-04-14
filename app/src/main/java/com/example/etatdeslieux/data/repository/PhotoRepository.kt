package com.example.etatdeslieux.data.repository

import com.example.etatdeslieux.data.dao.PhotoDao
import com.example.etatdeslieux.model.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flow


@Singleton
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao
) {
    fun getPhotosByRoomId(roomId: Long): Flow<List<Photo>> = photoDao.getPhotosByRoomId(roomId)

    suspend fun insertPhoto(photo: Photo): Long = photoDao.insertPhoto(photo)

    suspend fun updatePhoto(photo: Photo) = photoDao.updatePhoto(photo)

    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

    suspend fun deletePhotosByRoomId(roomId: Long) = photoDao.deletePhotosByRoomId(roomId)
}
