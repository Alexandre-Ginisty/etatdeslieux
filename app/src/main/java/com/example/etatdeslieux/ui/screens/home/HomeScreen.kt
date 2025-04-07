package com.example.etatdeslieux.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.RoomGroup
import com.example.etatdeslieux.ui.components.RoomGroupItem
import com.example.etatdeslieux.ui.components.RoomPreviewItem
import com.example.etatdeslieux.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddRoom: () -> Unit,
    onNavigateToRoom: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState()
    val selectedRoom by viewModel.selectedRoom.collectAsState()
    val error by viewModel.error.collectAsState()
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showRoomOptionsDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        if (error != null) {
            showErrorDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "État des Lieux",
                showBackButton = false
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddRoom,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ajouter une pièce") }
            )
        }
    ) { padding ->
        if (rooms.isEmpty()) {
            EmptyHomeContent(padding)
        } else {
            HomeContent(
                padding = padding,
                rooms = rooms,
                groups = groups,
                onShowCreateGroup = { showCreateGroupDialog = true },
                onNavigateToRoom = onNavigateToRoom,
                onNavigateToAddRoom = onNavigateToAddRoom,
                onRoomSelected = { room ->
                    viewModel.selectRoom(room)
                    showRoomOptionsDialog = true
                },
                viewModel = viewModel
            )
        }

        // Dialogs
        if (showCreateGroupDialog) {
            CreateGroupDialog(
                rooms = rooms,
                onDismiss = { showCreateGroupDialog = false },
                onCreateGroup = { name, selectedRooms ->
                    viewModel.createGroup(name, selectedRooms)
                    showCreateGroupDialog = false
                }
            )
        }

        if (showRoomOptionsDialog && selectedRoom != null) {
            RoomOptionsDialog(
                room = selectedRoom!!,
                onDismiss = {
                    showRoomOptionsDialog = false
                    viewModel.clearSelectedRoom()
                },
                onDelete = { room ->
                    viewModel.deleteRoom(room)
                    showRoomOptionsDialog = false
                    viewModel.clearSelectedRoom()
                }
            )
        }

        if (showErrorDialog && error != null) {
            ErrorDialog(
                error = error!!,
                onDismiss = { showErrorDialog = false }
            )
        }
    }
}

@Composable
private fun EmptyHomeContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Aucune pièce",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Appuyez sur le bouton + pour ajouter une pièce",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    rooms: List<Room>,
    groups: List<RoomGroup>,
    onShowCreateGroup: () -> Unit,
    onNavigateToRoom: (Long) -> Unit,
    onNavigateToAddRoom: () -> Unit,  // Ajouter cette ligne
    onRoomSelected: (Room) -> Unit,
    viewModel: HomeViewModel
) {
    var showAddToGroupDialog by remember { mutableStateOf<RoomGroup?>(null) }
    var showSelectExistingRoomDialog by remember { mutableStateOf<RoomGroup?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        item {
            GroupHeader(onShowCreateGroup)
        }

        items(
            items = groups,
            key = { it.id }
        ) { group ->
            RoomGroupItem(
                group = group,
                rooms = rooms,
                onGroupClick = { viewModel.toggleGroupExpansion(group.id) },
                onRoomClick = onNavigateToRoom,
                onRoomLongClick = { roomId ->
                    val room = rooms.find { it.id == roomId }
                    if (room != null) {
                        onRoomSelected(room)
                    }
                },
                onAddClick = { showAddToGroupDialog = group }
            )
        }

        item {
            Text(
                text = "Pièces non groupées",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val ungroupedRooms = rooms.filter { room ->
            groups.none { group -> room.id in group.roomIds }
        }
        items(
            items = ungroupedRooms,
            key = { it.id }
        ) { room ->
            RoomPreviewItem(
                room = room,
                onRoomClick = { onNavigateToRoom(room.id) }
            )
        }
    }

    // Dialog pour ajouter à un groupe
    showAddToGroupDialog?.let { group ->
        AlertDialog(
            onDismissRequest = { showAddToGroupDialog = null },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            title = { Text("Ajouter au groupe ${group.name}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onNavigateToAddRoom()
                            viewModel.setTargetGroupForNewRoom(group.id)
                            showAddToGroupDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Créer un nouvel état des lieux")
                    }

                    OutlinedButton(
                        onClick = { 
                            showAddToGroupDialog = null
                            showSelectExistingRoomDialog = group
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajouter un état des lieux existant")
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showAddToGroupDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog pour sélectionner une pièce existante
    showSelectExistingRoomDialog?.let { group ->
        val availableRooms = rooms.filter { it.id !in group.roomIds }
        
        AlertDialog(
            onDismissRequest = { showSelectExistingRoomDialog = null },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
            title = { Text("Sélectionner un état des lieux") },
            text = {
                if (availableRooms.isEmpty()) {
                    Text("Aucun état des lieux disponible à ajouter")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        availableRooms.forEach { room ->
                            ListItem(
                                headlineContent = { Text(room.name) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addExistingRoomToGroup(group.id, room.id)
                                        showSelectExistingRoomDialog = null
                                    },
                                leadingContent = {
                                    Icon(Icons.Default.Home, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showSelectExistingRoomDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun GroupHeader(onShowCreateGroup: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Groupes",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onShowCreateGroup) {
            Icon(Icons.Default.Add, contentDescription = "Créer un groupe")
        }
    }
}

@Composable
private fun CreateGroupDialog(
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onCreateGroup: (String, List<Long>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedRoomIds by remember { mutableStateOf(emptySet<Long>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        title = { Text("Créer un groupe") },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nom du groupe") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Sélectionner les pièces",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                rooms.forEach { room ->
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
                        Text(room.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onCreateGroup(groupName, selectedRoomIds.toList())
                    }
                },
                enabled = groupName.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun RoomOptionsDialog(
    room: Room,
    onDismiss: () -> Unit,
    onDelete: (Room) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (!showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            title = { Text(room.name) },
            text = {
                Column {
                    Text("Que souhaitez-vous faire avec cette pièce ?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Supprimer ${room.name} ?") },
            text = {
                Text("Cette action est irréversible. Toutes les photos associées seront également supprimées.")
            },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(room) }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun ErrorDialog(
    error: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text("Erreur") },
        text = { Text(error) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
