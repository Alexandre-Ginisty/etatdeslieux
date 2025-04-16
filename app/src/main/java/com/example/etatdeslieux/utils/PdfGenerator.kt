package com.example.etatdeslieux.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.Item
import com.example.etatdeslieux.model.ItemCondition
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.media.MediaScannerConnection
import android.graphics.LinearGradient
import android.graphics.Path
import android.graphics.Shader

/**
 * Classe utilitaire pour générer des PDF d'états des lieux avec un design moderne et professionnel
 */
class PdfGenerator(private val context: Context) {

    // Constantes pour la mise en page
    private val pageWidth = 595 // A4 width in points (72 points = 1 inch)
    private val pageHeight = 842 // A4 height in points
    private val margin = 50
    private val titleTextSize = 24f
    private val subtitleTextSize = 18f
    private val headerTextSize = 16f
    private val normalTextSize = 12f
    private val smallTextSize = 10f
    private val lineHeight = 20f
    private val cellPadding = 10f
    private val cornerRadius = 8f
    
    // Couleurs modernes
    private val colorPrimary = Color.parseColor("#1976D2")         // Bleu primaire
    private val colorPrimaryDark = Color.parseColor("#0D47A1")     // Bleu foncé
    private val colorPrimaryLight = Color.parseColor("#BBDEFB")    // Bleu clair
    private val colorAccent = Color.parseColor("#FF5722")          // Orange accent
    private val colorBlack = Color.parseColor("#212121")           // Noir profond
    private val colorDarkGray = Color.parseColor("#757575")        // Gris foncé
    private val colorMediumGray = Color.parseColor("#BDBDBD")      // Gris moyen
    private val colorLightGray = Color.parseColor("#F5F5F5")       // Gris clair
    private val colorWhite = Color.parseColor("#FFFFFF")           // Blanc
    
    // Couleurs pour les états d'objets
    private val colorStateNeuf = Color.parseColor("#4CAF50")       // Vert
    private val colorStateTresBon = Color.parseColor("#8BC34A")    // Vert clair
    private val colorStateBon = Color.parseColor("#2196F3")        // Bleu
    private val colorStateMauvais = Color.parseColor("#FF5722")    // Orange
    
    /**
     * Génère un PDF pour un état des lieux avec un design moderne
     * @param room La salle pour laquelle générer le PDF
     * @param photos Liste des photos associées à la salle
     * @param items Liste des objets associés à la salle
     * @return Le fichier PDF généré ou null en cas d'erreur
     */
    fun generateRoomPdf(room: Room, photos: List<Photo>, items: List<Item> = emptyList()): File? {
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
            
            // Ajouter la page de couverture
            addCoverPage(document, room)
            
            // Ajouter la page d'informations de la salle
            addRoomInfoPage(document, room)
            
            // Ajouter une page avec les objets si la liste n'est pas vide
            if (items.isNotEmpty()) {
                addItemsPage(document, items)
            }
            
            // Ajouter des pages pour les photos
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
     * Ajoute une page de couverture élégante
     */
    private fun addCoverPage(document: PdfDocument, room: Room) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Fond dégradé
        val paint = Paint()
        val gradient = LinearGradient(
            0f, 0f, 0f, pageHeight.toFloat(),
            colorPrimary, colorPrimaryDark,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paint)
        
        // Motif décoratif (cercles et lignes)
        paint.shader = null
        paint.color = colorPrimaryLight
        paint.alpha = 40 // Semi-transparent
        paint.style = Paint.Style.FILL
        
        // Dessiner des cercles décoratifs
        for (i in 0..5) {
            val radius = (50 + i * 30).toFloat()
            canvas.drawCircle(pageWidth.toFloat(), 0f, radius, paint)
            canvas.drawCircle(0f, pageHeight.toFloat(), radius, paint)
        }
        
        // Rectangle central blanc
        paint.color = colorWhite
        paint.alpha = 240
        paint.style = Paint.Style.FILL
        val rectF = RectF(
            margin.toFloat() * 1.5f,
            pageHeight / 3f,
            pageWidth - margin.toFloat() * 1.5f,
            pageHeight - pageHeight / 3f
        )
        canvas.drawRoundRect(rectF, cornerRadius * 2, cornerRadius * 2, paint)
        
        // Bordure du rectangle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = colorPrimaryLight
        paint.alpha = 255
        canvas.drawRoundRect(rectF, cornerRadius * 2, cornerRadius * 2, paint)
        
        // Titre principal
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = colorPrimaryDark
        paint.textSize = titleTextSize * 1.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        
        val title = "ÉTAT DES LIEUX"
        canvas.drawText(title, pageWidth / 2f, pageHeight / 2f - titleTextSize, paint)
        
        // Nom de la salle
        paint.textSize = titleTextSize
        paint.color = colorBlack
        canvas.drawText(room.name, pageWidth / 2f, pageHeight / 2f + titleTextSize, paint)
        
        // Informations supplémentaires
        paint.textSize = subtitleTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = colorDarkGray
        
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)
        val dateString = dateFormat.format(Date())
        canvas.drawText(dateString, pageWidth / 2f, pageHeight / 2f + titleTextSize * 2.5f, paint)
        
