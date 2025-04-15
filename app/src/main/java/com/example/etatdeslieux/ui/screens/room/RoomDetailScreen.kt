package com.example.etatdeslieux.ui.screens.room

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.model.DialogStates
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.ui.components.CameraPermission
import com.example.etatdeslieux.ui.components.PhotoSection
import com.example.etatdeslieux.utils.DateFormatter
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.ArrowBack

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

    // Restaurer l'URI de la photo depuis les préférences partagées au démarrage
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("photo_prefs", Context.MODE_PRIVATE)
        val savedUri = prefs.getString("current_photo_uri_${viewModel.getRoomId()}", null)
        if (savedUri != null) {
            viewModel.restorePhotoUri(Uri.parse(savedUri))
        }
    }

    // Sauvegarder l'URI de la photo dans les préférences partagées
    LaunchedEffect(Unit) {
        viewModel.currentPhotoUri.collect { uri ->
            if (uri != null) {
                val prefs = context.getSharedPreferences("photo_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("current_photo_uri_${viewModel.getRoomId()}", uri.toString()).apply()
            }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chargement...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
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
                    item {
                        RoomInfoSection(room)
                    }
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
                            },
                            onDownloadPhoto = { photo ->
                                scope.launch {
                                    viewModel.downloadPhoto(photo)
                                }
                            },
                            onEditComment = { photo ->
                                viewModel.setPhotoToEdit(photo)
                                dialogStates = dialogStates.copy(showEditCommentDialog = true)
                            }
                        )
                    }
                    
                    // Bouton pour générer le PDF
                    item {
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
                                        scope.launch {
                                            val pdfFile = viewModel.generatePdf()
                                            if (pdfFile != null) {
                                                Toast.makeText(
                                                    context,
                                                    "PDF généré avec succès: ${pdfFile.name}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
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

    if (dialogStates.showPhotoCommentDialog) {
        PhotoCommentDialog(
            onDismiss = { dialogStates = dialogStates.copy(showPhotoCommentDialog = false) },
            onConfirm = { comment ->
                scope.launch {
                    viewModel.addPhotoComment(comment)
                    dialogStates = dialogStates.copy(showPhotoCommentDialog = false)
                }
            }
        )
    }

    if (dialogStates.showEditCommentDialog && photoToEdit != null) {
        EditCommentDialog(
            initialComment = photoToEdit!!.comment,
            onDismiss = { dialogStates = dialogStates.copy(showEditCommentDialog = false) },
            onConfirm = { comment ->
                scope.launch {
                    viewModel.updatePhotoComment(comment)
                    dialogStates = dialogStates.copy(showEditCommentDialog = false)
                }
            }
        )
    }
}

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
    var creator by remember { mutableStateOf(room?.creator ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la salle") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Étage") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
private fun RoomInfoSection(room: Room) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoRow(Icons.Default.Home, "Nom", room.name)
        InfoRow(Icons.Default.Description, "Description", room.description)
        InfoRow(Icons.Default.Stairs, "Étage", room.floor.toString())
        InfoRow(Icons.Default.Person, "Créateur", room.creator)
        InfoRow(Icons.Default.AccessTime, "Créé le", DateFormatter.formatLocalDateTime(room.createdAt))
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
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val showCameraPermission: Boolean = false,
    val showPhotoCommentDialog: Boolean = false,
    val showEditCommentDialog: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (showDeleteDialog) 1 else 0)
        parcel.writeByte(if (showEditDialog) 1 else 0)
        parcel.writeByte(if (showCameraPermission) 1 else 0)
        parcel.writeByte(if (showPhotoCommentDialog) 1 else 0)
        parcel.writeByte(if (showEditCommentDialog) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialogStates> {
        override fun createFromParcel(parcel: Parcel): DialogStates {
            return DialogStates(parcel)
        }

        override fun newArray(size: Int): Array<DialogStates?> {
            return arrayOfNulls(size)
        }
    }
}