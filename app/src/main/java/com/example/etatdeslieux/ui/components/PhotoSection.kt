package com.example.etatdeslieux.ui.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.etatdeslieux.model.Photo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoSection(
    photos: List<Photo>,
    onAddPhotoClick: () -> Unit,
    onDeletePhoto: (Photo) -> Unit,
    onDownloadPhoto: (Photo) -> Unit,
    onEditComment: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 400.dp), // Ajout d'une hauteur maximale
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Photos",
                    style = MaterialTheme.typography.titleLarge
                )
                FilledTonalButton(
                    onClick = onAddPhotoClick,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une photo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter")
                }
            }

            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune photo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp) // Ajout d'une hauteur maximale
                ) {
                    items(photos) { photo ->
                        PhotoItem(
                            photo = photo,
                            onClick = {
                                selectedPhoto = photo
                                showPhotoDialog = true
                            },
                            onLongClick = {
                                selectedPhoto = photo
                                showContextMenu = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && selectedPhoto != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedPhoto = null
            },
            title = { Text("Supprimer la photo") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette photo ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedPhoto?.let { onDeletePhoto(it) }
                        showDeleteDialog = false
                        selectedPhoto = null
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedPhoto = null
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showPhotoDialog && selectedPhoto != null) {
        Dialog(
            onDismissRequest = {
                showPhotoDialog = false
                selectedPhoto = null
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(selectedPhoto?.uri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Photo en plein écran",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                        IconButton(
                            onClick = {
                                selectedPhoto?.let { onDeletePhoto(it) }
                                showPhotoDialog = false
                                selectedPhoto = null
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer la photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!selectedPhoto?.comment.isNullOrBlank()) {
                        Text(
                            text = selectedPhoto?.comment ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showContextMenu && selectedPhoto != null) {
        val position = remember { mutableStateOf(IntOffset.Zero) }
        val density = LocalDensity.current
        val context = LocalContext.current
        
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { 
                showContextMenu = false
                selectedPhoto = null
            },
            modifier = Modifier.wrapContentSize()
        ) {
            DropdownMenuItem(
                text = { Text("Voir la photo") },
                leadingIcon = { Icon(Icons.Default.Image, contentDescription = "Voir") },
                onClick = {
                    showContextMenu = false
                    showPhotoDialog = true
                }
            )
            
            DropdownMenuItem(
                text = { Text("Modifier le commentaire") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Modifier") },
                onClick = {
                    selectedPhoto?.let { onEditComment(it) }
                    showContextMenu = false
                    selectedPhoto = null
                }
            )
            
            DropdownMenuItem(
                text = { Text("Télécharger") },
                leadingIcon = { Icon(Icons.Default.Download, contentDescription = "Télécharger") },
                onClick = {
                    selectedPhoto?.let { onDownloadPhoto(it) }
                    android.widget.Toast.makeText(context, "Photo téléchargée dans Downloads", android.widget.Toast.LENGTH_SHORT).show()
                    showContextMenu = false
                    selectedPhoto = null
                }
            )
            
            DropdownMenuItem(
                text = { Text("Supprimer") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Supprimer") },
                onClick = {
                    showContextMenu = false
                    showDeleteDialog = true
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoItem(
    photo: Photo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Uri.parse(photo.uri))
                .crossfade(true)
                .build(),
            contentDescription = "Photo de la salle",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
