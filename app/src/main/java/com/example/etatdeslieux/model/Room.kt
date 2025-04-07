package com.example.etatdeslieux.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val size: Float,
    val floor: Int,
    val creator: String,
    val etatType: String,
    val etatNumber: Int
)
