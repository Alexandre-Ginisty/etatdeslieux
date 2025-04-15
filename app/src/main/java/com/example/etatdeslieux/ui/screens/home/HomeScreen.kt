package com.example.etatdeslieux.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.R
import com.example.etatdeslieux.ui.components.RoomGroupItem
import com.example.etatdeslieux.ui.components.RoomItem
import com.example.etatdeslieux.ui.components.RoomPreviewItem
import com.example.etatdeslieux.ui.components.SettingsDialog

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoom: (Long) -> Unit,
    onNavigateToAddRoom: () -> Unit,
    onNavigateToPortal: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var selectedRoomIds by remember { mutableStateOf(emptySet<Long>()) }
    var newGroupName by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    // Utiliser les valeurs du ViewModel pour la recherche
    val searchQuery = uiState.searchQuery
    val searchResults = uiState.searchResults
    val currentSearchIndex = uiState.currentSearchIndex
    val totalSearchResults = uiState.totalSearchResults

    // Effet pour faire défiler jusqu'au résultat actuel
    val currentSearchRoom = remember(currentSearchIndex) { 
        if (currentSearchIndex >= 0 && searchResults.isNotEmpty()) 
            searchResults.getOrNull(currentSearchIndex) 
        else null 
    }
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(currentSearchRoom) {
        currentSearchRoom?.let { room ->
            // Trouver l'index de l'élément dans la liste
            val roomIndex = uiState.rooms.indexOf(room)
            if (roomIndex >= 0) {
                // Faire défiler jusqu'à l'élément
                listState.animateScrollToItem(roomIndex)
            }
        }
    }

    val rotationAnimation by animateFloatAsState(
        targetValue = if (showCreateMenu) 45f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "FAB rotation"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "État des Lieux",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToPortal) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Retour au portail",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = !showSearchDialog }) {
                        Icon(
                            imageVector = if (showSearchDialog) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (showSearchDialog) "Fermer la recherche" else "Rechercher",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_adaptive_fore),
                        contentDescription = "Logo de l'application",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(48.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
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
                    .padding(16.dp),
                state = listState
            ) {
                // Afficher les pièces non groupées
                val ungroupedRooms = uiState.rooms.filter { currentRoom ->
                    uiState.roomGroups.none { group -> currentRoom.id in group.roomIds }
                }
                
                if (ungroupedRooms.isNotEmpty()) {
                    item {
                        Text(
                            text = "Pièces",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(ungroupedRooms) { room ->
                        val isCurrentSearchResult = uiState.searchResults.isNotEmpty() && 
                                                   uiState.currentSearchIndex >= 0 && 
                                                   uiState.searchResults.getOrNull(uiState.currentSearchIndex) == room
                        
                        RoomItem(
                            room = room,
                            onClick = { onNavigateToRoom(room.id) },
                            onLongClick = {
                                // Ouvrir le menu contextuel pour la pièce
                            },
                            isHighlighted = isCurrentSearchResult,
                            searchQuery = uiState.searchQuery
                        )
                    }
                }

                // Afficher les groupes de pièces
                items(uiState.roomGroups) { group ->
                    val roomsInGroup = uiState.rooms.filter { room -> room.id in group.roomIds }
                    
                    RoomGroupItem(
                        group = group,
                        rooms = roomsInGroup,
                        onRoomClick = { room -> onNavigateToRoom(room.id) },
                        onGroupClick = { viewModel.toggleGroupExpanded(group.id) },
                        onRoomDelete = { room -> viewModel.deleteRoom(room) },
                        onGroupDelete = { selectedGroup, deleteRooms ->
                            viewModel.deleteGroup(selectedGroup, deleteRooms)
                        },
                        onRemoveRoomFromGroup = { groupId, roomId ->
                            viewModel.removeRoomFromGroup(roomId, groupId)
                        },
                        searchQuery = uiState.searchQuery,
                        currentSearchRoom = viewModel.getCurrentSearchRoom()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Barre de boutons en bas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bouton des paramètres
                FloatingActionButton(
                    onClick = { showSettingsDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Paramètres"
                    )
                }

                // Bouton filtre
                FloatingActionButton(
                    onClick = { showFilterDialog = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtrer"
                    )
                }

                // Bouton recherche
                FloatingActionButton(
                    onClick = { showSearchDialog = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher"
                    )
                }

                // Bouton principal
                FloatingActionButton(
                    onClick = { showCreateMenu = !showCreateMenu },
                    modifier = Modifier.rotate(rotationAnimation),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Créer"
                    )
                }
            }

            if (showCreateMenu) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 88.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            showCreateMenu = false
                            showAddGroupDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Créer un groupe"
                            )
                            Text("Groupe")
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            showCreateMenu = false
                            onNavigateToAddRoom()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Créer une pièce"
                            )
                            Text("Pièce")
                        }
                    }
                }
            }

            if (showAddGroupDialog) {
                AlertDialog(
                    onDismissRequest = { showAddGroupDialog = false },
                    title = { Text("Nouveau groupe") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            TextField(
                                value = newGroupName,
                                onValueChange = { newGroupName = it },
                                label = { Text("Nom du groupe") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )

                            Text(
                                text = "Sélectionner les pièces",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
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
                        TextButton(onClick = { showAddGroupDialog = false }) {
                            Text("Annuler")
                        }
                    }
                )
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    onDismiss = { showSettingsDialog = false }
                )
            }

            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    title = { Text("Trier par") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option de tri par nom (A-Z)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSortOption(SortOption.NAME_ASC)
                                        showFilterDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.sortOption == SortOption.NAME_ASC,
                                    onClick = {
                                        viewModel.setSortOption(SortOption.NAME_ASC)
                                        showFilterDialog = false
                                    }
                                )
                                Text(
                                    text = "Nom (A-Z)",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            // Option de tri par nom (Z-A)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSortOption(SortOption.NAME_DESC)
                                        showFilterDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.sortOption == SortOption.NAME_DESC,
                                    onClick = {
                                        viewModel.setSortOption(SortOption.NAME_DESC)
                                        showFilterDialog = false
                                    }
                                )
                                Text(
                                    text = "Nom (Z-A)",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            // Option de tri par date (Plus récent)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSortOption(SortOption.DATE_DESC)
                                        showFilterDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.sortOption == SortOption.DATE_DESC,
                                    onClick = {
                                        viewModel.setSortOption(SortOption.DATE_DESC)
                                        showFilterDialog = false
                                    }
                                )
                                Text(
                                    text = "Date (Plus récent)",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            // Option de tri par date (Plus ancien)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSortOption(SortOption.DATE_ASC)
                                        showFilterDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.sortOption == SortOption.DATE_ASC,
                                    onClick = {
                                        viewModel.setSortOption(SortOption.DATE_ASC)
                                        showFilterDialog = false
                                    }
                                )
                                Text(
                                    text = "Date (Plus ancien)",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("Fermer")
                        }
                    }
                )
            }

            if (showSearchDialog) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    viewModel.updateSearchQuery(it)
                                    // Recherche automatique lorsque l'utilisateur tape
                                    if (it.length > 2) {
                                        viewModel.searchRooms()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Rechercher...") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { viewModel.searchRooms() }),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Rechercher",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            viewModel.clearSearch()
                                            showSearchDialog = false
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Effacer",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = totalSearchResults > 0,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$totalSearchResults résultat${if (totalSearchResults > 1) "s" else ""} trouvé${if (totalSearchResults > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (totalSearchResults > 0) {
                                        Text(
                                            text = "${currentSearchIndex + 1} / $totalSearchResults",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    
                                    FilledIconButton(
                                        onClick = viewModel::previousSearchResult,
                                        enabled = totalSearchResults > 1,
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Précédent"
                                        )
                                    }
                                    
                                    FilledIconButton(
                                        onClick = viewModel::nextSearchResult,
                                        enabled = totalSearchResults > 1,
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Suivant"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}