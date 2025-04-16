package com.example.etatdeslieux.di

import android.content.Context
import androidx.room.Room
import com.example.etatdeslieux.data.AppDatabase
import com.example.etatdeslieux.data.dao.ItemDao
import com.example.etatdeslieux.data.dao.PhotoDao
import com.example.etatdeslieux.data.dao.RoomDao
import com.example.etatdeslieux.data.dao.RoomGroupDao
import com.example.etatdeslieux.data.repository.ItemRepository
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

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
        .fallbackToDestructiveMigration() // Pour la migration vers la version 2
        .build()
    }

    @Provides
    fun provideRoomDao(database: AppDatabase) = database.roomDao()

    @Provides
    fun providePhotoDao(database: AppDatabase) = database.photoDao()

    @Provides
    fun provideRoomGroupDao(database: AppDatabase) = database.roomGroupDao()
    
    @Provides
    fun provideItemDao(database: AppDatabase) = database.itemDao()

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
    
    @Singleton
    @Provides
    fun provideItemRepository(itemDao: ItemDao): ItemRepository {
        return ItemRepository(itemDao)
    }
}
