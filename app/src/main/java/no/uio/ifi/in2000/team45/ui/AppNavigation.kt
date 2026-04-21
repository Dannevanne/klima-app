package no.uio.ifi.in2000.team45.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteScreen
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel
import no.uio.ifi.in2000.team45.ui.help.HelpScreen
import no.uio.ifi.in2000.team45.ui.info.InfoScreen
import no.uio.ifi.in2000.team45.ui.map.MapStateScreen
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.settings.SettingsScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mapViewModel: MapViewModel,
    statisticViewModel: StatisticViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel()
) {
    fun navigateTo(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    Surface {
        NavHost(
            navController = navController,
            startDestination = "MapScreen"
        ) {
            composable(route = "MapScreen") {
                MapStateScreen(
                    modifier = modifier,
                    onSettingsClick = { navigateTo("SettingsScreen") },
                    onInfoClick = { navigateTo("InfoScreen") },
                    onHelpClick = { navigateTo("HelpScreen") },
                    onStatisticClick = { navigateTo("StatisticScreen") },
                    onFavoritesClick = { navigateTo("FavoriteScreen") },
                    mapViewModel = mapViewModel,
                    favoriteViewModel = favoriteViewModel
                )
            }
            composable(route = "StatisticScreen") {
                StatisticScreen(
                    modifier = modifier,
                    onSettingsClick = { navigateTo("SettingsScreen") },
                    onInfoClick = { navigateTo("InfoScreen") },
                    onHelpClick = { navigateTo("HelpScreen") },
                    onMapClick = { navigateTo("MapScreen") },
                    onFavoritesClick = { navigateTo("FavoriteScreen") },
                    statisticViewModel = statisticViewModel,
                    favoriteViewModel = favoriteViewModel
                )
            }
            composable(route = "SettingsScreen") {
                SettingsScreen(
                    mapViewModel = mapViewModel,
                    modifier = modifier,
                    onClick = { navController.popBackStack() }
                )
            }
            composable(route = "FavoriteScreen") {
                FavoriteScreen(
                    favoriteViewModel = favoriteViewModel,
                    onSettingsClick = { navigateTo("SettingsScreen") },
                    onInfoClick = { navigateTo("InfoScreen") },
                    onHelpClick = { navigateTo("HelpScreen") },
                    onMapClick = { navigateTo("MapScreen") },
                    onStatisticClick = { navigateTo("StatisticScreen") },
                    modifier = modifier
                )
            }
            composable(route = "InfoScreen") {
                InfoScreen(
                    onClick = { navController.popBackStack() },
                    modifier = modifier
                )
            }
            composable(route = "HelpScreen") {
                HelpScreen(
                    onClick = { navController.popBackStack()},
                    modifier = modifier
                )
            }
        }
    }
}
