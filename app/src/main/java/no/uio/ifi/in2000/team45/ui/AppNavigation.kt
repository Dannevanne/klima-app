package no.uio.ifi.in2000.team45.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.isOnline
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesScreen
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel
import no.uio.ifi.in2000.team45.ui.help.HelpScreen
import no.uio.ifi.in2000.team45.ui.info.InfoScreen
import no.uio.ifi.in2000.team45.ui.map.MapStateScreen
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.settings.SettingsScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticScreen
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel

// navigation root identifiers for all the available screens
private const val ROUTE_MAP = "MapScreen"
private const val ROUTE_STATISTICS = "StatisticScreen"
private const val ROUTE_SAVED = "SavedPlacesScreen"
private const val ROUTE_SETTINGS = "SettingsScreen"
private const val ROUTE_INFO = "InfoScreen"
private const val ROUTE_HELP = "HelpScreen"

/**
 * Root composable that manages app navigation and screen layering.
 *
 * @param modifier Modifier applied to the root container.
 * @param navController Navigation controller used only for initial state sync.
 * @param mapViewModel ViewModel controlling map state and interactions.
 * @param statisticViewModel ViewModel managing statistics search and data.
 * @param savedPlacesViewModel ViewModel managing saved places.
 * @param soundEnabled Current sound enabled state.
 * @param onSoundEnabledChanged Callback for toggling sound setting.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mapViewModel: MapViewModel,
    statisticViewModel: StatisticViewModel = viewModel(),
    savedPlacesViewModel: SavedPlacesViewModel = viewModel(),
    soundEnabled: Boolean,
    onSoundEnabledChanged: (Boolean) -> Unit
) {
    val isOnline = isOnline()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Track the route locally; the NavController only seeds the initial value.
    // Bypassing NavHost lets every screen stay composed in memory after first
    // launch, so switching tabs is instant from the very first tap.
    var route by remember { mutableStateOf(ROUTE_MAP) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val backStackRoute = backStackEntry?.destination?.route
    if (backStackRoute != null && backStackRoute != route) route = backStackRoute

    var dismissed by remember { mutableStateOf(false) }

    // Aux screens (settings/info/help) need to return to whichever tab the
    // user came from. Stack so navigating aux -> aux still unwinds correctly.
    val auxRoutes = setOf(ROUTE_SETTINGS, ROUTE_INFO, ROUTE_HELP)
    val auxBackStack = remember { mutableStateListOf<String>() }

    fun navigateTo(target: String) {
        if (target == route) return
        if (target in auxRoutes) auxBackStack.add(route)
        route = target
        keyboardController?.hide()
    }

    fun popBack() {
        route = auxBackStack.removeLastOrNull() ?: ROUTE_MAP
        keyboardController?.hide()
    }

    BackHandler(enabled = route in auxRoutes) { popBack() }

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
                onShowInStatistics = { lat, lon, label ->
                    statisticViewModel.selectSavedPlace(lat, lon, label)
                    navigateTo(ROUTE_STATISTICS)
                },
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
                    onShowOnMap = { lat, lon ->
                        mapViewModel.showLocation(Point.fromLngLat(lon, lat), context)
                        navigateTo(ROUTE_MAP)
                    },
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
                    onShowInStatistics = { lat, lon, label ->
                        statisticViewModel.selectSavedPlace(lat, lon, label)
                        navigateTo(ROUTE_STATISTICS)
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
                    savedPlacesViewModel = savedPlacesViewModel,
                    soundEnabled = soundEnabled,
                    onSoundEnabledChanged = onSoundEnabledChanged
                )
            }
            ScreenLayer(visible = route == ROUTE_INFO) {
                InfoScreen(
                    onClick = { popBack() },
                    modifier = modifier,
                    onHelpClick = { navigateTo(ROUTE_HELP) }
                )
            }
            ScreenLayer(visible = route == ROUTE_HELP) {
                HelpScreen(
                    onClick = { popBack() },
                    modifier = modifier
                )
            }
        }

        // Shows a bar telling no internet-connection
        if (!isOnline && !dismissed) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 60.dp, vertical = 300.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.background, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = stringResource(R.string.no_internet_connection),
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.no_internet_connection),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(
                                stringResource(R.string.try_again),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.padding(8.dp))

                        OutlinedButton(
                            onClick = {dismissed = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                stringResource(R.string.use_without_internet),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (isOnline) dismissed = false
    }
}

/**
 * Renders a screen layer that is always composed but conditionally visible.
 *
 * @param visible Whether this layer is currently active and interactive.
 * @param content Screen content to render inside the layer.
 */
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
