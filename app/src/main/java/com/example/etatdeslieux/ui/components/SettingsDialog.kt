package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.etatdeslieux.ui.theme.AppTheme

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    var sliderPosition by remember { mutableStateOf(AppTheme.fontScale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Paramètres",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thème
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thème sombre")
                    Switch(
                        checked = AppTheme.isDarkTheme,
                        onCheckedChange = { AppTheme.toggleTheme() }
                    )
                }

                // Taille de la police
                Column {
                    Text("Taille de la police")
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0.8f..1.5f,
                        steps = 6,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("A", style = MaterialTheme.typography.bodySmall)
                        Text("A", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    AppTheme.updateFontScale(sliderPosition)
                    onDismiss()
                }
            ) {
                Text("Appliquer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