        // Créateur
        paint.textSize = normalTextSize
        canvas.drawText("Créé par: ${room.creator}", pageWidth / 2f, pageHeight / 2f + titleTextSize * 3.5f, paint)
        
        // Numéro d'état
        canvas.drawText("Référence: ${room.etatNumber}", pageWidth / 2f, pageHeight / 2f + titleTextSize * 4.5f, paint)
        
        // Pied de page
        paint.textSize = smallTextSize
        paint.color = colorWhite
        paint.alpha = 200
        canvas.drawText("Document généré automatiquement", pageWidth / 2f, pageHeight - margin / 2f, paint)
        
        document.finishPage(page)
    }
    
    /**
     * Ajoute une page avec les informations de la salle
     */
    private fun addRoomInfoPage(document: PdfDocument, room: Room) {
        // Créer une page
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Peinture pour le texte
        val paint = Paint()
        
        // En-tête avec dégradé
        val headerPaint = Paint()
        val headerGradient = LinearGradient(
            0f, 0f, pageWidth.toFloat(), 0f,
            colorPrimary, colorPrimaryDark,
            Shader.TileMode.CLAMP
        )
        headerPaint.shader = headerGradient
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), margin * 1.5f, headerPaint)
        
        // Titre dans l'en-tête
        paint.shader = null
        paint.color = colorWhite
        paint.textSize = titleTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Informations Détaillées", pageWidth / 2f, margin * 0.9f, paint)
        
        // Réinitialiser l'alignement du texte
        paint.textAlign = Paint.Align.LEFT
        
        // Sous-titre avec le nom de la pièce
        var yPosition = margin * 2f
        paint.color = colorPrimaryDark
        paint.textSize = subtitleTextSize
        val roomTitle = "Pièce: ${room.name}"
        canvas.drawText(roomTitle, margin.toFloat(), yPosition, paint)
        
        // Date
        paint.textSize = normalTextSize
        paint.color = colorDarkGray
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.FRANCE)
        val dateString = "Date: ${dateFormat.format(Date())}"
        val dateWidth = paint.measureText(dateString)
        canvas.drawText(dateString, pageWidth - margin - dateWidth, yPosition, paint)
        
        // Ligne de séparation
        yPosition += lineHeight
        paint.color = colorPrimaryLight
        paint.strokeWidth = 2f
        canvas.drawLine(margin.toFloat(), yPosition, pageWidth - margin.toFloat(), yPosition, paint)
        
        // Tableau d'informations avec coins arrondis
        yPosition += lineHeight * 1.5f
        paint.textSize = subtitleTextSize
        paint.color = colorBlack
        canvas.drawText("Caractéristiques de la pièce", margin.toFloat(), yPosition, paint)
        
        // Dessiner le tableau avec coins arrondis
        yPosition += lineHeight
        val tableStartY = yPosition
        val colWidth1 = (pageWidth - 2 * margin) * 0.3f
        val colWidth2 = (pageWidth - 2 * margin) * 0.7f
        
        // Fond du tableau
        paint.color = colorLightGray
        paint.style = Paint.Style.FILL
        val tableRect = RectF(
            margin.toFloat(),
            tableStartY,
            margin + colWidth1 + colWidth2,
            tableStartY + lineHeight * 6 // Hauteur pour 6 lignes
        )
        canvas.drawRoundRect(tableRect, cornerRadius, cornerRadius, paint)
        
        // En-têtes du tableau
        val headerRect = RectF(
            margin.toFloat(),
            tableStartY,
            margin + colWidth1 + colWidth2,
            tableStartY + lineHeight
        )
        paint.color = colorPrimary
        canvas.drawRoundRect(
            RectF(headerRect.left, headerRect.top, headerRect.right, headerRect.bottom + cornerRadius),
            cornerRadius, cornerRadius, paint
        )
        
        // Ligne verticale de séparation dans l'en-tête
        paint.color = colorWhite
        paint.strokeWidth = 2f
        canvas.drawLine(
            margin + colWidth1,
            tableStartY + cornerRadius / 2,
            margin + colWidth1,
            tableStartY + lineHeight - cornerRadius / 2,
            paint
        )
        
        // Texte des en-têtes
        paint.color = colorWhite
        paint.textSize = headerTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Propriété", margin + cellPadding, tableStartY + lineHeight - cellPadding, paint)
        canvas.drawText("Valeur", margin + colWidth1 + cellPadding, tableStartY + lineHeight - cellPadding, paint)
        
        // Données du tableau
        val tableData = arrayOf(
            Pair("Nom", room.name),
            Pair("Description", room.description.ifEmpty { "Non spécifiée" }),
            Pair("Étage", "${room.floor}"),
            Pair("Type d'état", room.etatType),
            Pair("Numéro d'état", "${room.etatNumber}"),
            Pair("Créateur", room.creator)
        )
        
        // Lignes du tableau
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var rowY = tableStartY + lineHeight
        
        tableData.forEachIndexed { index, (property, value) ->
            // Alterner les couleurs de fond
            if (index % 2 == 0) {
                paint.color = colorWhite
            } else {
                paint.color = colorLightGray
            }
            
            // Dessiner le fond de la ligne
            val rowRect = RectF(
                margin.toFloat(),
                rowY,
                margin + colWidth1 + colWidth2,
                rowY + lineHeight
            )
            canvas.drawRect(rowRect, paint)
            
            // Ligne verticale de séparation
            paint.color = colorMediumGray
            paint.strokeWidth = 1f
            canvas.drawLine(
                margin + colWidth1,
                rowY,
                margin + colWidth1,
                rowY + lineHeight,
                paint
            )
            
            // Ligne horizontale de séparation
            if (index < tableData.size - 1) {
                canvas.drawLine(
                    margin.toFloat(),
                    rowY + lineHeight,
                    margin + colWidth1 + colWidth2,
                    rowY + lineHeight,
                    paint
                )
            }
            
            // Texte des propriétés
            paint.color = colorPrimaryDark
            paint.textSize = normalTextSize
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(property, margin + cellPadding, rowY + lineHeight - cellPadding, paint)
            
            // Texte des valeurs
            paint.color = colorBlack
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
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
                        lineY += lineHeight / 2
                        line = word
                    }
                }
                
                if (line.isNotEmpty()) {
                    canvas.drawText(line, margin + colWidth1 + cellPadding, lineY, paint)
                }
                
                // Ajuster la hauteur de la ligne si nécessaire
                val additionalLines = ((lineY - (rowY + lineHeight - cellPadding)) / (lineHeight / 2)).toInt()
                if (additionalLines > 0) {
                    rowY += additionalLines * (lineHeight / 2)
                }
            } else {
                canvas.drawText(value, margin + colWidth1 + cellPadding, rowY + lineHeight - cellPadding, paint)
            }
            
            // Passer à la ligne suivante
            rowY += lineHeight
        }
        
        // Ajouter une note explicative
        yPosition = rowY + lineHeight * 2
        paint.color = colorDarkGray
        paint.textSize = smallTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("* Ce document présente les informations détaillées de la pièce et son état actuel.", 
            margin.toFloat(), yPosition, paint)
        yPosition += lineHeight
        canvas.drawText("* Les photos et l'inventaire des objets sont présentés dans les pages suivantes.", 
            margin.toFloat(), yPosition, paint)
        
        // Pied de page
        paint.color = colorPrimary
        paint.textSize = smallTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page 2/${document.pages.size + 3}", pageWidth / 2f, pageHeight - margin / 2f, paint)
        
        // Finaliser la page
        document.finishPage(page)
    }
    
    /**
     * Ajoute une page avec les objets
     */
    private fun addItemsPage(document: PdfDocument, items: List<Item>) {
        // Créer une page
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        
        // En-tête avec dégradé
        val headerPaint = Paint()
        val headerGradient = LinearGradient(
            0f, 0f, pageWidth.toFloat(), 0f,
            colorPrimary, colorPrimaryDark,
            Shader.TileMode.CLAMP
        )
        headerPaint.shader = headerGradient
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), margin * 1.5f, headerPaint)
        
        // Titre dans l'en-tête
        val paint = Paint()
        paint.shader = null
        paint.color = colorWhite
        paint.textSize = titleTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Inventaire des Objets", pageWidth / 2f, margin * 0.9f, paint)
        
        // Réinitialiser l'alignement du texte
        paint.textAlign = Paint.Align.LEFT
        
        // Sous-titre
        var yPosition = margin * 2f
        paint.color = colorPrimaryDark
        paint.textSize = subtitleTextSize
        canvas.drawText("Liste des objets présents dans la pièce", margin.toFloat(), yPosition, paint)
        
        // Ligne de séparation
        yPosition += lineHeight
        paint.color = colorPrimaryLight
        paint.strokeWidth = 2f
        canvas.drawLine(margin.toFloat(), yPosition, pageWidth - margin.toFloat(), yPosition, paint)
        
        // Tableau d'objets
        yPosition += lineHeight * 1.5f
        
        // Définir les largeurs des colonnes
        val colWidth1 = (pageWidth - 2 * margin) * 0.30f  // Nom
        val colWidth2 = (pageWidth - 2 * margin) * 0.10f  // Quantité
        val colWidth3 = (pageWidth - 2 * margin) * 0.20f  // État
        val colWidth4 = (pageWidth - 2 * margin) * 0.40f  // Commentaire
        
        // Fond du tableau avec coins arrondis
        if (items.isNotEmpty()) {
            paint.color = colorLightGray
            paint.style = Paint.Style.FILL
            val tableHeight = lineHeight * (items.size + 1) // +1 pour l'en-tête
            val tableRect = RectF(
                margin.toFloat(),
                yPosition,
                margin + colWidth1 + colWidth2 + colWidth3 + colWidth4,
                yPosition + tableHeight
            )
            canvas.drawRoundRect(tableRect, cornerRadius, cornerRadius, paint)
        }
        
        // En-têtes du tableau
        val headerRect = RectF(
            margin.toFloat(),
            yPosition,
            margin + colWidth1 + colWidth2 + colWidth3 + colWidth4,
            yPosition + lineHeight
        )
        paint.color = colorPrimary
        canvas.drawRoundRect(
            RectF(headerRect.left, headerRect.top, headerRect.right, headerRect.bottom + cornerRadius),
            cornerRadius, cornerRadius, paint
        )
        
        // Lignes verticales du tableau (en-tête)
        paint.color = colorWhite
        paint.strokeWidth = 2f
        
        // Ligne verticale après "Nom"
        canvas.drawLine(
            margin + colWidth1,
            yPosition + cornerRadius / 2,
            margin + colWidth1,
            yPosition + lineHeight - cornerRadius / 2,
            paint
        )
        
        // Ligne verticale après "Quantité"
        canvas.drawLine(
            margin + colWidth1 + colWidth2,
            yPosition + cornerRadius / 2,
            margin + colWidth1 + colWidth2,
            yPosition + lineHeight - cornerRadius / 2,
            paint
        )
        
        // Ligne verticale après "État"
        canvas.drawLine(
            margin + colWidth1 + colWidth2 + colWidth3,
            yPosition + cornerRadius / 2,
            margin + colWidth1 + colWidth2 + colWidth3,
            yPosition + lineHeight - cornerRadius / 2,
            paint
        )
        
        // Texte des en-têtes
        paint.color = colorWhite
        paint.textSize = headerTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Nom", margin + cellPadding, yPosition + lineHeight - cellPadding, paint)
        canvas.drawText("Qté", margin + colWidth1 + cellPadding, yPosition + lineHeight - cellPadding, paint)
        canvas.drawText("État", margin + colWidth1 + colWidth2 + cellPadding, yPosition + lineHeight - cellPadding, paint)
        canvas.drawText("Commentaire", margin + colWidth1 + colWidth2 + colWidth3 + cellPadding, yPosition + lineHeight - cellPadding, paint)
        
        // Lignes du tableau
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var rowY = yPosition + lineHeight
        
        // Couleurs pour les états
        val stateColors = mapOf(
            "Mauvais" to colorStateMauvais,
            "Bon" to colorStateBon,
            "Très Bon" to colorStateTresBon,
            "Neuf" to colorStateNeuf
        )
        
        if (items.isEmpty()) {
            // Afficher un message si aucun objet n'est présent
            paint.color = colorDarkGray
            paint.textSize = normalTextSize
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                "Aucun objet enregistré pour cette pièce",
                pageWidth / 2f,
                rowY + lineHeight * 2,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
        } else {
            items.forEachIndexed { index, item ->
                // Alterner les couleurs de fond
                if (index % 2 == 0) {
                    paint.color = colorWhite
                } else {
                    paint.color = colorLightGray
                }
                
                // Dessiner le fond de la ligne
                val rowRect = RectF(
                    margin.toFloat(),
                    rowY,
                    margin + colWidth1 + colWidth2 + colWidth3 + colWidth4,
                    rowY + lineHeight * 1.2f
                )
                canvas.drawRect(rowRect, paint)
                
                // Lignes verticales de séparation
                paint.color = colorMediumGray
                paint.strokeWidth = 1f
                
                // Ligne verticale après "Nom"
                canvas.drawLine(
                    margin + colWidth1,
                    rowY,
                    margin + colWidth1,
                    rowY + lineHeight * 1.2f,
                    paint
                )
                
                // Ligne verticale après "Quantité"
                canvas.drawLine(
                    margin + colWidth1 + colWidth2,
                    rowY,
                    margin + colWidth1 + colWidth2,
                    rowY + lineHeight * 1.2f,
                    paint
                )
                
                // Ligne verticale après "État"
                canvas.drawLine(
                    margin + colWidth1 + colWidth2 + colWidth3,
                    rowY,
                    margin + colWidth1 + colWidth2 + colWidth3,
                    rowY + lineHeight * 1.2f,
                    paint
                )
                
                // Ligne horizontale de séparation
                if (index < items.size - 1) {
                    canvas.drawLine(
                        margin.toFloat(),
                        rowY + lineHeight * 1.2f,
                        margin + colWidth1 + colWidth2 + colWidth3 + colWidth4,
                        rowY + lineHeight * 1.2f,
                        paint
                    )
                }
                
                // Contenu des cellules
                paint.style = Paint.Style.FILL
                
                // Nom (en gras)
                paint.color = colorPrimaryDark
                paint.textSize = normalTextSize
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(item.name, margin + cellPadding, rowY + lineHeight - cellPadding, paint)
                
                // Quantité
                paint.color = colorBlack
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(item.quantity.toString(), margin + colWidth1 + cellPadding, rowY + lineHeight - cellPadding, paint)
                
                // État (avec couleur et badge)
                val stateColor = stateColors[item.condition] ?: colorBlack
                
                // Badge d'état
                paint.color = stateColor
                val badgeRadius = lineHeight / 4
                val badgeCenterX = margin + colWidth1 + colWidth2 + cellPadding * 2
                val badgeCenterY = rowY + lineHeight / 2
                canvas.drawCircle(badgeCenterX, badgeCenterY, badgeRadius, paint)
                
                // Texte d'état
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(item.condition, margin + colWidth1 + colWidth2 + cellPadding * 3, rowY + lineHeight - cellPadding, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = colorBlack
                
                // Commentaire (avec gestion des retours à la ligne)
                if (item.comment.isNotEmpty()) {
                    val maxTextWidth = colWidth4 - 2 * cellPadding
                    val words = item.comment.split(" ")
                    var line = ""
                    var lineY = rowY + lineHeight - cellPadding
                    
                    for (word in words) {
                        val testLine = if (line.isEmpty()) word else "$line $word"
                        if (paint.measureText(testLine) <= maxTextWidth) {
                            line = testLine
                        } else {
                            canvas.drawText(line, margin + colWidth1 + colWidth2 + colWidth3 + cellPadding, lineY, paint)
                            lineY += lineHeight / 2
                            line = word
                        }
                    }
                    
                    if (line.isNotEmpty()) {
                        canvas.drawText(line, margin + colWidth1 + colWidth2 + colWidth3 + cellPadding, lineY, paint)
                    }
                }
                
                // Passer à la ligne suivante
                rowY += lineHeight * 1.2f
                
                // Vérifier si on atteint la fin de la page
                if (rowY > pageHeight - margin * 2) {
                    // Finaliser la page actuelle
                    document.finishPage(page)
                    
                    // Créer une nouvelle page
                    val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                    val newPage = document.startPage(newPageInfo)
                    val newCanvas = newPage.canvas
                    
                    // En-tête avec dégradé
                    newCanvas.drawRect(0f, 0f, pageWidth.toFloat(), margin * 1.5f, headerPaint)
                    
                    // Titre dans l'en-tête
                    paint.shader = null
                    paint.color = colorWhite
                    paint.textSize = titleTextSize
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textAlign = Paint.Align.CENTER
                    newCanvas.drawText("Inventaire des Objets (suite)", pageWidth / 2f, margin * 0.9f, paint)
                    
                    // Réinitialiser l'alignement du texte
                    paint.textAlign = Paint.Align.LEFT
                    
                    // Réinitialiser le tableau
                    yPosition = margin * 2f
                    
                    // En-têtes du tableau
                    val newHeaderRect = RectF(
                        margin.toFloat(),
                        yPosition,
                        margin + colWidth1 + colWidth2 + colWidth3 + colWidth4,
                        yPosition + lineHeight
                    )
                    paint.color = colorPrimary
                    newCanvas.drawRoundRect(
                        RectF(newHeaderRect.left, newHeaderRect.top, newHeaderRect.right, newHeaderRect.bottom + cornerRadius),
                        cornerRadius, cornerRadius, paint
                    )
                    
                    // Lignes verticales du tableau (en-tête)
                    paint.color = colorWhite
                    paint.strokeWidth = 2f
                    
                    // Ligne verticale après "Nom"
                    newCanvas.drawLine(
                        margin + colWidth1,
                        yPosition + cornerRadius / 2,
                        margin + colWidth1,
                        yPosition + lineHeight - cornerRadius / 2,
                        paint
                    )
                    
                    // Ligne verticale après "Quantité"
                    newCanvas.drawLine(
                        margin + colWidth1 + colWidth2,
                        yPosition + cornerRadius / 2,
                        margin + colWidth1 + colWidth2,
                        yPosition + lineHeight - cornerRadius / 2,
                        paint
                    )
                    
                    // Ligne verticale après "État"
                    newCanvas.drawLine(
                        margin + colWidth1 + colWidth2 + colWidth3,
                        yPosition + cornerRadius / 2,
                        margin + colWidth1 + colWidth2 + colWidth3,
                        yPosition + lineHeight - cornerRadius / 2,
                        paint
                    )
                    
                    // Texte des en-têtes
                    paint.color = colorWhite
                    paint.textSize = headerTextSize
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    newCanvas.drawText("Nom", margin + cellPadding, yPosition + lineHeight - cellPadding, paint)
                    newCanvas.drawText("Qté", margin + colWidth1 + cellPadding, yPosition + lineHeight - cellPadding, paint)
                    newCanvas.drawText("État", margin + colWidth1 + colWidth2 + cellPadding, yPosition + lineHeight - cellPadding, paint)
                    newCanvas.drawText("Commentaire", margin + colWidth1 + colWidth2 + colWidth3 + cellPadding, yPosition + lineHeight - cellPadding, paint)
                    
                    // Mettre à jour les variables pour la nouvelle page
                    canvas = newCanvas
                    rowY = yPosition + lineHeight
                    page = newPage
                }
            }
        }
        
        // Légende des états
        if (items.isNotEmpty()) {
            rowY += lineHeight * 2
            paint.color = colorBlack
            paint.textSize = normalTextSize
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Légende des états :", margin.toFloat(), rowY, paint)
            
            rowY += lineHeight * 1.2f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            // Créer la légende pour chaque état
            val states = listOf("Neuf", "Très Bon", "Bon", "Mauvais")
            val stateDescriptions = mapOf(
                "Neuf" to "Objet neuf ou comme neuf, sans aucun défaut visible",
                "Très Bon" to "Objet en très bon état, avec des signes d'usure minimes",
                "Bon" to "Objet en bon état, avec des signes d'usure normaux",
                "Mauvais" to "Objet en mauvais état, avec des défauts visibles ou des dysfonctionnements"
            )
            
            states.forEachIndexed { index, state ->
                val stateColor = stateColors[state] ?: colorBlack
                
                // Badge d'état
                paint.color = stateColor
                val badgeRadius = lineHeight / 4
                val badgeCenterX = margin + cellPadding
                val badgeCenterY = rowY + lineHeight / 2
                canvas.drawCircle(badgeCenterX, badgeCenterY, badgeRadius, paint)
                
                // Texte d'état
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(state, margin + cellPadding * 3, rowY + lineHeight / 2, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = colorBlack
                
                // Description de l'état
                canvas.drawText(": ${stateDescriptions[state]}", margin + cellPadding * 3 + paint.measureText(state) + cellPadding, rowY + lineHeight / 2, paint)
                
                rowY += lineHeight
            }
        }
        
        // Pied de page
        paint.color = colorPrimary
        paint.textSize = smallTextSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page ${document.pages.size}/${document.pages.size + 2}", pageWidth / 2f, pageHeight - margin / 2f, paint)
        
        // Finaliser la page
        document.finishPage(page)
    }
    
    /**
     * Ajoute des pages avec les photos
     */
    private fun addPhotoPages(document: PdfDocument, photos: List<Photo>) {
        val photosPerPage = 4
        val pages = (photos.size + photosPerPage - 1) / photosPerPage // Arrondi supérieur
        
        for (pageIndex in 0 until pages) {
            val pageNumber = document.pages.size + 1
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            // En-tête avec dégradé
            val headerPaint = Paint()
            val headerGradient = LinearGradient(
                0f, 0f, pageWidth.toFloat(), 0f,
                colorPrimary, colorPrimaryDark,
                Shader.TileMode.CLAMP
            )
            headerPaint.shader = headerGradient
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), margin * 1.5f, headerPaint)
            
            // Titre dans l'en-tête
            val paint = Paint()
            paint.shader = null
            paint.color = colorWhite
            paint.textSize = titleTextSize
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            
            val title = if (pages > 1) {
                "Photos (Page ${pageIndex + 1}/${pages})"
            } else {
                "Photos"
            }
            canvas.drawText(title, pageWidth / 2f, margin * 0.9f, paint)
            
            // Réinitialiser l'alignement du texte
            paint.textAlign = Paint.Align.LEFT
            
            // Dessiner les photos
            var yPosition = margin * 2f
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
                    
                    // Dessiner un fond arrondi pour la photo
                    paint.color = colorWhite
                    paint.style = Paint.Style.FILL
                    val photoRect = RectF(
                        x.toFloat(),
                        y,
                        (x + photoWidth).toFloat(),
                        y + photoHeight
                    )
                    canvas.drawRoundRect(photoRect, cornerRadius, cornerRadius, paint)
                    
                    // Dessiner l'image
                    canvas.drawBitmap(scaledBitmap, x.toFloat(), y, null)
                    
                    // Dessiner un cadre autour de l'image
                    paint.color = colorPrimaryLight
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f
                    canvas.drawRoundRect(photoRect, cornerRadius, cornerRadius, paint)
                    
                    // Ajouter une étiquette avec le numéro de la photo
                    paint.color = colorPrimary
                    paint.style = Paint.Style.FILL
                    val labelRect = RectF(
                        x.toFloat(),
                        y - lineHeight,
                        x.toFloat() + photoWidth / 4,
                        y
                    )
                    canvas.drawRoundRect(labelRect, cornerRadius, cornerRadius, paint)
                    
                    // Numéro de la photo
                    paint.color = colorWhite
                    paint.textSize = normalTextSize
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(
                        "Photo ${photoIndex + 1}",
                        x.toFloat() + photoWidth / 8,
                        y - lineHeight / 3,
                        paint
                    )
                    paint.textAlign = Paint.Align.LEFT
                    
                    // Fond pour le commentaire
                    if (photo.comment.isNotEmpty()) {
                        paint.color = colorLightGray
                        paint.alpha = 230
                        val commentRect = RectF(
                            x.toFloat(),
                            y + photoHeight,
                            (x + photoWidth).toFloat(),
                            y + photoHeight + lineHeight * 2
                        )
                        canvas.drawRoundRect(
                            RectF(commentRect.left, commentRect.top - cornerRadius, commentRect.right, commentRect.bottom),
                            cornerRadius, cornerRadius, paint
                        )
                        
                        // Icône de commentaire
                        paint.color = colorPrimary
                        paint.alpha = 255
                        val iconRadius = lineHeight / 3
                        canvas.drawCircle(
                            x.toFloat() + cellPadding,
                            y + photoHeight + lineHeight / 2,
                            iconRadius,
                            paint
                        )
                        
                        // Texte du commentaire
                        paint.color = colorBlack
                        paint.textSize = smallTextSize
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        
                        // Découper le commentaire en plusieurs lignes si nécessaire
                        val maxTextWidth = photoWidth - cellPadding * 3
                        val words = photo.comment.split(" ")
                        var line = ""
                        var lineY = y + photoHeight + lineHeight / 2
                        
                        for (word in words) {
                            val testLine = if (line.isEmpty()) word else "$line $word"
                            if (paint.measureText(testLine) <= maxTextWidth) {
                                line = testLine
                            } else {
                                canvas.drawText(line, x.toFloat() + cellPadding * 3, lineY, paint)
                                lineY += lineHeight / 2
                                line = word
                            }
                        }
                        
                        if (line.isNotEmpty()) {
                            canvas.drawText(line, x.toFloat() + cellPadding * 3, lineY, paint)
                        }
                    }
                } else {
                    // Dessiner un rectangle avec un message d'erreur
                    paint.color = colorLightGray
                    paint.style = Paint.Style.FILL
                    val errorRect = RectF(
                        x.toFloat(),
                        y,
                        (x + photoWidth).toFloat(),
                        y + photoHeight
                    )
                    canvas.drawRoundRect(errorRect, cornerRadius, cornerRadius, paint)
                    
                    // Bordure
                    paint.color = colorMediumGray
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    canvas.drawRoundRect(errorRect, cornerRadius, cornerRadius, paint)
                    
                    // Icône d'erreur
                    paint.color = colorAccent
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(
                        x.toFloat() + photoWidth / 2,
                        y + photoHeight / 2 - lineHeight,
                        lineHeight / 2,
                        paint
                    )
                    
                    // Symbole d'exclamation
                    paint.color = colorWhite
                    paint.strokeWidth = lineHeight / 8
                    canvas.drawLine(
                        x.toFloat() + photoWidth / 2,
                        y + photoHeight / 2 - lineHeight * 1.2f,
                        x.toFloat() + photoWidth / 2,
                        y + photoHeight / 2 - lineHeight * 0.8f,
                        paint
                    )
                    canvas.drawCircle(
                        x.toFloat() + photoWidth / 2,
                        y + photoHeight / 2 - lineHeight * 0.6f,
                        lineHeight / 16,
                        paint
                    )
                    
                    // Message d'erreur
                    paint.color = colorAccent
                    paint.textSize = normalTextSize
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(
                        "Image non disponible",
                        x + (photoWidth - paint.measureText("Image non disponible")) / 2,
                        y + photoHeight / 2,
                        paint
                    )
                    
                    // Sous-titre explicatif
                    paint.textSize = smallTextSize
                    paint.color = colorDarkGray
                    canvas.drawText(
                        "L'image a peut-être été supprimée ou déplacée",
                        x + (photoWidth - paint.measureText("L'image a peut-être été supprimée ou déplacée")) / 2,
                        y + photoHeight / 2 + lineHeight,
                        paint
                    )
                    
                    paint.textAlign = Paint.Align.LEFT
                    paint.color = colorBlack
                }
            }
            
            // Pied de page
            paint.color = colorPrimary
            paint.textSize = smallTextSize
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                "Page ${document.pages.size}/${document.pages.size + (pages - pageIndex)}",
                pageWidth / 2f,
                pageHeight - margin / 2f,
                paint
            )
            
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
