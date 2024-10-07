package com.example.etatdeslieux

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salle)

        val salleTitle = findViewById<TextView>(R.id.salleTitle)
        val salleDescription = findViewById<TextView>(R.id.salleDescription)
        val salleSize = findViewById<TextView>(R.id.salleSize)
        val salleFloor = findViewById<TextView>(R.id.salleFloor)

        val name = intent.getStringExtra("PIECE_NAME")
        val description = intent.getStringExtra("PIECE_DESCRIPTION")
        val size = intent.getFloatExtra("PIECE_SIZE", 0f)
        val floor = intent.getIntExtra("PIECE_FLOOR", 0)

        salleTitle.text = name
        salleDescription.text = description
        salleSize.text = "Taille: $size m²"
        salleFloor.text = "Étage: $floor"
    }
}
