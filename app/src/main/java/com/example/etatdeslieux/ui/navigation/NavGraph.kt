package com.example.etatdeslieux.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.etatdeslieux.ui.screens.home.HomeScreen
import com.example.etatdeslieux.ui.screens.addroom.AddRoomScreen
import com.example.etatdeslieux.ui.screens.main.MainScreen
import com.example.etatdeslieux.ui.screens.room.RoomDetailScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Home : Screen("home")
    object AddRoom : Screen("add_room")
    object WorkingOnIt : Screen("working_on_it")
    object RoomDetail : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToEtatDesLieux = { navController.navigate(Screen.Home.route) },
                onNavigateToWorkingOnIt = { navController.navigate(Screen.WorkingOnIt.route) }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRoom = { roomId ->
                    navController.navigate(Screen.RoomDetail.createRoute(roomId))
                },
                onNavigateToAddRoom = {
                    navController.navigate(Screen.AddRoom.route)
                }
            )
        }

        composable(Screen.WorkingOnIt.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Fonctionnalité en développement")
            }
        }

        composable(Screen.AddRoom.route) {
            AddRoomScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRoomAdded = { roomId ->
                    navController.navigate(Screen.RoomDetail.createRoute(roomId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.RoomDetail.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.LongType }
            )
        ) {
            val roomId = it.arguments?.getLong("roomId") ?: return@composable
            RoomDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}