package com.aliceqr.vicevirtue.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.screens.addtrackable.AddTrackableScreen
import com.aliceqr.vicevirtue.ui.screens.dashboard.DashboardScreen
import com.aliceqr.vicevirtue.ui.screens.detail.DetailScreen
import com.aliceqr.vicevirtue.ui.screens.history.HistoryScreen
import com.aliceqr.vicevirtue.ui.screens.logevent.LogEventScreen
import com.aliceqr.vicevirtue.ui.screens.settings.SettingsScreen

@Composable
fun ViceVirtueNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(navController, startDestination = startDestination) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onNavigateToAdd = { navController.navigate(Screen.AddTrackable.createRoute()) },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onNavigateToLog = { id -> navController.navigate(Screen.LogEvent.createRoute(id)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(
            Screen.AddTrackable.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("trackableId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddTrackableScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.LogEvent.route,
            arguments = listOf(navArgument("trackableId") { type = NavType.LongType })
        ) {
            LogEventScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.History.route,
            arguments = listOf(
                navArgument("trackableId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            HistoryScreen(navController = navController)
        }
        composable(
            Screen.Detail.route,
            arguments = listOf(navArgument("trackableId") { type = NavType.LongType })
        ) {
            DetailScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
