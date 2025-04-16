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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.model.DialogStates
import com.example.etatdeslieux.model.Photo
import com.example.etatdeslieux.model.Room
import com.example.etatdeslieux.model.Item
import com.example.etatdeslieux.ui.components.CameraPermission
import com.example.etatdeslieux.ui.components.PhotoSectionSimple
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
    
    // Observer pour les objets
    val items = uiState.items
    
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
                title = { Text(uiState.room?.name ?: "Détails de la pièce") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    if (uiState.room != null) {
                        IconButton(onClick = {
                            dialogStates = dialogStates.copy(showEditDialog = true)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier"
                            )
                        }
                        IconButton(onClick = {
                            dialogStates = dialogStates.copy(showDeleteDialog = true)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer"
                            )
                        }
                        IconButton(onClick = {
                            scope.launch {
                                val pdfFile = viewModel.generatePdf()
                                if (pdfFile != null) {
                                    Toast.makeText(
                                        context,
                                        "PDF généré: ${pdfFile.name}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Erreur lors de la génération du PDF",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Générer PDF"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogStates = dialogStates.copy(showCameraPermission = true)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Prendre une photo"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.room == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Pièce non trouvée")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Informations",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            RoomInfoSection(uiState.room!!)
                        }
                    }
                }
                
                // Section des objets
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Objets",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = {
                                    dialogStates = dialogStates.copy(showAddItemDialog = true)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Ajouter un objet"
                                    )
                                }
                            }
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (items.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aucun objet \uD83D\uDE9E\nCliquez sur + pour ajouter",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                ItemsSection(
                                    items = items,
                                    onEditItem = { item ->
                                        viewModel.setItemToEdit(item)
                                        dialogStates = dialogStates.copy(showEditItemDialog = true)
                                    },
                                    onDeleteItem = { item ->
                                        viewModel.setItemToDelete(item)
                                        dialogStates = dialogStates.copy(showDeleteItemDialog = true)
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Section des photos
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Photos",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (uiState.photos.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aucune photo \uD83D\uDCF7\nUtilisez le bouton en bas à droite pour prendre une photo",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                PhotoSectionSimple(
                                    photos = uiState.photos,
                                    onPhotoClick = { photo ->
                                        viewModel.setPhotoToEdit(photo)
                                        dialogStates = dialogStates.copy(showEditCommentDialog = true)
                                    },
                                    onDeletePhoto = { photo ->
                                        viewModel.deletePhoto(photo)
                                    }
                                )
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
    
    // Dialogues pour les objets
    if (dialogStates.showAddItemDialog) {
        AddItemDialog(
            onDismiss = { dialogStates = dialogStates.copy(showAddItemDialog = false) },
            onConfirm = { name, quantity, condition, comment ->
                dialogStates = dialogStates.copy(showAddItemDialog = false)
                viewModel.addItem(name, quantity, condition, comment)
            }
        )
    }
    
    val itemToEdit by viewModel.itemToEdit.collectAsState()
    if (dialogStates.showEditItemDialog && itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            onDismiss = { 
                dialogStates = dialogStates.copy(showEditItemDialog = false)
                viewModel.clearItemToEdit()
            },
            onConfirm = { updatedItem ->
                dialogStates = dialogStates.copy(showEditItemDialog = false)
                viewModel.updateItem(updatedItem)
            }
        )
    }
    
    val itemToDelete by viewModel.itemToDelete.collectAsState()
    if (dialogStates.showDeleteItemDialog && itemToDelete != null) {
        DeleteItemDialog(
            item = itemToDelete!!,
            onDismiss = { 
                dialogStates = dialogStates.copy(showDeleteItemDialog = false)
                viewModel.clearItemToDelete()
            },
            onConfirm = {
                dialogStates = dialogStates.copy(showDeleteItemDialog = false)
                viewModel.deleteItem(itemToDelete!!)
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

@Composable
private fun ItemsSection(
    items: List<Item>,
    onEditItem: (Item) -> Unit,
    onDeleteItem: (Item) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ItemRow(
                item = item,
                onEditItem = onEditItem,
                onDeleteItem = onDeleteItem
            )
        }
    }
}

@Composable
private fun ItemRow(
    item: Item,
    onEditItem: (Item) -> Unit,
    onDeleteItem: (Item) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantité: ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Badge pour l'état
                    val (backgroundColor, textColor) = when(item.condition) {
                        "Mauvais" -> Pair(Color(0xFFFFCDD2), Color(0xFFB71C1C))
                        "Bon" -> Pair(Color(0xFFE1F5FE), Color(0xFF0277BD))
                        "Très Bon" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        "Neuf" -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
                        else -> Pair(Color(0xFFEEEEEE), Color(0xFF424242))
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = backgroundColor,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = item.condition,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                if (item.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Row {
                IconButton(onClick = { onEditItem(item) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Éditer l'objet",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDeleteItem(item) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer l'objet",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onConfirm: (Item) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var condition by remember { mutableStateOf(item.condition) }
    var comment by remember { mutableStateOf(item.comment) }
    var expanded by remember { mutableStateOf(false) }
    
    val conditions = listOf("Mauvais", "Bon", "Très Bon", "Neuf")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Éditer l'objet") },
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        // Accepter uniquement les chiffres
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            quantity = it
                        }
                    },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Menu déroulant pour l'état
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("État") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        conditions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    condition = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && quantity.isNotBlank()) {
                        onConfirm(
                            item.copy(
                                name = name,
                                quantity = quantity.toIntOrNull() ?: 1,
                                condition = condition,
                                comment = comment
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && quantity.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var condition by remember { mutableStateOf("Bon") }
    var comment by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val conditions = listOf("Mauvais", "Bon", "Très Bon", "Neuf")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un objet") },
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        // Accepter uniquement les chiffres
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            quantity = it
                        }
                    },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Menu déroulant pour l'état
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("État") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        conditions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    condition = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && quantity.isNotBlank()) {
                        onConfirm(
                            name,
                            quantity.toIntOrNull() ?: 1,
                            condition,
                            comment
                        )
                    }
                },
                enabled = name.isNotBlank() && quantity.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer l'objet") },
        text = { Text("Êtes-vous sûr de vouloir supprimer l'objet '${item.name}' ?") },
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