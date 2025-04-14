package com.example.etatdeslieux.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun CameraPermission(
    onPermissionGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            if (shouldShowRationale(context)) {
                showRationaleDialog = true
            } else {
                showSettingsDialog = true
            }
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRationaleDialog = false
                onDismiss()
            },
            title = { Text("Permission nécessaire") },
            text = { Text("L'accès à la caméra est nécessaire pour prendre des photos.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Réessayer")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRationaleDialog = false
                    onDismiss()  
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSettingsDialog = false
                onDismiss()
            },
            title = { Text("Permission refusée") },
            text = { Text("L'accès à la caméra est nécessaire. Veuillez l'activer dans les paramètres.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    openAppSettings(context)
                }) {
                    Text("Paramètres")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSettingsDialog = false
                    onDismiss() 
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        when {
            isPermissionGranted(context) -> onPermissionGranted()
            shouldShowRationale(context) -> showRationaleDialog = true
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

private fun isPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun shouldShowRationale(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_DENIED
}

private fun openAppSettings(context: Context) {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(this)
    }
}
