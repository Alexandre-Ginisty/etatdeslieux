package com.example.etatdeslieux

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageView = findViewById<ImageView>(R.id.fullScreenImageView)
        val imagePath = intent.getStringExtra("imagePath")

        imagePath?.let {
            val imageFile = File(it)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }

        imageView.setOnClickListener {
            finish() // Ferme l'activité lorsqu'on clique sur l'image
        }
    }
}
