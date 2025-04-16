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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.etatdeslieux.model.Photo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoSectionSimple(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit,
    onDeletePhoto: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
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
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            items(photos) { photo ->
                PhotoItemSimple(
                    photo = photo,
                    onClick = { onPhotoClick(photo) },
                    onDelete = { onDeletePhoto(photo) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoItemSimple(
    photo: Photo,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick
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
        
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.small
                )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer la photo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
