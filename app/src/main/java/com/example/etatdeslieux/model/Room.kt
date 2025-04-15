package com.example.etatdeslieux.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val size: Float = 0f,
    val floor: Int = 0,
    val creator: String = "",
    val etatType: String = "",
    val etatNumber: Int = 0,
    val groupId: Long = 0,
    var createdAt: LocalDateTime = LocalDateTime.now()
)
