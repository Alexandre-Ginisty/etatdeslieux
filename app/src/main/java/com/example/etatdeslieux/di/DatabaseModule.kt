package com.example.etatdeslieux.di

import android.content.Context
import com.example.etatdeslieux.data.AppDatabase
import com.example.etatdeslieux.data.dao.PhotoDao
import com.example.etatdeslieux.data.dao.RoomDao
import com.example.etatdeslieux.data.dao.RoomGroupDao
import com.example.etatdeslieux.data.repository.PhotoRepository
import com.example.etatdeslieux.data.repository.RoomRepository
import com.example.etatdeslieux.data.repository.RoomGroupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideRoomDao(database: AppDatabase): RoomDao {
        return database.roomDao()
    }

    @Provides
    fun providePhotoDao(database: AppDatabase): PhotoDao {
        return database.photoDao()
    }

    @Provides
    fun provideRoomGroupDao(database: AppDatabase): RoomGroupDao {
        return database.roomGroupDao()
    }

    @Singleton
    @Provides
    fun provideRoomRepository(roomDao: RoomDao): RoomRepository {
        return RoomRepository(roomDao)
    }

    @Singleton
    @Provides
    fun providePhotoRepository(photoDao: PhotoDao): PhotoRepository {
        return PhotoRepository(photoDao)
    }

    @Singleton
    @Provides
    fun provideRoomGroupRepository(roomGroupDao: RoomGroupDao): RoomGroupRepository {
        return RoomGroupRepository(roomGroupDao)
    }
}
