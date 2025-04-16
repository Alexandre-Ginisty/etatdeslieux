package com.example.etatdeslieux.data.dao

import androidx.room.*
import com.example.etatdeslieux.model.Item
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations sur les objets dans la base de données
 */
@Dao
interface ItemDao {
    /**
     * Insère un nouvel objet dans la base de données
     * @param item L'objet à insérer
     * @return L'ID de l'objet inséré
     */
    @Insert
    suspend fun insertItem(item: Item): Long

    /**
     * Met à jour un objet existant dans la base de données
     * @param item L'objet à mettre à jour
     */
    @Update
    suspend fun updateItem(item: Item)

    /**
     * Supprime un objet de la base de données
     * @param item L'objet à supprimer
     */
    @Delete
    suspend fun deleteItem(item: Item)

    /**
     * Récupère un objet par son ID
     * @param itemId L'ID de l'objet à récupérer
     * @return L'objet correspondant à l'ID
     */
    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): Item?

    /**
     * Récupère tous les objets d'une pièce
     * @param roomId L'ID de la pièce
     * @return Un flux contenant la liste des objets de la pièce
     */
    @Query("SELECT * FROM items WHERE roomId = :roomId ORDER BY createdAt DESC")
    fun getItemsByRoomId(roomId: Long): Flow<List<Item>>

    /**
     * Supprime tous les objets d'une pièce
     * @param roomId L'ID de la pièce
     */
    @Query("DELETE FROM items WHERE roomId = :roomId")
    suspend fun deleteItemsByRoomId(roomId: Long)
}
