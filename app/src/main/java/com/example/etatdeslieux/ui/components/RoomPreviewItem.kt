package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import com.example.etatdeslieux.ui.components.highlightText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomPreviewItem(
    room: Room,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onAddToGroup: ((Room) -> Unit)? = null,
    availableGroups: List<RoomGroup> = emptyList(),
    isInGroup: Boolean = false,
    isHighlighted: Boolean = false,
    searchQuery: String = ""
) {
    var showMenu by remember { mutableStateOf(false) }
    var showGroupSelectionDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)  
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),  
            horizontalArrangement = Arrangement.spacedBy(8.dp),  
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "État des lieux",
                modifier = Modifier.size(20.dp),  
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Nom de la pièce avec mise en évidence de la recherche
            val titleText = if (searchQuery.isNotBlank() && room.name.contains(searchQuery, ignoreCase = true)) {
                highlightText(room.name, searchQuery)
            } else {
                AnnotatedString(room.name)
            }
            
            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyMedium,  
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            // Type d'état des lieux (entrée/sortie)
            Text(
                text = room.etatType,
                style = MaterialTheme.typography.bodySmall,  
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (!isInGroup && onAddToGroup != null && availableGroups.isNotEmpty()) {
                DropdownMenuItem(
                    text = { Text("Ajouter à un groupe") },
                    onClick = {
                        showMenu = false
                        showGroupSelectionDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter à un groupe"
                        )
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Supprimer") },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer"
                    )
                }
            )
        }
    }

    if (showGroupSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showGroupSelectionDialog = false },
            title = { Text("Ajouter à un groupe") },
            text = {
                Column {
                    availableGroups.forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    onAddToGroup?.invoke(room)
                                    showGroupSelectionDialog = false
                                }
                            ) {
                                Text("Ajouter")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupSelectionDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}