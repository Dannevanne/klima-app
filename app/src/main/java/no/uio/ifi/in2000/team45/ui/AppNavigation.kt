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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.IsOnline
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesScreen
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel
import no.uio.ifi.in2000.team45.ui.help.HelpScreen
import no.uio.ifi.in2000.team45.ui.info.InfoScreen
import no.uio.ifi.in2000.team45.ui.map.MapStateScreen
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.settings.SettingsScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel

private const val ROUTE_MAP = "MapScreen"
private const val ROUTE_STATISTICS = "StatisticScreen"
private const val ROUTE_SAVED = "SavedPlacesScreen"
private const val ROUTE_SETTINGS = "SettingsScreen"
private const val ROUTE_INFO = "InfoScreen"
private const val ROUTE_HELP = "HelpScreen"

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mapViewModel: MapViewModel,
    statisticViewModel: StatisticViewModel = viewModel(),
    savedPlacesViewModel: SavedPlacesViewModel = viewModel()
) {
    val isOnline = IsOnline()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Track the route locally; the NavController only seeds the initial value.
    // Bypassing NavHost lets every screen stay composed in memory after first
    // launch, so switching tabs is instant from the very first tap.
    var route by remember { mutableStateOf(ROUTE_MAP) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val backStackRoute = backStackEntry?.destination?.route
    if (backStackRoute != null && backStackRoute != route) route = backStackRoute

    fun navigateTo(target: String) {
        if (target == route) return
        route = target
        keyboardController?.hide()
    }

    fun popBack() {
        // Settings/Info/Help all return to the map for now.
        route = ROUTE_MAP
        keyboardController?.hide()
    }

    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            // The map sits at the bottom and is always composed.
            MapStateScreen(
                modifier = modifier,
                onSettingsClick = { navigateTo(ROUTE_SETTINGS) },
                onInfoClick = { navigateTo(ROUTE_INFO) },
                onHelpClick = { navigateTo(ROUTE_HELP) },
                onStatisticClick = { navigateTo(ROUTE_STATISTICS) },
                onSavedPlacesClick = { navigateTo(ROUTE_SAVED) },
                mapViewModel = mapViewModel,
                savedPlacesViewModel = savedPlacesViewModel
            )

            ScreenLayer(visible = route == ROUTE_STATISTICS) {
                StatisticScreen(
                    modifier = modifier,
                    onSettingsClick = { navigateTo(ROUTE_SETTINGS) },
                    onInfoClick = { navigateTo(ROUTE_INFO) },
                    onHelpClick = { navigateTo(ROUTE_HELP) },
                    onMapClick = { navigateTo(ROUTE_MAP) },
                    onSavedPlacesClick = { navigateTo(ROUTE_SAVED) },
                    statisticViewModel = statisticViewModel,
                    savedPlacesViewModel = savedPlacesViewModel
                )
            }
            ScreenLayer(visible = route == ROUTE_SAVED) {
                SavedPlacesScreen(
                    savedPlacesViewModel = savedPlacesViewModel,
                    onSettingsClick = { navigateTo(ROUTE_SETTINGS) },
                    onInfoClick = { navigateTo(ROUTE_INFO) },
                    onHelpClick = { navigateTo(ROUTE_HELP) },
                    onMapClick = { navigateTo(ROUTE_MAP) },
                    onStatisticClick = { navigateTo(ROUTE_STATISTICS) },
                    onShowOnMap = { lat, lon ->
                        mapViewModel.showLocation(Point.fromLngLat(lon, lat), context)
                        navigateTo(ROUTE_MAP)
                    },
                    modifier = modifier
                )
            }
            ScreenLayer(visible = route == ROUTE_SETTINGS) {
                SettingsScreen(
                    mapViewModel = mapViewModel,
                    modifier = modifier,
                    onClick = { popBack() },
                    statisticViewModel = statisticViewModel,
                    savedPlacesViewModel = savedPlacesViewModel
                )
            }
            ScreenLayer(visible = route == ROUTE_INFO) {
                InfoScreen(
                    onClick = { popBack() },
                    modifier = modifier
                )
            }
            ScreenLayer(visible = route == ROUTE_HELP) {
                HelpScreen(
                    onClick = { popBack() },
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

// Renders the screen always (so it stays in memory) but pushes inactive
// screens behind everything and absorbs no input. The active layer floats on
// top with full opacity.
@Composable
private fun ScreenLayer(visible: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (visible) 1f else -1f)
            .alpha(if (visible) 1f else 0f)
            .then(
                if (visible) Modifier
                else Modifier.pointerInput(Unit) { /* swallow input when hidden */ }
            )
    ) {
        content()
    }
}
