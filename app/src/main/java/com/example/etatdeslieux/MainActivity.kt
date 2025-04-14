package com.example.etatdeslieux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.etatdeslieux.ui.components.LoadingSpinner
import com.example.etatdeslieux.ui.navigation.NavGraph
import com.example.etatdeslieux.ui.theme.AppTheme
import com.example.etatdeslieux.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme by settingsViewModel.darkTheme.collectAsState(initial = false)
            val fontScale by settingsViewModel.fontScale.collectAsState(initial = 1.0f)

            AppTheme(
                darkTheme = darkTheme,
                fontScale = fontScale,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }

    }
    
}
@Composable
fun MainScreen() {
    var isLoading by remember { mutableStateOf(true) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Contenu principal
        
        if (isLoading) {
            LoadingSpinner(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
