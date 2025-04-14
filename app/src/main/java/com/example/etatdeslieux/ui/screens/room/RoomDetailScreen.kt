package com.example.etatdeslieux.ui.screens.room

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.ui.components.CameraPermission
import com.example.etatdeslieux.ui.components.PhotoSection
import com.example.etatdeslieux.ui.screens.room.RoomViewModel
import com.example.etatdeslieux.utils.ComposeFileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    viewModel: RoomViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dialogStates by remember { mutableStateOf(DialogStates()) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            scope.launch {
                viewModel.handlePhotoCapture()
            }
        }
    }


    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.room?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { dialogStates = dialogStates.copy(showEditDialog = true) }) {
                        Icon(Icons.Default.Edit, "Modifier")
                    }
                    IconButton(onClick = { dialogStates = dialogStates.copy(showDeleteDialog = true) }) {
                        Icon(Icons.Default.Delete, "Supprimer")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.room?.let { room ->
                    item { RoomInfoSection(room) }
                    item {
                        PhotoSection(
                            photos = uiState.photos,
                            onAddPhotoClick = {
                                dialogStates = dialogStates.copy(showCameraPermission = true)
                            },
                            onDeletePhoto = { photo ->
                                scope.launch {
                                    viewModel.deletePhoto(photo)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (dialogStates.showDeleteDialog) {
        DeleteDialog(
            onDismiss = { dialogStates = dialogStates.copy(showDeleteDialog = false) },
            onConfirm = {
                scope.launch {
                    viewModel.deleteRoom()
                    onNavigateBack()
                }
            }
        )
    }

    if (dialogStates.showEditDialog && uiState.room != null) {
        EditDialog(
            room = uiState.room,
            onDismiss = { dialogStates = dialogStates.copy(showEditDialog = false) },
            onConfirm = { updatedRoom ->
                scope.launch {
                    viewModel.updateRoom(updatedRoom)
                    dialogStates = dialogStates.copy(showEditDialog = false)
                }
            }
        )
    }

    if (dialogStates.showCameraPermission) {
        CameraPermission(
            onPermissionGranted = {
                viewModel.preparePhotoCapture()?.let { uri ->
                    cameraLauncher.launch(uri)
                }
                dialogStates = dialogStates.copy(showCameraPermission = false)
            },
            onDismiss = { dialogStates = dialogStates.copy(showCameraPermission = false) }
        )
    }
}

// Keep the rest of the code (DeleteDialog, EditDialog, RoomInfoSection, InfoRow, and DialogStates) unchanged



@Composable
private fun DeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer la salle") },
        text = { Text("Êtes-vous sûr de vouloir supprimer cette salle ?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Supprimer")
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
private fun EditDialog(
    room: Room?,
    onDismiss: () -> Unit,
    onConfirm: (Room) -> Unit
) {
    var name by remember { mutableStateOf(room?.name ?: "") }
    var description by remember { mutableStateOf(room?.description ?: "") }
    var floor by remember { mutableStateOf(room?.floor?.toString() ?: "") }
    var size by remember { mutableStateOf(room?.size?.toString() ?: "") }
    var creator by remember { mutableStateOf(room?.creator ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la salle") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Étage") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Taille (m²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = creator,
                    onValueChange = { creator = it },
                    label = { Text("Créateur") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    room?.let {
                        onConfirm(
                            it.copy(
                                name = name,
                                description = description,
                                floor = floor.toIntOrNull() ?: 0,
                                size = size.toFloatOrNull() ?: 0f,
                                creator = creator
                            )
                        )
                    }
                }
            ) {
                Text("Enregistrer")
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
private fun RoomInfoSection(room: Room) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoRow(Icons.Default.Home, "Nom", room.name)
        InfoRow(Icons.Default.Description, "Description", room.description)
        InfoRow(Icons.Default.Stairs, "Étage", room.floor.toString())
        InfoRow(Icons.Default.SquareFoot, "Taille", "${room.size} m²")
        InfoRow(Icons.Default.Person, "Créateur", room.creator)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private data class DialogStates(
    val showDeleteDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showCameraPermission: Boolean = false
)