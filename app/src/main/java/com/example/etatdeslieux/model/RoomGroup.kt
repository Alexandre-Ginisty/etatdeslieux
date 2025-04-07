package com.example.etatdeslieux.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.etatdeslieux.data.Converters

@Entity(tableName = "room_groups")
data class RoomGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @TypeConverters(Converters::class)
    val roomIds: List<Long>,
    val isExpanded: Boolean = false
)
