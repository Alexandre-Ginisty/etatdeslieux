package com.example.etatdeslieux.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.etatdeslieux.data.converter.DateTimeConverter
import com.example.etatdeslieux.data.converter.SetConverter
import com.example.etatdeslieux.data.dao.PhotoDao
import com.example.etatdeslieux.data.dao.RoomDao
import com.example.etatdeslieux.data.dao.RoomGroupDao
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room as ModelRoom
import com.example.etatdeslieux.model.RoomGroup

@Database(
    entities = [ModelRoom::class, Photo::class, RoomGroup::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateTimeConverter::class, SetConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun photoDao(): PhotoDao
    abstract fun roomGroupDao(): RoomGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
