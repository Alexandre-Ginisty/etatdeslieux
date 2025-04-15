package com.example.etatdeslieux.ui.screens.room

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.etatdeslieux.model.DialogStates
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.ui.components.*
import com.example.etatdeslieux.utils.ComposeFileProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun RoomScreen(
    onNavigateBack: () -> Unit,
    onTakePhoto: () -> Unit,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val room by viewModel.room.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val error by viewModel.error.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var dialogStates by remember { mutableStateOf(DialogStates()) }
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.handlePhotoCapture()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            showErrorDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = room?.name ?: "Chargement...",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            BottomBar(
                onSettingsClick = { showSettingsDialog = true },
                onFilterClick = { showFilterDialog = true },
                onAddClick = {
                    expanded = !expanded
                    showAddDialog = true
                },
                isAddExpanded = expanded
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Aucune photo",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Appuyez sur le bouton + pour ajouter une photo",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                PhotoSection(
                    photos = photos,
                    onAddPhotoClick = { 
                        dialogStates = dialogStates.copy(showCameraPermission = true) 
                    },
                    onDeletePhoto = { photo -> 
                        viewModel.deletePhoto(photo) 
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showPhotoDialog && selectedPhoto != null) {
        var tempComment by remember { mutableStateOf(selectedPhoto!!.comment) }
        
        AlertDialog(
            onDismissRequest = {
                showPhotoDialog = false
                selectedPhoto = null
            },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            title = { Text("Photo") },
            text = {
                Column {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(selectedPhoto!!.uri))
                            .crossfade(true)
                            .build(),
                        contentDescription = selectedPhoto!!.comment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Fit
                    )
                    OutlinedTextField(
                        value = tempComment,
                        onValueChange = { tempComment = it },
                        label = { Text("Commentaire") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            selectedPhoto?.let { photo ->
                                viewModel.updatePhoto(photo.copy(comment = tempComment))
                            }
                            showPhotoDialog = false
                            selectedPhoto = null
                        }
                    ) {
                        Text("Enregistrer")
                    }
                    TextButton(
                        onClick = {
                            showPhotoDialog = false
                            showDeleteConfirmation = true
                        }
                    ) {
                        Text("Supprimer")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoDialog = false
                        selectedPhoto = null
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showDeleteConfirmation && selectedPhoto != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Supprimer la photo") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette photo ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedPhoto?.let { photo ->
                            viewModel.deletePhoto(photo)
                        }
                        showDeleteConfirmation = false
                        selectedPhoto = null
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showErrorDialog && error != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text("Erreur") },
            text = { Text(error!!) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filtrer") },
            text = {
                // TODO: Ajouter les options de filtrage
                Text("Options de filtrage à venir")
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    if (dialogStates.showCameraPermission) {
        CameraPermission(
            onPermissionGranted = {
                ComposeFileProvider.getImageUri(context).let { uri ->
                    cameraLauncher.launch(uri)
                }
                dialogStates = dialogStates.copy(showCameraPermission = false)
            },
            onDismiss = {
                dialogStates = dialogStates.copy(showCameraPermission = false)
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    if (showAddDialog) {
        AddDialog(
            onDismiss = {
                showAddDialog = false
                expanded = false
            }
        )
    }
}

