package com.example.etatdeslieux.di

import android.content.Context
import com.example.etatdeslieux.data.AppDatabase
import com.example.etatdeslieux.data.PhotoDao
import com.example.etatdeslieux.data.RoomDao
import com.example.etatdeslieux.data.RoomGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideRoomDao(database: AppDatabase): RoomDao {
        return database.roomDao()
    }

    @Provides
    @Singleton
    fun providePhotoDao(database: AppDatabase): PhotoDao {
        return database.photoDao()
    }

    @Provides
    @Singleton
    fun provideRoomGroupDao(database: AppDatabase): RoomGroupDao {
        return database.roomGroupDao()
    }
}
