package com.example.etatdeslieux.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.model.RoomGroup
import com.example.etatdeslieux.ui.components.RoomGroupItem
import com.example.etatdeslieux.ui.components.RoomPreviewItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoom: (Long) -> Unit,
    onNavigateToAddRoom: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var selectedRoomIds by remember { mutableStateOf(emptySet<Long>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("État des Lieux") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddRoom) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une pièce")
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
                val ungroupedRooms = uiState.rooms.filter { room ->
                    uiState.roomGroups.none { group -> room.id in group.roomIds }
                }

                if (ungroupedRooms.isNotEmpty()) {
                    item {
                        Text(
                            text = "Pièces non groupées",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(ungroupedRooms) { room ->
                        RoomPreviewItem(
                            room = room,
                            onClick = { onNavigateToRoom(room.id) },
                            onDelete = { viewModel.deleteRoom(room) }
                        )
                    }
                }

                // Afficher les groupes
                items(uiState.roomGroups) { group ->
                    RoomGroupItem(
                        group = group,
                        rooms = uiState.rooms.filter { it.id in group.roomIds },
                        onRoomClick = { room -> onNavigateToRoom(room.id) }, 
                        onGroupClick = { viewModel.toggleGroupExpanded(group.id) },
                        onRoomDelete = { room -> viewModel.deleteRoom(room) },
                        onGroupDelete = { group, deleteRooms -> viewModel.deleteGroup(group, deleteRooms) }                    )
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