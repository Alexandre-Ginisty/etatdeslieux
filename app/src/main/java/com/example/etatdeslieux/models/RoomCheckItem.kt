package com.example.etatdeslieux.models

data class RoomCheckItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val category: String,
    val item: String,
    var state: State = State.BON,
    var comment: String = ""
) {
    enum class State {
        BON, MOYEN, MAUVAIS, NON_VERIFIE
    }

    companion object {
        fun getDefaultChecklist(): List<RoomCheckItem> {
            return listOf(
                // Murs
                RoomCheckItem(category = "Murs", item = "Peinture/Tapisserie"),
                RoomCheckItem(category = "Murs", item = "Plinthes"),
                // Sol
                RoomCheckItem(category = "Sol", item = "Revêtement"),
                RoomCheckItem(category = "Sol", item = "État général"),
                // Plafond
                RoomCheckItem(category = "Plafond", item = "État général"),
                RoomCheckItem(category = "Plafond", item = "Points lumineux"),
                // Fenêtres
                RoomCheckItem(category = "Fenêtres", item = "Vitres"),
                RoomCheckItem(category = "Fenêtres", item = "Encadrement"),
                RoomCheckItem(category = "Fenêtres", item = "Poignées"),
                // Électricité
                RoomCheckItem(category = "Électricité", item = "Prises"),
                RoomCheckItem(category = "Électricité", item = "Interrupteurs"),
                // Portes
                RoomCheckItem(category = "Portes", item = "État général"),
                RoomCheckItem(category = "Portes", item = "Serrure/Poignée")
            )
        }
    }
}
