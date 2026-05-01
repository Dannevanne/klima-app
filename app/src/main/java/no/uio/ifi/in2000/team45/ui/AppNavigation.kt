package no.uio.ifi.in2000.team45.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
    savedPlacesViewModel: SavedPlacesViewModel = viewModel(),
    musicEnabled: Boolean,
    onMusicEnabledChanged: (Boolean) -> Unit,
    soundEnabled: Boolean,
    onSoundEnabledChanged: (Boolean) -> Unit
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

    var dismissed by remember { mutableStateOf(false) }

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
                    musicEnabled = musicEnabled,
                    onMusicEnabledChanged = onMusicEnabledChanged,
                    soundEnabled = soundEnabled,
                    onSoundEnabledChanged = onSoundEnabledChanged
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
                            contentDescription = stringResource(R.string.no_internettconnection),
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.no_internettconnection),
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
