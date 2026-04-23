package no.uio.ifi.in2000.team45.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.IsOnline
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteScreen
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel
import no.uio.ifi.in2000.team45.ui.help.HelpScreen
import no.uio.ifi.in2000.team45.ui.info.InfoScreen
import no.uio.ifi.in2000.team45.ui.map.MapStateScreen
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.settings.SettingsScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel
import java.nio.file.WatchEvent

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mapViewModel: MapViewModel,
    statisticViewModel: StatisticViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel()
) {

    val isOnline = IsOnline()

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

        // Shows a bar telling no internettconnection
        if (!isOnline) {
            var dismissed by remember { mutableStateOf(false) }
            if (!dismissed) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        dismissAction = {
                            IconButton(onClick = {dismissed = true}) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.close)
                                )
                            }
                        }
                    ) {
                        Text(stringResource(R.string.no_internettconnection))
                    }
                }
            }
        }
    }
}
