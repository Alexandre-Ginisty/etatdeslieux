package com.example.etatdeslieux.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {
    private val paint = Paint()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

    fun exportRoomGroup(roomGroup: RoomGroup, rooms: List<Room>): File {
        val document = PdfDocument()
        var currentPage = 1
        var yPosition = 0f

        // Création de la première page
        var pageInfo = PageInfo.Builder(595, 842, currentPage).create() // A4 en points (72 dpi)
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // En-tête
        yPosition = drawHeader(canvas, roomGroup.name)

        // Contenu
        rooms.forEach { room ->
            // Vérifier si on a besoin d'une nouvelle page
            if (yPosition > 700f) {
                document.finishPage(page)
                currentPage++
                pageInfo = PageInfo.Builder(595, 842, currentPage).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawHeader(canvas, roomGroup.name)
            }

            yPosition = drawRoom(canvas, room, yPosition)
        }

        // Finalisation du document
        document.finishPage(page)

        // Sauvegarde du fichier
        val fileName = "etat_des_lieux_${roomGroup.name}_${dateFormat.format(Date())}.pdf"
            .replace(" ", "_")
            .replace("/", "_")
            .replace(":", "_")
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun drawHeader(canvas: Canvas, title: String): Float {
        var yPosition = 50f

        // Logo (si disponible)
        // canvas.drawBitmap(logo, 50f, yPosition, paint)

        // Titre
        paint.apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(title, 50f, yPosition, paint)
        yPosition += 40f

        // Date
        paint.apply {
            textSize = 14f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText("Date : ${dateFormat.format(Date())}", 50f, yPosition, paint)
        yPosition += 40f

        return yPosition
    }

    private fun drawRoom(canvas: Canvas, room: Room, startY: Float): Float {
        var yPosition = startY

        // Nom de la pièce
        paint.apply {
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(room.name, 50f, yPosition, paint)
        yPosition += 30f

        // Description
        paint.apply {
            textSize = 14f
            typeface = Typeface.DEFAULT
        }
        room.description?.let {
            canvas.drawText(it, 50f, yPosition, paint)
            yPosition += 20f
        }

        // Photos (à implémenter selon le stockage des photos)
        // room.photos.forEach { photo ->
        //     val bitmap = loadPhotoBitmap(photo)
        //     canvas.drawBitmap(bitmap, 50f, yPosition, paint)
        //     yPosition += bitmap.height + 20f
        // }

        yPosition += 30f // Espacement entre les pièces
        return yPosition
    }

    // Méthode à implémenter pour charger les photos
    private fun loadPhotoBitmap(photoPath: String): Bitmap {
        // Implémenter le chargement des photos depuis le stockage
        TODO("Not yet implemented")
    }
}
