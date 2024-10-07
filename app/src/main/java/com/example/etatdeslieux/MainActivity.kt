package com.example.etatdeslieux

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val pieces = mutableListOf<Piece>()
    private lateinit var salleAdapter: SalleAdapter
    private lateinit var overlayLayout: FrameLayout
    private lateinit var actionButtonsLayout: LinearLayout
    private lateinit var deleteButton: Button
    private lateinit var openButton: Button
    private var selectedPiece: Piece? = null
    private var selectedPieceButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues avec les bons IDs
        // Initialisation des vues avec les bons IDs
        val editTextPieceName = findViewById<EditText>(R.id.editTextPieceName)
        val editTextPieceDescription = findViewById<EditText>(R.id.editTextPieceDescription)
        val editTextPieceSize = findViewById<EditText>(R.id.editTextPieceSize)
        val editTextPieceFloor = findViewById<EditText>(R.id.editTextPieceFloor)
        val creerPieceButton = findViewById<Button>(R.id.creerPieceButton)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        overlayLayout = findViewById(R.id.overlayLayout)
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout)
        deleteButton = findViewById(R.id.deleteButton)
        openButton = findViewById(R.id.openButton)


        // Charger les données sauvegardées
        loadPieces()

        salleAdapter = SalleAdapter(pieces, { piece ->
            // Clic normal : ouvrir la pièce
            val intent = Intent(this, SalleActivity::class.java)
            intent.putExtra("PIECE_NAME", piece.name)
            intent.putExtra("PIECE_DESCRIPTION", piece.description)
            intent.putExtra("PIECE_SIZE", piece.size)
            intent.putExtra("PIECE_FLOOR", piece.floor)
            startActivity(intent)
        }, { piece, button ->
            // Long clic : afficher les boutons de survol
            selectedPiece = piece
            selectedPieceButton = button
            showActionButtons(button)
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = salleAdapter

        // Gérer l'ajout de pièce
        creerPieceButton.setOnClickListener {
            ajouterPiece(editTextPieceName, editTextPieceDescription, editTextPieceSize, editTextPieceFloor)
        }

        // Gérer les boutons de survol
        deleteButton.setOnClickListener {
            selectedPiece?.let { piece ->
                pieces.remove(piece)
                savePieces()
                salleAdapter.notifyDataSetChanged()
                hideActionButtons()
                selectedPiece = null
            }
        }

        openButton.setOnClickListener {
            selectedPiece?.let { piece ->
                val intent = Intent(this, SalleActivity::class.java)
                intent.putExtra("PIECE_NAME", piece.name)
                intent.putExtra("PIECE_DESCRIPTION", piece.description)
                intent.putExtra("PIECE_SIZE", piece.size)
                intent.putExtra("PIECE_FLOOR", piece.floor)
                startActivity(intent)
                hideActionButtons()
                selectedPiece = null
            }
        }

        overlayLayout.setOnClickListener {
            hideActionButtons()
        }
    }

    private fun ajouterPiece(nameField: EditText, descriptionField: EditText, sizeField: EditText, floorField: EditText) {
        val name = nameField.text.toString()
        val description = descriptionField.text.toString()
        val size = sizeField.text.toString().toFloatOrNull() ?: 0f
        val floor = floorField.text.toString().toIntOrNull() ?: 0

        if (name.isNotEmpty() && description.isNotEmpty() && size > 0 && floor >= 0) {
            val piece = Piece(pieces.size, name, description, size, floor)
            pieces.add(piece)
            savePieces()
            salleAdapter.notifyDataSetChanged()
            nameField.text.clear()
            descriptionField.text.clear()
            sizeField.text.clear()
            floorField.text.clear()
        } else {
            Toast.makeText(this, "Veuillez entrer des informations valides pour la pièce", Toast.LENGTH_SHORT).show()
        }
    }

    // Sauvegarde des pièces dans SharedPreferences
    private fun savePieces() {
        val sharedPreferences = getSharedPreferences("pieces", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(pieces)
        editor.putString("piece_list", json)
        editor.apply()
    }

    // Chargement des pièces depuis SharedPreferences
    private fun loadPieces() {
        val sharedPreferences = getSharedPreferences("pieces", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("piece_list", null)
        if (json != null) {
            val type = object : TypeToken<List<Piece>>() {}.type
            pieces.addAll(gson.fromJson(json, type))
        }
    }

    private fun showActionButtons(button: Button) {
        overlayLayout.visibility = FrameLayout.VISIBLE
        actionButtonsLayout.visibility = LinearLayout.VISIBLE
        val animation = TranslateAnimation(0f, 0f, -button.height.toFloat(), 0f)
        animation.duration = 300
        actionButtonsLayout.startAnimation(animation)
    }

    private fun hideActionButtons() {
        selectedPieceButton?.let { button ->
            val animation = TranslateAnimation(0f, 0f, 0f, -button.height.toFloat())
            animation.duration = 300
            actionButtonsLayout.startAnimation(animation)
        }
        overlayLayout.visibility = FrameLayout.GONE
        actionButtonsLayout.visibility = LinearLayout.GONE
    }
}