package com.example.etatdeslieux.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    onNavigateToEtatDesLieux: () -> Unit,
    onNavigateToWorkingOnIt: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onNavigateToEtatDesLieux,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("État des Lieux")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateToWorkingOnIt,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Working on it")
        }
    }
}