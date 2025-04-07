package com.example.etatdeslieux.ui.navigation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.etatdeslieux.ui.screens.addroom.AddRoomScreen
import com.example.etatdeslieux.ui.screens.home.HomeScreen
import com.example.etatdeslieux.ui.screens.room.RoomScreen
import com.example.etatdeslieux.ui.screens.room.RoomViewModel
import com.example.etatdeslieux.utils.ComposeFileProvider

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddRoom : Screen("add_room")
    object Room : Screen("room/{roomId}") {
        fun createRoute(roomId: Long) = "room/$roomId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        addNavGraph(navController = navController)
    }
}

fun NavGraphBuilder.addNavGraph(
    navController: NavHostController
) {
    composable(route = Screen.Home.route) {
        HomeScreen(
            onNavigateToAddRoom = { navController.navigate(Screen.AddRoom.route) },
            onNavigateToRoom = { roomId -> navController.navigate(Screen.Room.createRoute(roomId)) }
        )
    }

    composable(route = Screen.AddRoom.route) {
        AddRoomScreen(
            onNavigateBack = { navController.popBackStack() },
            onRoomAdded = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.Room.route,
        arguments = listOf(navArgument("roomId") { type = NavType.LongType })
    ) { backStackEntry ->
        val roomId = backStackEntry.arguments?.getLong("roomId") ?: return@composable
        var photoUri by remember { mutableStateOf<Uri?>(null) }
        val context = LocalContext.current
        val viewModel: RoomViewModel = hiltViewModel()

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && photoUri != null) {
                // Afficher le dialogue de commentaire
                photoUri?.let { uri ->
                    viewModel.addPhoto(uri, "")
                }
            }
            photoUri = null
        }

        RoomScreen(
            roomId = roomId,
            onNavigateBack = { navController.popBackStack() },
            onTakePhoto = {
                val uri = ComposeFileProvider.getImageUri(context)
                photoUri = uri
                cameraLauncher.launch(uri)
            }
        )
    }
}
