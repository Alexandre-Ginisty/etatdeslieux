package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(
    onSettingsClick: () -> Unit,
    onFilterClick: () -> Unit,
    onAddClick: () -> Unit,
    isAddExpanded: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton Paramètres
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Paramètres",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Bouton Filtre
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filtrer",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Bouton Ajouter avec animation
            IconButton(onClick = onAddClick) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter",
                    modifier = Modifier.rotate(if (isAddExpanded) 45f else 0f),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
