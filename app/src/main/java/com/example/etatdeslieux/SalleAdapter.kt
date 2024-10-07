package com.example.etatdeslieux

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class SalleAdapter(
    private val pieces: List<Piece>,
    private val clickListener: (Piece) -> Unit,
    private val longClickListener: (Piece, Button) -> Unit
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
        holder.salleButton.text = piece.name  // Mettre le nom de la pièce sur le bouton
        holder.salleButton.setOnClickListener { clickListener(piece) }
        holder.salleButton.setOnLongClickListener {
            longClickListener(piece, holder.salleButton)
            true
        }
    }

    override fun getItemCount() = pieces.size
}
