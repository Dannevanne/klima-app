package no.uio.ifi.in2000.team45.ui

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.team45.ui.map.MapScreen
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.statistic.StatisticScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel


@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mapViewModel: MapViewModel = MapViewModel(),
    statisticViewModel: StatisticViewModel = StatisticViewModel()
) {
    Surface {
        NavHost(
            navController = navController,
            startDestination = "MapScreen"
        ) {
            composable(route = "MapScreen") {
                MapScreen (
                    modifier = modifier,
                    onSettingsClick = {},
                    onStatisticsClick = {},
                    mapViewModel = mapViewModel
                )
            }
            composable(route = "StatisticScreen") {
                StatisticScreen (
                    modifier = modifier,
                    onClick = {},
                    statisticViewModel = statisticViewModel
                )
            }
        }
    }
}