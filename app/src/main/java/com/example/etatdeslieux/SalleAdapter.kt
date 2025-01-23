package com.example.etatdeslieux

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class SalleAdapter(
    private val pieces: List<Piece>,
    private val clickListener: (Piece) -> Unit,
    private val longClickListener: (Piece, Button) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<SalleAdapter.SalleViewHolder>() {

    class SalleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val salleButton: Button = itemView.findViewById(R.id.salleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_salle, parent, false)
        return SalleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalleViewHolder, position: Int) {
        val piece = pieces[position]

        // Mettre le nom de la pièce, le type et le numéro sur le bouton
        holder.salleButton.text = "${piece.name} - ${piece.typeEtatDesLieux} #${piece.etatDesLieuxNumber}"

        // Clic normal
        holder.salleButton.setOnClickListener {
            clickListener(piece)
        }

        // Long clic pour afficher les actions
        holder.salleButton.setOnLongClickListener {
            longClickListener(piece, holder.salleButton)
            true
        }

        // Drag-and-drop
        holder.itemView.setOnTouchListener { _, event ->
            // Si l'utilisateur commence à interagir avec l'élément, déclencher le drag-and-drop
            if (event.actionMasked == android.view.MotionEvent.ACTION_DOWN) {
                onStartDrag(holder)
            }
            false
        }
    }

    override fun getItemCount() = pieces.size
}
