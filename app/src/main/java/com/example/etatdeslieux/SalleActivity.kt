package com.example.etatdeslieux

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SalleActivity : AppCompatActivity() {

    private lateinit var salleTitle: TextView
    private lateinit var salleDescription: TextView
    private lateinit var salleSize: TextView
    private lateinit var salleFloor: TextView
    private lateinit var salleCreator: TextView
    private lateinit var salleEtatType: TextView
    private lateinit var salleEtatNumber: TextView
    private lateinit var photoContainer: LinearLayout
    private lateinit var takePhotoButton: Button

    private var currentPhotoPath: String? = null
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salle)

        // Bind UI components
        salleTitle = findViewById(R.id.salleTitle)
        salleDescription = findViewById(R.id.salleDescription)
        salleSize = findViewById(R.id.salleSize)
        salleFloor = findViewById(R.id.salleFloor)
        salleCreator = findViewById(R.id.salleCreator)
        salleEtatType = findViewById(R.id.salleEtatType)
        salleEtatNumber = findViewById(R.id.salleEtatNumber)
        photoContainer = findViewById(R.id.photoContainer)
        takePhotoButton = findViewById(R.id.takePhotoButton)

        // Initialize ActivityResultLauncher for photo capture
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                currentPhotoPath?.let { path ->
                    saveImageDetails(path, "")
                    displaySavedImages() // Refresh the UI
                } ?: Toast.makeText(this, "Erreur lors de la capture de la photo.", Toast.LENGTH_SHORT).show()
            }
        }

        // Load room details from intent
        val name = intent.getStringExtra("PIECE_NAME") ?: "Titre non défini"
        val description = intent.getStringExtra("PIECE_DESCRIPTION") ?: "Description non définie"
        val size = intent.getFloatExtra("PIECE_SIZE", 0f)
        val floor = intent.getIntExtra("PIECE_FLOOR", 0)
        val creator = intent.getStringExtra("PIECE_CREATOR") ?: "Créateur inconnu"
        val etatType = intent.getStringExtra("PIECE_ETAT_TYPE") ?: "Type non défini"
        val etatNumber = intent.getIntExtra("PIECE_ETAT_NUMBER", 0)

        // Populate UI
        salleTitle.text = name
        salleDescription.text = description
        salleSize.text = "Taille : $size m²"
        salleFloor.text = "Étage : $floor"
        salleCreator.text = "Créateur : $creator"
        salleEtatType.text = "Type d'état des lieux : $etatType"
        salleEtatNumber.text = "Numéro d'état des lieux : $etatNumber"

        // Set up button for adding photos
        takePhotoButton.setOnClickListener { dispatchTakePictureIntent() }

        // Display saved photos
        displaySavedImages()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePhotoLauncher.launch(takePictureIntent)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun saveImageDetails(imagePath: String, description: String) {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(storageDir, "image_details.json")

        val json = if (file.exists()) file.readText() else "{}"
        val jsonObject = JSONObject(json)

        val photos = jsonObject.optJSONArray("photos") ?: JSONArray()

        val existingPhoto = (0 until photos.length()).map { photos.getJSONObject(it) }
            .firstOrNull { it.getString("path") == imagePath }

        if (existingPhoto != null) {
            existingPhoto.put("description", description)
        } else {
            val newPhoto = JSONObject().apply {
                put("path", imagePath)
                put("description", description)
                put("room", salleTitle.text.toString()) // Associate photo with room
            }
            photos.put(newPhoto)
        }

        jsonObject.put("photos", photos)
        file.writeText(jsonObject.toString())
    }

    private fun loadImageDetails(): JSONArray {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(storageDir, "image_details.json")
        return if (file.exists()) {
            val json = file.readText()
            val jsonObject = JSONObject(json)
            jsonObject.optJSONArray("photos") ?: JSONArray()
        } else {
            JSONArray()
        }
    }

    private fun displaySavedImages() {
        photoContainer.removeAllViews()
        val photos = loadImageDetails()
        for (i in 0 until photos.length()) {
            val photo = photos.getJSONObject(i)
            val path = photo.getString("path")
            val description = photo.getString("description")
            if (photo.optString("room") == salleTitle.text.toString()) {
                addPhotoToContainer(path, description)
            }
        }
    }

    private fun deletePhoto(imagePath: String) {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(storageDir, "image_details.json")

        if (file.exists()) {
            val json = file.readText()
            val jsonObject = JSONObject(json)
            val photos = jsonObject.optJSONArray("photos") ?: JSONArray()

            val filteredPhotos = JSONArray()
            for (i in 0 until photos.length()) {
                val photo = photos.getJSONObject(i)
                if (photo.getString("path") != imagePath) {
                    filteredPhotos.put(photo)
                }
            }

            jsonObject.put("photos", filteredPhotos)
            file.writeText(jsonObject.toString())
        }

        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            imageFile.delete()
        }

        displaySavedImages()
    }

    private fun addPhotoToContainer(imagePath: String, description: String) {
        // Inflate the photo item layout
        val photoLayout = layoutInflater.inflate(R.layout.photo_item, photoContainer, false) as LinearLayout

        // Find views in the photo item
        val imageView = photoLayout.findViewById<ImageView>(R.id.photoImage)
        val viewDescriptionButton = photoLayout.findViewById<ImageButton>(R.id.viewDescriptionButton)
        val editDescriptionButton = photoLayout.findViewById<ImageButton>(R.id.editDescriptionButton)
        val deletePhotoButton = photoLayout.findViewById<ImageButton>(R.id.deletePhotoButton)

        // Load the image from the given path
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            val imageUri = Uri.fromFile(imageFile)
            imageView.setImageURI(imageUri)
        }

        // Set onClickListener for the "View Description" button
        viewDescriptionButton.setOnClickListener {
            AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Description de la photo")
                .setMessage(description.ifEmpty { "Pas de description disponible." })
                .setPositiveButton("Fermer", null)
                .show()
        }

        // Set onClickListener for the "Edit Description" button
        editDescriptionButton.setOnClickListener {
            val input = EditText(this).apply {
                setText(description)
            }

            AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Modifier la description")
                .setView(input)
                .setPositiveButton("Enregistrer") { _, _ ->
                    val newDescription = input.text.toString()
                    saveImageDetails(imagePath, newDescription)
                    displaySavedImages() // Refresh UI
                }
                .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // Set onClickListener for the "Delete Photo" button
        deletePhotoButton.setOnClickListener {
            AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Supprimer la photo")
                .setMessage("Voulez-vous vraiment supprimer cette photo ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    deletePhoto(imagePath)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // Set onClickListener for the image view to display it in full-screen
        imageView.setOnClickListener {
            val fullScreenIntent = Intent(this, FullScreenImageActivity::class.java)
            fullScreenIntent.putExtra("imagePath", imagePath)
            startActivity(fullScreenIntent)
        }

        // Add the photo layout to the container
        photoContainer.addView(photoLayout)
    }
}
