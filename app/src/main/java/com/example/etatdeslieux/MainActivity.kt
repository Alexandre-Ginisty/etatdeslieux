package com.example.etatdeslieux

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.animation.TranslateAnimation
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
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
    private lateinit var spinnerEtatDesLieux: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues avec les bons IDs
        val editTextPieceName = findViewById<EditText>(R.id.editTextPieceName)
        val editTextPieceDescription = findViewById<EditText>(R.id.editTextPieceDescription)
        val editTextPieceSize = findViewById<EditText>(R.id.editTextPieceSize)
        val editTextPieceFloor = findViewById<EditText>(R.id.editTextPieceFloor)
        val editTestPieceCreator = findViewById<EditText>(R.id.editTextPieceCreator)
        spinnerEtatDesLieux = findViewById(R.id.spinnerEtatDesLieux)

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
            intent.putExtra("PIECE_CREATOR", piece.creator)
            startActivity(intent)
        }, { piece, button ->
            // Long clic : afficher les boutons de survol
            selectedPiece = piece
            selectedPieceButton = button
            showActionButtons(button)
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = salleAdapter

        // Configurer le Spinner pour forcer l'utilisateur à choisir une option
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.etat_des_lieux_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEtatDesLieux.adapter = adapter

        // Gérer l'ajout de pièce
        creerPieceButton.setOnClickListener {
            ajouterPiece(editTextPieceName, editTextPieceDescription, editTextPieceSize, editTextPieceFloor, editTestPieceCreator)
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
                intent.putExtra("PIECE_CREATOR", piece.creator)
                startActivity(intent)
                hideActionButtons()
                selectedPiece = null
            }
        }

        overlayLayout.setOnClickListener {
            hideActionButtons()
        }
    }

    private fun ajouterPiece(nameField: EditText, descriptionField: EditText, sizeField: EditText, floorField: EditText, creatorField: EditText) {
        val name = nameField.text.toString()
        val description = descriptionField.text.toString()
        val size = sizeField.text.toString().toFloatOrNull() ?: 0f
        val floor = floorField.text.toString().toIntOrNull() ?: 0
        val creator = creatorField.text.toString()
        val etatSelection = spinnerEtatDesLieux.selectedItem.toString()

        if (etatSelection == "Choisir...") {
            Toast.makeText(this, "Veuillez choisir un état des lieux (Entrée ou Sortie)", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isNotEmpty() && description.isNotEmpty() && size > 0 && floor >= 0 && creator.isNotEmpty()) {
            val etatDesLieuxNumber = pieces.count { it.name == name } + 1

            val piece = Piece(pieces.size, name, description, size, floor, creator, etatSelection, etatDesLieuxNumber)
            pieces.add(piece)
            savePieces()
            salleAdapter.notifyDataSetChanged()

            // Ouvrir la nouvelle pièce après l'avoir créée
            val intent = Intent(this, SalleActivity::class.java).apply {
                putExtra("PIECE_NAME", piece.name)
                putExtra("PIECE_DESCRIPTION", piece.description)
                putExtra("PIECE_SIZE", piece.size)
                putExtra("PIECE_FLOOR", piece.floor)
                putExtra("PIECE_CREATOR", piece.creator)
                putExtra("PIECE_ETAT_TYPE", piece.typeEtatDesLieux)
                putExtra("PIECE_ETAT_NUMBER", piece.etatDesLieuxNumber)
            }
            startActivity(intent)

            // Nettoyer les champs de texte
            nameField.text.clear()
            descriptionField.text.clear()
            sizeField.text.clear()
            floorField.text.clear()
            creatorField.text.clear()
            spinnerEtatDesLieux.setSelection(0)  // Réinitialiser le Spinner à "Choisir..."
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
