package com.example.etatdeslieux.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Énumération des états possibles pour un objet
 */
enum class ItemCondition {
    MAUVAIS, BON, TRES_BON, NEUF;
    
    companion object {
        fun fromString(value: String): ItemCondition {
            return when (value.uppercase()) {
                "MAUVAIS" -> MAUVAIS
                "BON" -> BON
                "TRES BON" -> TRES_BON
                "NEUF" -> NEUF
                else -> BON // Valeur par défaut
            }
        }
    }
}

/**
 * Représente un objet dans un état des lieux
 */
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Room::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val name: String,
    val quantity: Int,
    val condition: String, // Stocké comme String pour faciliter la sérialisation
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
