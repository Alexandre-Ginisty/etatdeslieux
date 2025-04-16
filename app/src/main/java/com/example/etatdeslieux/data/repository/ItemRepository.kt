package com.example.etatdeslieux.data.repository

import com.example.etatdeslieux.data.dao.ItemDao
import com.example.etatdeslieux.model.Item
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les opérations sur les objets
 */
@Singleton
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao
) {
    /**
     * Insère un nouvel objet dans la base de données
     * @param item L'objet à insérer
     * @return L'ID de l'objet inséré
     */
    suspend fun insertItem(item: Item): Long {
        return itemDao.insertItem(item)
    }

    /**
     * Met à jour un objet existant dans la base de données
     * @param item L'objet à mettre à jour
     */
    suspend fun updateItem(item: Item) {
        itemDao.updateItem(item)
    }

    /**
     * Supprime un objet de la base de données
     * @param item L'objet à supprimer
     */
    suspend fun deleteItem(item: Item) {
        itemDao.deleteItem(item)
    }

    /**
     * Supprime un objet par son ID
     * @param itemId L'ID de l'objet à supprimer
     */
    suspend fun deleteItemById(itemId: Long) {
        val item = itemDao.getItemById(itemId)
        item?.let {
            itemDao.deleteItem(it)
        }
    }

    /**
     * Récupère un objet par son ID
     * @param itemId L'ID de l'objet à récupérer
     * @return L'objet correspondant à l'ID
     */
    suspend fun getItemById(itemId: Long): Item? {
        return itemDao.getItemById(itemId)
    }

    /**
     * Récupère tous les objets d'une pièce
     * @param roomId L'ID de la pièce
     * @return Un flux contenant la liste des objets de la pièce
     */
    fun getItemsByRoomId(roomId: Long): Flow<List<Item>> {
        return itemDao.getItemsByRoomId(roomId)
    }

    /**
     * Supprime tous les objets d'une pièce
     * @param roomId L'ID de la pièce
     */
    suspend fun deleteItemsByRoomId(roomId: Long) {
        itemDao.deleteItemsByRoomId(roomId)
    }
}
