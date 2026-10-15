package com.salut.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.salut.ui.booking.BookingScreen
import com.salut.ui.bookings.BookingsScreen
import com.salut.ui.detail.TableDetailScreen
import com.salut.ui.filters.FiltersScreen
import com.salut.ui.settings.SettingsScreen
import com.salut.ui.tables.TablesScreen

@Composable
fun SalutNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.TABLES,
        modifier = modifier,
    ) {
        composable(Routes.TABLES) {
            TablesScreen(
                onTableClick = { id -> navController.navigate(Routes.tableDetail(id)) },
                onOpenFilters = { navController.navigate(Routes.FILTERS) },
                onOpenBookings = { navController.navigate(Routes.BOOKINGS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.FILTERS) {
            FiltersScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.TABLE_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_TABLE_ID) { type = NavType.LongType }),
        ) {
            TableDetailScreen(
                onBack = { navController.popBackStack() },
                onBook = { id -> navController.navigate(Routes.booking(id)) },
            )
        }
        composable(
            route = Routes.BOOKING,
            arguments = listOf(navArgument(Routes.ARG_TABLE_ID) { type = NavType.LongType }),
        ) {
            BookingScreen(
                onBack = { navController.popBackStack() },
                onBooked = {
                    navController.navigate(Routes.BOOKINGS) {
                        popUpTo(Routes.TABLES) { inclusive = false }
                    }
                },
            )
        }
        composable(Routes.BOOKINGS) {
            BookingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
