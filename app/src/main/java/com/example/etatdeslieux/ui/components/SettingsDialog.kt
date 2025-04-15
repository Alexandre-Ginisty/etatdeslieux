package com.example.etatdeslieux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.etatdeslieux.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val darkTheme by settingsViewModel.darkTheme.collectAsState(initial = false)
    val fontScale by settingsViewModel.fontScale.collectAsState(initial = 1.0f)

    var tempFontScale by remember { mutableStateOf(fontScale) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Paramètres",
                    style = MaterialTheme.typography.titleLarge
                )

                // Thème sombre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thème sombre",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { settingsViewModel.updateDarkTheme(it) }
                    )
                }

                // Taille de police
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Taille de police",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = tempFontScale,
                        onValueChange = { tempFontScale = it },
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

                // Boutons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            settingsViewModel.updateFontScale(tempFontScale)
                            onDismiss()
                        }
                    ) {
                        Text("Appliquer")
                    }
                }
            }
        }
    }
}
