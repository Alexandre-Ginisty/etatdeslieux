package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onGroupClick,
                    onLongClick = { showMenu = true }
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (group.isExpanded) 
                            Icons.Default.KeyboardArrowDown 
                        else 
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = if (group.isExpanded) "Réduire" else "Développer",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                    )
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleLarge
                    )
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
        
        if (group.isExpanded) {
            rooms.forEach { room ->
                RoomPreviewItem(
                    room = room,
                    onClick = { onRoomClick(room) },  // Utiliser onRoomClick au lieu de onNavigateToRoom
                    onDelete = { onRoomDelete(room) }
                )
            }
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