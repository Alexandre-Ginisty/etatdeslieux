package com.example.etatdeslieux.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.etatdeslieux.data.converter.SetConverter
import java.time.LocalDateTime

@Entity(tableName = "room_groups")
data class RoomGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    @TypeConverters(SetConverter::class)
    val roomIds: Set<Long> = emptySet(),
    val isExpanded: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
