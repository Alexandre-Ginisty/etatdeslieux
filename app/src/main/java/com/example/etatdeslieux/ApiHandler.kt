package com.example.etatdeslieux

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ApiHandler(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("pieces_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getPieces(responseListener: (List<Piece>) -> Unit, errorListener: (Exception) -> Unit) {
        try {
            val piecesJson = sharedPreferences.getString("pieces", null)
            if (piecesJson != null) {
                val pieceType = object : TypeToken<List<Piece>>() {}.type
                val pieces: List<Piece> = gson.fromJson(piecesJson, pieceType)
                responseListener(pieces)
            } else {
                responseListener(emptyList())
            }
        } catch (e: Exception) {
            errorListener(e)
        }
    }

    fun insertPiece(piece: Piece, responseListener: () -> Unit, errorListener: (Exception) -> Unit) {
        try {
            val pieces = getPiecesList().toMutableList()
            val newPiece = piece.copy(id = getNextId(pieces))
            pieces.add(newPiece)
            savePiecesList(pieces)
            responseListener()
        } catch (e: Exception) {
            errorListener(e)
        }
    }

    fun deletePiece(id: Int, responseListener: () -> Unit, errorListener: (Exception) -> Unit) {
        try {
            val pieces = getPiecesList().filter { it.id != id }
            savePiecesList(pieces)
            responseListener()
        } catch (e: Exception) {
            errorListener(e)
        }
    }

    private fun getPiecesList(): List<Piece> {
        val piecesJson = sharedPreferences.getString("pieces", null)
        return if (piecesJson != null) {
            val pieceType = object : TypeToken<List<Piece>>() {}.type
            gson.fromJson(piecesJson, pieceType)
        } else {
            emptyList()
        }
    }

    private fun savePiecesList(pieces: List<Piece>) {
        val editor = sharedPreferences.edit()
        val piecesJson = gson.toJson(pieces)
        editor.putString("pieces", piecesJson)
        editor.apply()
    }

    private fun getNextId(pieces: List<Piece>): Int {
        return if (pieces.isEmpty()) {
            1
        } else {
            pieces.maxOf { it.id } + 1
        }
    }
}
