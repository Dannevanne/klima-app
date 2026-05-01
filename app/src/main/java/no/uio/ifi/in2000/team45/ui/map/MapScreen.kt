package no.uio.ifi.in2000.team45.ui.map

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import kotlinx.coroutines.Job
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.mapbox.maps.CameraOptions
import no.uio.ifi.in2000.team45.ui.common.AppBottomBar
import no.uio.ifi.in2000.team45.ui.common.AppTopBar
import no.uio.ifi.in2000.team45.ui.common.BottomBarItemData
import no.uio.ifi.in2000.team45.ui.common.OverflowMenu
import no.uio.ifi.in2000.team45.ui.common.OverflowMenuButton
import no.uio.ifi.in2000.team45.ui.map.util.moveToPoint
import no.uio.ifi.in2000.team45.ui.theme.loadingColor

@Composable
fun MapStateScreen(
    mapViewModel: MapViewModel,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onHelpClick: () -> Unit,
    onStatisticClick: () -> Unit,
    onSavedPlacesClick: () -> Unit,
    savedPlacesViewModel: SavedPlacesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState = mapViewModel.mapUiState
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Shows module asking for permission in settings when user
    // clicks the location button without permission
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.need_location_permission)) },
            text = { Text(stringResource(R.string.permission_settings)) },
            confirmButton = {
                Button(onClick = {
                    showDialog = false

                    // Opens the apps settings in systems settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {

                        // Points to the apps settings
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.go_to_settings))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LocationPermission { locationGranted ->
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            MapScreen(
                mapViewModel = mapViewModel,
                savedPlacesViewModel = savedPlacesViewModel,
                locationGranted = locationGranted,
                onLocationClick = {
                    if (locationGranted){
                        mapViewModel.moveToUserLocation()
                    } else {
                        showDialog = true
                    }
                },
                onSettingsClick = onSettingsClick,
                onInfoClick = onInfoClick,
                onHelpClick = onHelpClick,
                onStatisticClick = onStatisticClick,
                onSavedPlacesClick = onSavedPlacesClick,
                modifier = modifier
            )
            when (uiState) {
                is MapUiState.Error -> Text("Error", color = Color.Red)
                else -> {}
            }

        }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    locationGranted: Boolean,
    onLocationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onHelpClick: () -> Unit,
    onStatisticClick: () -> Unit,
    onSavedPlacesClick: () -> Unit,
    savedPlacesViewModel: SavedPlacesViewModel,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.map),
                titleFontSize = 24.sp,
                navigationIcon = {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    OverflowMenuButton(onClick = { menuExpanded = true })
                    OverflowMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        onSettingsClick = {
                            menuExpanded = false
                            onSettingsClick()
                        },
                        onInfoClick = {
                            menuExpanded = false
                            onInfoClick()
                        },
                        onHelpClick = {
                            menuExpanded = false
                            onHelpClick()
                        }
                    )
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                items = listOf(
                    BottomBarItemData(
                        label = stringResource(R.string.nav_map),
                        onClick = {},
                        icon = { Icon(Icons.Filled.Place, contentDescription = null) }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.saved_places),
                        onClick = onSavedPlacesClick,
                        icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.nav_statistics),
                        onClick = onStatisticClick,
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_analytics),
                                contentDescription = null
                            )
                        }
                    )
                )
            )
        }
    ) { innerPadding ->
        DataMap(
            mapViewModel = mapViewModel,
            locationGranted = locationGranted,
            onLocationClick = onLocationClick,
            onSavedPlacesClick = onSavedPlacesClick,
            savedPlacesViewModel = savedPlacesViewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun DataMap(
    mapViewModel: MapViewModel,
    locationGranted: Boolean,
    onLocationClick: () -> Unit,
    onSavedPlacesClick: () -> Unit,
    savedPlacesViewModel: SavedPlacesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState = mapViewModel.mapUiState

    // The selected theme is translated to a boolean that decides if the theme
    // should be dark or not.
    val useDarkMap = useDarkThemeFrom(
        theme = mapViewModel.appTheme,
        systemIsDark = isSystemInDarkTheme()
    )
    // Long-press save dialog state
    var pendingPoint by remember { mutableStateOf<Point?>(null) }
    var locationName by remember { mutableStateOf("") }
    var mapWidth by remember { mutableStateOf(0) }
    var mapHeight by remember { mutableStateOf(0) }
    val currentMapWidth by rememberUpdatedState(mapWidth)
    val currentMapHeight by rememberUpdatedState(mapHeight)
    var mapViewRef by remember { mutableStateOf<com.mapbox.maps.MapView?>(null) }

    // Sets the cameras start position
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(mapViewModel.lastCameraCenter)
            zoom(mapViewModel.lastCameraZoom)
            pitch(0.0)
            bearing(0.0)
        }
    }

    val activeOverlay = mapViewModel.activeOverlay

    // Switch overlay: leave the camera alone if it's already inside the
    // overlay's allowed zoom band. Only nudge when out of range, in which
    // case clamp to the nearest edge (zoom in if too far out, zoom out if
    // too far in).
    LaunchedEffect(activeOverlay) {
        val currentZoom = mapViewRef?.mapboxMap?.cameraState?.zoom ?: return@LaunchedEffect
        val targetZoom = currentZoom.coerceIn(activeOverlay.minZoom, activeOverlay.maxZoom)
        if (targetZoom != currentZoom) {
            val camera = CameraOptions.Builder()
                .zoom(targetZoom)
                .center(mapViewModel.lastCameraCenter)
                .build()
            viewportState.easeTo(camera)
        }
    }
    
    val bitmap = (uiState as? MapUiState.Success)?.bitmap
    val overlayBBox = (uiState as? MapUiState.Success)?.bbox

    // Compose state from MapViewModel
    val searchQuery = mapViewModel.searchQuery
    val searchSuggestions = mapViewModel.searchSuggestions
    val isSearching = mapViewModel.isSearching

    // Saving the bitmap creating as a job, so it is cancellable.
    // But this "DataMap" should have its own viewmodel for all of this state hosting.
    var imageJob by remember {mutableStateOf<Job?>(null)}

    DisposableEffect(Unit) {
        onDispose {
            imageJob?.cancel()
            mapViewModel.cancelMapScreenWork()
        }
    }

    LaunchedEffect(viewportState.cameraState) {
        val camera = viewportState.cameraState ?: return@LaunchedEffect
        mapViewModel.saveCamera(
            center = camera.center,
            zoom = camera.zoom
        )
    }

    // Bumped each time the user pinches the zoom-out button while at the
    // overlay's minZoom; LaunchedEffect re-arms the auto-dismiss timer.
    var zoomBlockedNonce by remember { mutableStateOf(0) }
    var zoomBlockedVisible by remember { mutableStateOf(false) }
    LaunchedEffect(zoomBlockedNonce) {
        if (zoomBlockedNonce > 0) {
            zoomBlockedVisible = true
            kotlinx.coroutines.delay(2000)
            zoomBlockedVisible = false
        }
    }

    // Hide the Mapbox GL surface behind a themed scrim until Mapbox itself
    // says the first frame is rendered. Listening to subscribeMapLoaded means
    // the scrim stays up through tile loading, not just the surface attach.
    // The 5s timeout is a safety net so we never get stuck on the spinner.
    var mapSurfaceReady by remember { mutableStateOf(false) }
    LaunchedEffect(mapViewRef) {
        mapSurfaceReady = false
        val mv = mapViewRef ?: return@LaunchedEffect
        val ready = kotlinx.coroutines.CompletableDeferred<Unit>()
        val sub = mv.mapboxMap.subscribeMapLoaded { ready.complete(Unit) }
        try {
            kotlinx.coroutines.withTimeoutOrNull(5000) { ready.await() }
        } finally {
            sub.cancel()
        }
        mapSurfaceReady = true
    }

    var overlayPillExpanded by remember { mutableStateOf(false) }
    val savedPlaces by savedPlacesViewModel.locations.collectAsState()
    var selectedSavedPlace by remember { mutableStateOf<no.uio.ifi.in2000.team45.data.savedplaces.SavedPlacesLocation?>(null) }
    var showOverlayLegend by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        MapContent(
            viewportState = viewportState,
            mapViewRef = mapViewRef,
            onMapViewReady = { mapViewRef = it },
            onMapSizeChanged = { width, height ->
                mapWidth = width
                mapHeight = height
            },
            pendingBitmap = bitmap,
            overlayBBox = overlayBBox,
            useDarkMap = useDarkMap,
            currentMapWidth = currentMapWidth,
            currentMapHeight = currentMapHeight,
            locationGranted = locationGranted,
            mapViewModel = mapViewModel,
            savedPlaces = savedPlaces,
            onSavedPlaceClick = { selectedSavedPlace = it },
            onMapLongClick = { point ->
                pendingPoint = point
                locationName = ""
            },
            onMapClick = {
                focusManager.clearFocus()
                overlayPillExpanded = false
                selectedSavedPlace = null
            },
            context = context,
            modifier = Modifier.fillMaxSize()
        )

        MapSearchSection(
            searchQuery = searchQuery,
            searchSuggestions = searchSuggestions,
            isSearching = isSearching,
            onSearchQueryChange = { mapViewModel.updateSearchQuery(it) },
            onClearSearch = { mapViewModel.clearSearch() },
            onSuggestionClick = { item ->
                mapViewModel.selectSuggestion(item) { point ->
                    moveToPoint(point, viewportState)
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapZoomControlsSection(
                mapView = mapViewRef,
                viewportState = viewportState,
                mapViewModel = mapViewModel,
                activeOverlay = mapViewModel.activeOverlay,
                onZoomOutBlocked = { zoomBlockedNonce++ }
            )

            MapLocationButton(
                locationGranted = locationGranted,
                onClick = onLocationClick,
            )
        }

        MapOverlaySelector(
            activeOverlay = mapViewModel.activeOverlay,
            expanded = overlayPillExpanded,
            onExpandedChange = { overlayPillExpanded = it },
            onOverlaySelected = { type -> mapViewModel.setOverlay(type, context) },
            onLegendClick = { showOverlayLegend = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        )

        if (showOverlayLegend) {
            OverlayLegendSheet(
                overlay = mapViewModel.activeOverlay,
                onDismiss = { showOverlayLegend = false }
            )
        }

        selectedSavedPlace?.let { place ->
            SavedPlacePopup(
                place = place,
                onOpenSavedPlaces = {
                    savedPlacesViewModel.requestFocus(place)
                    selectedSavedPlace = null
                    onSavedPlacesClick()
                },
                onDismiss = { selectedSavedPlace = null },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 96.dp, start = 16.dp, end = 16.dp)
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = zoomBlockedVisible,
            enter = androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(180)
            ),
            exit = androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(180)
            ),
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Padding gives the elevation shadow room to render without
            // getting clipped by the AnimatedVisibility's tight bounds.
            Box(modifier = Modifier.padding(8.dp)) {
                androidx.compose.material3.Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.change_layer_to_zoom_out),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = !mapSurfaceReady,
            // Snap in so the scrim is up on first frame, hiding Mapbox's black
            // GL clear; only the dismissal animates as the map comes into view.
            enter = androidx.compose.animation.EnterTransition.None,
            exit = androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(250)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    pendingPoint?.let { point ->
        MapSaveLocationDialog(
            point = point,
            locationName = locationName,
            onLocationNameChange = { locationName = it },
            onDismiss = { pendingPoint = null },
            savedPlacesViewModel = savedPlacesViewModel,
        )
    }
}

// Composable to change map look when the selected theme changes
@Composable
fun MapThemeEffect(useDarkMap: Boolean){
    MapEffect(useDarkMap) { mapView ->
        mapView.mapboxMap.loadStyle(
            if(useDarkMap) "mapbox://styles/mapbox/navigation-night-v1"
            else Style.STANDARD
        )
    }
}