package com.example.etatdeslieux.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import com.example.etatdeslieux.utils.DateFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomGroupItem(
    group: RoomGroup,
    rooms: List<Room>,
    onRoomClick: (Room) -> Unit,
    onGroupClick: () -> Unit,
    onRoomDelete: (Room) -> Unit,
    onGroupDelete: (RoomGroup, Boolean) -> Unit,
    onRemoveRoomFromGroup: (Long, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(group.isExpanded) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Arrow rotation"
    )
    
    // État pour le menu contextuel
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Carte du groupe avec bordures rectangulaires
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onGroupClick,
                onLongClick = { showDeleteDialog = true }
            ),
        shape = RectangleShape, // Bordures rectangulaires pour les groupes
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // En-tête du groupe
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Date de création",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Créé le ${DateFormatter.formatLocalDateTime(group.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${rooms.size} état${if (rooms.size > 1) "s" else ""} des lieux",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Réduire" else "Développer",
                        modifier = Modifier.rotate(rotationState),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Liste des pièces
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    rooms.forEach { room ->
                        // État pour le menu contextuel de la pièce
                        var showRoomMenu by remember { mutableStateOf(false) }
                        
                        // Carte d'état des lieux avec bordures arrondies
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .combinedClickable(
                                    onClick = { onRoomClick(room) },
                                    onLongClick = { showRoomMenu = true }
                                ),
                            shape = RoundedCornerShape(12.dp), // Bordures arrondies pour les états des lieux
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 1.dp
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface) // Bordure noire
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = room.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "État des lieux ${room.etatType} n°${room.etatNumber}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Créé le ${DateFormatter.formatLocalDateTime(room.createdAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Voir détails",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Menu contextuel pour la pièce
                        if (showRoomMenu) {
                            AlertDialog(
                                onDismissRequest = { showRoomMenu = false },
                                title = { Text("Options pour \"${room.name}\"") },
                                text = { Text("Que souhaitez-vous faire avec cet état des lieux ?") },
                                confirmButton = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onRoomDelete(room)
                                                showRoomMenu = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Supprimer l'état des lieux")
                                        }
                                        
                                        Button(
                                            onClick = {
                                                onRemoveRoomFromGroup(group.id, room.id)
                                                showRoomMenu = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Retirer du groupe")
                                        }
                                        
                                        OutlinedButton(
                                            onClick = { showRoomMenu = false },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Annuler")
                                        }
                                    }
                                },
                                dismissButton = null
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialogue de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le groupe") },
            text = { 
                Text("Voulez-vous supprimer le groupe \"${group.name}\" ? " +
                     "Vous pouvez également supprimer toutes les pièces qu'il contient.")
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onGroupDelete(group, true)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Supprimer tout")
                    }
                    
                    Button(
                        onClick = {
                            onGroupDelete(group, false)
                            showDeleteDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Supprimer le groupe uniquement")
                    }
                    
                    OutlinedButton(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler")
                    }
                }
            },
            dismissButton = null
        )
    }
}