package com.example.etatdeslieux

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class SalleActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: Button
    private lateinit var salleTitle: TextView
    private lateinit var salleDescription: TextView
    private lateinit var salleSize: TextView
    private lateinit var salleFloor: TextView
    private lateinit var salleCreator: TextView
    private lateinit var salleEtatType: TextView
    private lateinit var salleEtatNumber: TextView
    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPieceImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salle)

        // Lier les vues du XML
        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        salleTitle = findViewById(R.id.salleTitle)
        salleDescription = findViewById(R.id.salleDescription)
        salleSize = findViewById(R.id.salleSize)
        salleFloor = findViewById(R.id.salleFloor)
        salleCreator = findViewById(R.id.salleCreator)
        salleEtatType = findViewById(R.id.salleEtatType)
        salleEtatNumber = findViewById(R.id.salleEtatNumber)

        // Charger les détails de la salle à partir de l'intent
        val name = intent.getStringExtra("PIECE_NAME")
        val description = intent.getStringExtra("PIECE_DESCRIPTION")
        val size = intent.getFloatExtra("PIECE_SIZE", 0f)
        val floor = intent.getIntExtra("PIECE_FLOOR", 0)
        val creator = intent.getStringExtra("PIECE_CREATOR")
        val etatType = intent.getStringExtra("PIECE_ETAT_TYPE")
        val etatNumber = intent.getIntExtra("PIECE_ETAT_NUMBER", 0)
        currentPieceImagePath = intent.getStringExtra("PIECE_IMAGE_PATH")

        // Mettre à jour l'interface utilisateur avec les informations de la salle
        salleTitle.text = name
        salleDescription.text = description
        salleSize.text = "Taille: $size m²"
        salleFloor.text = "Étage: $floor"
        salleCreator.text = "Créateur: $creator"
        salleEtatType.text = "Type d'état des lieux: $etatType"
        salleEtatNumber.text = "Numéro d'état des lieux: $etatNumber"

        // Charger l'image si un chemin est disponible
        if (currentPieceImagePath != null) {
            val imageFile = File(currentPieceImagePath!!)
            if (imageFile.exists()) {
                val imageUri = Uri.fromFile(imageFile)
                imageView.setImageURI(imageUri)
            }
        }

        // Bouton pour prendre une photo
        takePhotoButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Sauvegarder l'image sur le stockage local
            val imageFileName = "IMG_${System.currentTimeMillis()}.jpg"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File(storageDir, imageFileName)

            try {
                val outputStream = FileOutputStream(imageFile)
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                // Mettre à jour l'image de la salle actuelle
                imageView.setImageBitmap(imageBitmap)
                currentPieceImagePath = imageFile.absolutePath

                // Enregistrer le chemin de l'image dans les données de la pièce (via SharedPreferences ou base de données)
                saveImagePathForCurrentPiece(currentPieceImagePath!!)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveImagePathForCurrentPiece(imagePath: String) {
        // Sauvegarder le chemin de l'image pour la pièce actuelle
        val sharedPreferences = getSharedPreferences("pieces", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val pieceId = intent.getIntExtra("PIECE_ID", -1)
        editor.putString("PIECE_IMAGE_PATH_$pieceId", imagePath)
        editor.apply()

        // Recharger l'activité avec les informations mises à jour pour garantir la persistance de l'image
        intent.putExtra("PIECE_IMAGE_PATH", imagePath)
        finish()
        startActivity(intent)
    }
}
