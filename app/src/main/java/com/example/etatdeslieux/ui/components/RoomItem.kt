package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.etatdeslieux.model.Room

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomItem(
    room: Room,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isHighlighted: Boolean = false,
    searchQuery: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .let {
                    if (isHighlighted) {
                        it.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    } else {
                        it
                    }
                }
        ) {
            // Titre avec mise en évidence de la recherche
            val titleText = if (searchQuery.isNotBlank() && room.name.contains(searchQuery, ignoreCase = true)) {
                highlightText(room.name, searchQuery)
            } else {
                AnnotatedString(room.name)
            }
            
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Description avec mise en évidence de la recherche
            val descriptionText = if (searchQuery.isNotBlank() && room.description.contains(searchQuery, ignoreCase = true)) {
                highlightText(room.description, searchQuery)
            } else {
                AnnotatedString(room.description)
            }
            
            Text(
                text = descriptionText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Étage: ${room.floor}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Type: ${room.etatType}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Par: ${room.creator}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Fonction utilitaire pour mettre en évidence le texte de recherche
 */
fun highlightText(text: String, query: String): AnnotatedString {
    return buildAnnotatedString {
        val lowercaseText = text.lowercase()
        val lowercaseQuery = query.lowercase()
        
        var startIndex = 0
        while (startIndex < text.length) {
            val matchIndex = lowercaseText.indexOf(lowercaseQuery, startIndex)
            if (matchIndex < 0) {
                // Aucune correspondance trouvée, ajouter le reste du texte
                append(text.substring(startIndex))
                break
            }
            
            // Ajouter le texte avant la correspondance
            append(text.substring(startIndex, matchIndex))
            
            // Ajouter la correspondance avec mise en évidence
            val endIndex = matchIndex + query.length
            withStyle(
                SpanStyle(
                    background = Color.Yellow.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(matchIndex, endIndex))
            }
            
            startIndex = endIndex
        }
    }
}
