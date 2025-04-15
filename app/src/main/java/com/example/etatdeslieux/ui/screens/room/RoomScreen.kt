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
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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

    // Observer pour la photo en attente de commentaire
    val pendingPhotoUri by viewModel.pendingPhotoUri.collectAsState()
    
    // Observer pour la photo dont le commentaire est en cours d'édition
    val photoToEdit by viewModel.photoToEdit.collectAsState()
    
    // Afficher la boîte de dialogue de commentaire si une photo est en attente
    LaunchedEffect(pendingPhotoUri) {
        if (pendingPhotoUri != null) {
            dialogStates = dialogStates.copy(showPhotoCommentDialog = true)
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PhotoSection(
                        photos = photos,
                        onAddPhotoClick = { 
                            dialogStates = dialogStates.copy(showCameraPermission = true) 
                        },
                        onDeletePhoto = { photo -> 
                            viewModel.deletePhoto(photo) 
                        },
                        onDownloadPhoto = { photo ->
                            viewModel.downloadPhoto(photo)
                        },
                        onEditComment = { photo ->
                            viewModel.setPhotoToEdit(photo)
                            dialogStates = dialogStates.copy(showEditCommentDialog = true)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Bouton pour générer le PDF
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Générer un PDF de l'état des lieux",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Button(
                                onClick = {
                                    val pdfFile = viewModel.generatePdf()
                                    if (pdfFile != null) {
                                        Toast.makeText(
                                            context,
                                            "PDF généré avec succès: ${pdfFile.name}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Générer PDF",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Générer PDF")
                            }
                        }
                    }
                }
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
                    viewModel.setPhotoUri(uri)
                    cameraLauncher.launch(uri)
                }
                dialogStates = dialogStates.copy(showCameraPermission = false)
            },
            onDismiss = {
                dialogStates = dialogStates.copy(showCameraPermission = false)
            }
        )
    }
    
    if (dialogStates.showPhotoCommentDialog) {
        PhotoCommentDialog(
            onDismiss = { dialogStates = dialogStates.copy(showPhotoCommentDialog = false) },
            onConfirm = { comment ->
                viewModel.addPhotoComment(comment)
                dialogStates = dialogStates.copy(showPhotoCommentDialog = false)
            }
        )
    }

    if (dialogStates.showEditCommentDialog && photoToEdit != null) {
        EditCommentDialog(
            initialComment = photoToEdit!!.comment,
            onDismiss = { dialogStates = dialogStates.copy(showEditCommentDialog = false) },
            onConfirm = { comment ->
                viewModel.updatePhotoComment(comment)
                dialogStates = dialogStates.copy(showEditCommentDialog = false)
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

@Composable
private fun PhotoCommentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un commentaire à la photo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(comment)
            }) {
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
private fun EditCommentDialog(
    initialComment: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var comment by remember { mutableStateOf(initialComment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Éditer le commentaire de la photo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(comment)
            }) {
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
