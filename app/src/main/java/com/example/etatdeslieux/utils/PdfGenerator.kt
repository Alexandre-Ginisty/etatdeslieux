package com.example.etatdeslieux.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.media.MediaScannerConnection

/**
 * Classe utilitaire pour générer des PDF d'états des lieux
 */
class PdfGenerator(private val context: Context) {

    // Constantes pour la mise en page
    private val pageWidth = 595 // A4 width in points (72 points = 1 inch)
    private val pageHeight = 842 // A4 height in points
    private val margin = 50
    private val titleTextSize = 18f
    private val subtitleTextSize = 14f
    private val normalTextSize = 12f
    private val smallTextSize = 10f
    private val lineHeight = 20f
    private val cellPadding = 10f
    
    // Couleurs
    private val colorBlack = Color.BLACK
    private val colorGray = Color.GRAY
    private val colorLightGray = Color.parseColor("#EEEEEE")
    
    /**
     * Génère un PDF pour un état des lieux
     * @param room La salle pour laquelle générer le PDF
     * @param photos Liste des photos associées à la salle
     * @return Le fichier PDF généré ou null en cas d'erreur
     */
    fun generateRoomPdf(room: Room, photos: List<Photo>): File? {
        return try {
            // Créer le répertoire de destination
            val pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }

            // Créer le fichier PDF
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "EtatDesLieux_${room.name.replace(" ", "_")}_$timestamp.pdf"
            val pdfFile = File(pdfDir, fileName)

            // Initialiser le document PDF
            val document = PdfDocument()
            
            // Ajouter la première page avec les informations de la salle
            addRoomInfoPage(document, room)
            
            // Ajouter des pages pour les photos (4 photos par page maximum)
            if (photos.isNotEmpty()) {
                addPhotoPages(document, photos)
            }
            
            // Finaliser et enregistrer le document
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()
            
            // Notifier la galerie qu'un nouveau fichier a été ajouté
            MediaScannerConnection.scanFile(
                context,
                arrayOf(pdfFile.absolutePath),
                arrayOf("application/pdf"),
                null
            )
            
            Log.d("PdfGenerator", "PDF généré avec succès: ${pdfFile.absolutePath}")
            pdfFile
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Erreur lors de la génération du PDF", e)
            null
        }
    }
    
    /**
     * Ajoute une page avec les informations de la salle
     */
    private fun addRoomInfoPage(document: PdfDocument, room: Room) {
        // Créer une page
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Peinture pour le texte
        val paint = Paint()
        paint.color = colorBlack
        
        // Titre
        var yPosition = margin + lineHeight
        paint.textSize = titleTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val title = "État des Lieux - ${room.name}"
        val titleWidth = paint.measureText(title)
        canvas.drawText(title, (pageWidth - titleWidth) / 2, yPosition, paint)
        
        // Date
        yPosition += lineHeight * 2
        paint.textSize = normalTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = "Date: ${dateFormat.format(Date())}"
        val dateWidth = paint.measureText(dateString)
        canvas.drawText(dateString, pageWidth - margin - dateWidth, yPosition, paint)
        
        // Tableau d'informations
        yPosition += lineHeight * 2
        paint.textSize = subtitleTextSize
        canvas.drawText("Informations de la salle", margin.toFloat(), yPosition, paint)
        
        // Dessiner le tableau
        yPosition += lineHeight
        val tableStartY = yPosition
        val colWidth1 = (pageWidth - 2 * margin) * 0.3f
        val colWidth2 = (pageWidth - 2 * margin) * 0.7f
        
        // En-têtes du tableau
        paint.color = colorGray
        canvas.drawRect(margin.toFloat(), tableStartY, margin + colWidth1, tableStartY + lineHeight, paint)
        canvas.drawRect(margin + colWidth1, tableStartY, margin + colWidth1 + colWidth2, tableStartY + lineHeight, paint)
        
        paint.color = Color.WHITE
        paint.textSize = normalTextSize
        canvas.drawText("Propriété", margin + cellPadding, tableStartY + lineHeight - cellPadding, paint)
        canvas.drawText("Valeur", margin + colWidth1 + cellPadding, tableStartY + lineHeight - cellPadding, paint)
        
        // Lignes du tableau
        val tableData = arrayOf(
            Pair("Nom", room.name),
            Pair("Description", room.description),
            Pair("Étage", "${room.floor}"),
            Pair("Type d'état", room.etatType),
            Pair("Numéro d'état", "${room.etatNumber}"),
            Pair("Créateur", room.creator)
        )
        
        paint.color = colorBlack
        var rowY = tableStartY + lineHeight
        
        tableData.forEachIndexed { index, (property, value) ->
            // Alterner les couleurs de fond
            if (index % 2 == 0) {
                paint.color = colorLightGray
                canvas.drawRect(margin.toFloat(), rowY, margin + colWidth1 + colWidth2, rowY + lineHeight, paint)
            }
            
            paint.color = colorBlack
            canvas.drawText(property, margin + cellPadding, rowY + lineHeight - cellPadding, paint)
            
            // Gérer les valeurs longues avec retour à la ligne si nécessaire
            val maxTextWidth = colWidth2 - 2 * cellPadding
            if (paint.measureText(value) > maxTextWidth) {
                // Découper le texte en plusieurs lignes
                val words = value.split(" ")
                var line = ""
                var lineY = rowY + lineHeight - cellPadding
                
                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"
                    if (paint.measureText(testLine) <= maxTextWidth) {
                        line = testLine
                    } else {
                        canvas.drawText(line, margin + colWidth1 + cellPadding, lineY, paint)
                        lineY += lineHeight
                        line = word
                    }
                }
                
                if (line.isNotEmpty()) {
                    canvas.drawText(line, margin + colWidth1 + cellPadding, lineY, paint)
                }
                
                // Ajuster la hauteur de la ligne
                rowY += lineHeight * ((lineY - (rowY + lineHeight - cellPadding)) / lineHeight + 1).toInt()
            } else {
                canvas.drawText(value, margin + colWidth1 + cellPadding, rowY + lineHeight - cellPadding, paint)
                rowY += lineHeight
            }
        }
        
        // Finaliser la page
        document.finishPage(page)
    }
    
    /**
     * Ajoute des pages avec les photos (4 photos par page maximum)
     */
    private fun addPhotoPages(document: PdfDocument, photos: List<Photo>) {
        val photosPerPage = 4
        val pages = (photos.size + photosPerPage - 1) / photosPerPage // Arrondi supérieur
        
        for (pageIndex in 0 until pages) {
            val pageNumber = pageIndex + 2 // La première page est pour les infos de la salle
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            // Peinture pour le texte
            val paint = Paint()
            paint.color = colorBlack
            
            // Titre de la page
            var yPosition = margin + lineHeight
            paint.textSize = titleTextSize
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val title = "Photos (Page ${pageIndex + 1}/${pages})"
            val titleWidth = paint.measureText(title)
            canvas.drawText(title, (pageWidth - titleWidth) / 2, yPosition, paint)
            
            // Dessiner les photos
            yPosition += lineHeight * 2
            val photoWidth = (pageWidth - 2 * margin - 20) / 2 // 2 photos par ligne, 20 d'espacement
            val photoHeight = photoWidth * 0.75f // Ratio 4:3
            
            for (i in 0 until photosPerPage) {
                val photoIndex = pageIndex * photosPerPage + i
                if (photoIndex >= photos.size) break
                
                val photo = photos[photoIndex]
                val row = i / 2
                val col = i % 2
                
                val x = margin + col * (photoWidth + 20)
                val y = yPosition + row * (photoHeight + lineHeight * 3)
                
                // Charger et dessiner l'image
                val bitmap = loadBitmapFromUri(photo.uri)
                if (bitmap != null) {
                    // Redimensionner le bitmap pour qu'il corresponde à la taille souhaitée
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, photoWidth.toInt(), photoHeight.toInt(), true)
                    canvas.drawBitmap(scaledBitmap, x.toFloat(), y, null)
                    
                    // Dessiner un cadre autour de l'image
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    canvas.drawRect(x.toFloat(), y,
                        (x + photoWidth).toFloat(), y + photoHeight, paint)
                    paint.style = Paint.Style.FILL
                    
                    // Ajouter le commentaire sous l'image
                    paint.textSize = smallTextSize
                    val commentY = y + photoHeight + lineHeight
                    
                    if (photo.comment.isNotEmpty()) {
                        // Découper le commentaire en plusieurs lignes si nécessaire
                        val maxTextWidth = photoWidth
                        val words = photo.comment.split(" ")
                        var line = ""
                        var lineY = commentY
                        
                        for (word in words) {
                            val testLine = if (line.isEmpty()) word else "$line $word"
                            if (paint.measureText(testLine) <= maxTextWidth) {
                                line = testLine
                            } else {
                                canvas.drawText(line, x.toFloat(), lineY, paint)
                                lineY += lineHeight
                                line = word
                            }
                        }
                        
                        if (line.isNotEmpty()) {
                            canvas.drawText(line, x.toFloat(), lineY, paint)
                        }
                    }
                } else {
                    // Dessiner un rectangle avec un message d'erreur
                    paint.color = Color.LTGRAY
                    canvas.drawRect(x.toFloat(), y,
                        (x + photoWidth).toFloat(), y + photoHeight, paint)
                    
                    paint.color = Color.RED
                    paint.textSize = normalTextSize
                    val errorMsg = "Image non disponible"
                    val errorWidth = paint.measureText(errorMsg)
                    canvas.drawText(
                        errorMsg,
                        x + (photoWidth - errorWidth) / 2,
                        y + photoHeight / 2,
                        paint
                    )
                    
                    paint.color = colorBlack
                }
            }
            
            // Finaliser la page
            document.finishPage(page)
        }
    }
    
    /**
     * Charge un bitmap à partir d'une URI
     */
    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.e("PdfGenerator", "Erreur lors du chargement de l'image: $uriString", e)
            null
        }
    }
}
