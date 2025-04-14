package com.example.etatdeslieux.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.R
import com.example.etatdeslieux.model.RoomGroup
import com.example.etatdeslieux.ui.components.RoomGroupItem
import com.example.etatdeslieux.ui.components.RoomPreviewItem
import com.example.etatdeslieux.ui.components.SettingsDialog
import com.example.etatdeslieux.ui.theme.AppAnimations
import com.example.etatdeslieux.ui.theme.EtatDesLieuxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoom: (Long) -> Unit,
    onNavigateToAddRoom: () -> Unit,
    onNavigateToPortal: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var showCreateMenu by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var selectedRoomIds by remember { mutableStateOf(emptySet<Long>()) }
    var showMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val governmentColors = MaterialTheme.colorScheme.copy(
        primary = Color(0xFF000091),        // Bleu gouvernement
        onPrimary = Color.White,
        surface = Color.White.copy(alpha = 0.95f),  // Barre légèrement transparente
        secondary = Color(0xFFE1000F),      // Rouge Marianne
        tertiary = Color(0xFF161616)        // Gris foncé
    )

    val fabRotation by AppAnimations.rotateFabAnimation(showCreateMenu)
    val menuOffsetY by animateIntAsState(
        targetValue = if (showCreateMenu) -200 else 0,
        animationSpec = AppAnimations.menuSlideSpec(),
        label = "menu_offset"
    )

    EtatDesLieuxTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "État des Lieux",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToPortal) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Retour au portail",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    actions = {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_adaptive_fore),
                            contentDescription = "Logo de l'application",
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(48.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.tertiary,
                        navigationIconContentColor = MaterialTheme.colorScheme.tertiary,
                        actionIconContentColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bouton Paramètres
                    FloatingActionButton(
                        onClick = { showSettingsDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Icon(Icons.Default.Settings, "Paramètres")
                    }

                    // Bouton principal avec animation
                    FloatingActionButton(
                        onClick = { showCreateMenu = !showCreateMenu },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.rotate(fabRotation)
                    ) {
                        Icon(Icons.Default.Add, "Créer")
                    }
                }

                // Menu contextuel avec animation
                if (showCreateMenu) {
                    Box(
                        modifier = Modifier
                            .offset(y = menuOffsetY.dp)
                            .padding(bottom = 80.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .animateContentSize(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Créer un groupe") },
                                    onClick = {
                                        showCreateMenu = false
                                        showAddGroupDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Nouvel état des lieux") },
                                    onClick = {
                                        showCreateMenu = false
                                        onNavigateToAddRoom()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Afficher les pièces non groupées
                    val ungroupedRooms = uiState.rooms.filter { currentRoom ->
                        uiState.roomGroups.none { existingGroup -> currentRoom.id in existingGroup.roomIds }
                    }

                    if (ungroupedRooms.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pièces non groupées",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(ungroupedRooms) { ungroupedRoom ->
                            RoomPreviewItem(
                                room = ungroupedRoom,
                                onClick = { onNavigateToRoom(ungroupedRoom.id) },
                                onDelete = { viewModel.deleteRoom(ungroupedRoom) },
                                onAddToGroup = { roomToMove ->
                                    val availableGroups = uiState.roomGroups.filter { targetGroup ->
                                        !targetGroup.roomIds.contains(roomToMove.id)
                                    }
                                    if (availableGroups.isNotEmpty()) {
                                        availableGroups.firstOrNull()?.let { targetGroup ->
                                            viewModel.addRoomToGroup(roomToMove.id, targetGroup.id)
                                        }
                                    }
                                },
                                availableGroups = uiState.roomGroups.filter { targetGroup ->
                                    !targetGroup.roomIds.contains(ungroupedRoom.id)
                                }
                            )
                        }
                    }

                    // Afficher les groupes
                    items(uiState.roomGroups) { currentGroup ->
                        RoomGroupItem(
                            group = currentGroup,
                            rooms = uiState.rooms.filter { room -> room.id in currentGroup.roomIds },
                            onGroupClick = { viewModel.toggleGroupExpanded(currentGroup.id) },
                            onGroupDelete = { groupToUpdate, deleteRooms ->
                                if (deleteRooms) {
                                    groupToUpdate.roomIds.forEach { roomId ->
                                        uiState.rooms.find { it.id == roomId }?.let { room ->
                                            viewModel.deleteRoom(room)
                                        }
                                    }
                                }
                                viewModel.deleteGroup(groupToUpdate, deleteRooms)
                            },
                            onRoomClick = { room -> onNavigateToRoom(room.id) },
                            onRoomDelete = { room -> viewModel.deleteRoom(room) }
                        )
                    }

                    item {
                        Button(
                            onClick = { showAddGroupDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("Créer un nouveau groupe")
                        }
                    }
                }

                // Menu latéral
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 8.dp)
                ) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("État des lieux") },
                            onClick = { showMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.Home, contentDescription = "État des lieux")
                            }
                        )
                        // Autres options du menu à ajouter plus tard
                    }
                }
            }

            if (showAddGroupDialog) {
                AlertDialog(
                    onDismissRequest = { showAddGroupDialog = false },
                    title = { Text("Nouveau groupe") },
                    text = {
                        Column {
                            TextField(
                                value = newGroupName,
                                onValueChange = { newGroupName = it },
                                label = { Text("Nom du groupe") }
                            )

                            Text(
                                text = "Sélectionner les pièces",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )

                            uiState.rooms.forEach { room ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedRoomIds.contains(room.id),
                                        onCheckedChange = { checked ->
                                            selectedRoomIds = if (checked) {
                                                selectedRoomIds + room.id
                                            } else {
                                                selectedRoomIds - room.id
                                            }
                                        }
                                    )
                                    Text(
                                        text = room.name,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newGroupName.isNotBlank()) {
                                    viewModel.createRoomGroup(newGroupName, selectedRoomIds)
                                    newGroupName = ""
                                    selectedRoomIds = emptySet()
                                    showAddGroupDialog = false
                                }
                            }
                        ) {
                            Text("Créer")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showAddGroupDialog = false }) {
                            Text("Annuler")
                        }
                    }
                )
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Gestion des erreurs
            uiState.error?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Erreur") },
                    text = { Text(error) },
                    confirmButton = {
                        Button(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}