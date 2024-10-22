package com.example.etatdeslieux

data class Piece(
    val id: Int,
    val name: String,
    val description: String,
    val size: Float,
    val floor: Int,
    val creator: String,
    val typeEtatDesLieux: String,
    val etatDesLieuxNumber: Int,
    val imagePath: String? = null // Chemin vers la photo
)