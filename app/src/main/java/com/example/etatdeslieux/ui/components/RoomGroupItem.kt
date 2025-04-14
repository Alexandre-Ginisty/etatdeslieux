package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomGroupItem(
    group: RoomGroup,
    rooms: List<Room>,
    onRoomClick: (Room) -> Unit,
    onGroupClick: () -> Unit,
    onRoomDelete: (Room) -> Unit,
    onGroupDelete: (RoomGroup, Boolean) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onGroupClick,
                    onLongClick = { showMenu = true }
                ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Groupe",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${rooms.size} pièce${if (rooms.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Icon(
                    imageVector = if (group.isExpanded) 
                        Icons.Default.KeyboardArrowDown 
                    else 
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (group.isExpanded) "Réduire" else "Développer",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (group.isExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    rooms.forEach { room ->
                        RoomPreviewItem(
                            room = room,
                            onClick = { onRoomClick(room) },
                            onDelete = { onRoomDelete(room) },
                            onAddToGroup = { roomToMove ->
                                // Supprimer la pièce du groupe actuel
                                onGroupDelete(group.copy(roomIds = group.roomIds - roomToMove.id), false)
                            },
                            isInGroup = true
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Supprimer") },
                onClick = { 
                    showDeleteDialog = true
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le groupe") },
            text = { Text("Que souhaitez-vous supprimer ?") },
            confirmButton = {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            onGroupDelete(group, false)
                            showDeleteDialog = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Supprimer uniquement le groupe")
                    }
                    Button(
                        onClick = { 
                            onGroupDelete(group, true)
                            showDeleteDialog = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Supprimer le groupe et son contenu")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}