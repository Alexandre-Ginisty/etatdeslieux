package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .padding(vertical = 2.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .let {
                    if (isHighlighted) {
                        it.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    } else {
                        it
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Titre avec mise en évidence de la recherche
            val titleText = if (searchQuery.isNotBlank() && room.name.contains(searchQuery, ignoreCase = true)) {
                highlightText(room.name, searchQuery)
            } else {
                AnnotatedString(room.name)
            }
            
            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Type d'état des lieux (entrée/sortie)
            Text(
                text = room.etatType,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
