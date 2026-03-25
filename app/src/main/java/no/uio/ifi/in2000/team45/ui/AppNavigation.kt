package no.uio.ifi.in2000.team45.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    statisticViewModel: StatisticViewModel = viewModel()
) {
    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo("MapScreen") { saveState = true }
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

                MapStateScreen (
                    modifier = modifier,
                    onSettingsClick = { navigateTo("SettingsScreen") },
                    onStatisticClick = { navigateTo("StatisticScreen") },
                    mapViewModel = mapViewModel
                )
            }
            composable(route = "StatisticScreen") {
                StatisticScreen(
                    modifier = modifier,
                    onSettingsClick = { navigateTo("SettingsScreen") },
                    onMapClick = { navigateTo("MapScreen") },
                    statisticViewModel = statisticViewModel
                )
            }
            composable(route = "SettingsScreen") {
                SettingsScreen (
                    mapViewModel = mapViewModel,

                    modifier = modifier,
                    onMapClick = { navigateTo("MapScreen") },
                    onStatisticClick = { navigateTo("StatisticScreen") }
                )
            }
        }
    }
}
