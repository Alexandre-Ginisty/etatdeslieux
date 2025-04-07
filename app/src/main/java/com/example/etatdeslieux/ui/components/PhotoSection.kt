package com.example.etatdeslieux.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.etatdeslieux.model.Photo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSection(
    photos: List<Photo>,
    onPhotoAdded: (Uri, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var photoComment by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val photoFile = remember { File(context.cacheDir, "temp_photo.jpg") }
    val photoUri = remember { Uri.fromFile(photoFile) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedUri = photoUri
            showDialog = true
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            showDialog = true
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Photos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos) { photo ->
                PhotoItem(photo = photo)
            }
            
            item {
                Row {
                    // Bouton appareil photo
                    FilledTonalIconButton(
                        onClick = { cameraLauncher.launch(photoUri) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Prendre une photo")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Bouton galerie
                    FilledTonalIconButton(
                        onClick = { galleryLauncher.launch("image/*") }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter depuis la galerie")
                    }
                }
            }
        }
    }

    if (showDialog && selectedUri != null) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                photoComment = ""
                selectedUri = null
            },
            title = { Text("Ajouter une description") },
            text = {
                TextField(
                    value = photoComment,
                    onValueChange = { photoComment = it },
                    label = { Text("Description de la photo") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedUri?.let { uri ->
                            onPhotoAdded(uri, photoComment)
                        }
                        showDialog = false
                        photoComment = ""
                        selectedUri = null
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        photoComment = ""
                        selectedUri = null
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun PhotoItem(
    photo: Photo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(120.dp)
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = photo.comment,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
