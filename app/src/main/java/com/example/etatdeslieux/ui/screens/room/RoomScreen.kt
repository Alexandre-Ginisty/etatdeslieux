package com.example.etatdeslieux.ui.screens.room

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
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
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.ui.components.TopBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    roomId: Long,
    onNavigateBack: () -> Unit,
    onTakePhoto: () -> Unit,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val room by viewModel.room.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val error by viewModel.error.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onTakePhoto,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Ajouter une photo") }
            )
        }
    ) { padding ->
        if (photos.isEmpty()) {
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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos) { photo ->
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clickable {
                                selectedPhoto = photo
                                showPhotoDialog = true
                            }
                    ) {
                        Box {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(File(photo.uri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = photo.comment,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (photo.comment.isNotBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = photo.comment,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Photo Dialog
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

        // Delete Confirmation Dialog
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

        // Error Dialog
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
    }
}
