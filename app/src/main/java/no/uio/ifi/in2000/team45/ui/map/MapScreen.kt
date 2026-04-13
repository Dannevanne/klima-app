package no.uio.ifi.in2000.team45.ui.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.ImageSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.sources.updateImage
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.toCameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.map.OverlayType
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel
import no.uio.ifi.in2000.team45.ui.logo.LogoScreen
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextField
import androidx.compose.ui.text.style.LineHeightStyle
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import androidx.compose.foundation.lazy.items
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener

@Composable
fun MapStateScreen(
    mapViewModel: MapViewModel,
    onSettingsClick: () -> Unit,
    onStatisticClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    favoriteViewModel: FavoriteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState = mapViewModel.mapUiState
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    //Shows module asking for permission in settings when user clicks the locationbutton without permission
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.need_location_permission)) },
            text = { Text(stringResource(R.string.permission_settings)) },
            confirmButton = {
                Button(onClick = {
                    showDialog = false

                    //Opens the apps settings in systemssettings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {

                        //Points to the apps settings
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
                favoriteViewModel = favoriteViewModel,
                locationGranted = locationGranted,
                onLocationClick = {
                    if (locationGranted){
                        mapViewModel.moveToUserLocation()
                    } else {
                        showDialog = true
                    }
                },
                onSettingsClick = onSettingsClick,
                onStatisticClick = onStatisticClick,
                onFavoritesClick = onFavoritesClick,
                modifier = modifier
            )
            when (uiState) {
                is MapUiState.Loading -> CircularProgressIndicator()
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
    onStatisticClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    favoriteViewModel: FavoriteViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    Icon(Icons.Filled.Place,
                        contentDescription = "") },
                title = {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.map),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold)
                }
            )
        },
        bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ){
                    Row(
                        modifier = Modifier
                            .padding(2.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,

                    ) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.to_settings))
                        }
                        IconButton({}) {
                            Icon(Icons.Filled.Place, contentDescription = "")
                        }
                        IconButton(onClick = onStatisticClick) {
                            Icon(Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.to_statistics))
                        }
                        IconButton(onClick = onFavoritesClick) {
                            Icon(Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favoritter")
                        }
                    }
                    }
            }
    ) { innerPadding ->
        DataMap(
            mapViewModel = mapViewModel,
            locationGranted = locationGranted,
            onLocationClick = onLocationClick,
            favoriteViewModel = favoriteViewModel,
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
    favoriteViewModel: FavoriteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState = mapViewModel.mapUiState
    val useDarkMap = useDarkThemeFrom(
        theme = mapViewModel.mapTheme,
        systemIsDark = isSystemInDarkTheme()
    )
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    // Long-press save dialog state
    var pendingPoint by remember { mutableStateOf<Point?>(null) }
    var locationName by remember { mutableStateOf("") }
    var mapWidth by remember { mutableStateOf(0) }
    var mapHeight by remember { mutableStateOf(0) }
    val currentMapWidth by rememberUpdatedState(mapWidth)
    val currentMapHeight by rememberUpdatedState(mapHeight)
    var mapViewRef by remember { mutableStateOf<com.mapbox.maps.MapView?>(null) }
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(11.05, 60.5))
            zoom(11.5)
            pitch(0.0)
            bearing(0.0)
        }
    }
    val overlayURL = (uiState as? MapUiState.Success)?.overlayURL
    val overlayBBox = (uiState as? MapUiState.Success)?.bbox
    val searchQuery = mapViewModel.searchQuery
    val searchSuggestions = mapViewModel.searchSuggestions
    val isSearching = mapViewModel.isSearching

    // saving the bitmap creating as a job, so it is cancellable
    // but this "DataMap" should have its own viewmodel for all of this state hosting
    var imageJob by remember {mutableStateOf<Job?>(null)}

    DisposableEffect(Unit) {
        onDispose {
            imageJob?.cancel()
            mapViewModel.cancelMapScreenWork()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    mapWidth = it.width
                    mapHeight = it.height
                },
            mapViewportState = viewportState
        ){
            MapEffect(Unit) { mapView ->
                mapViewRef = mapView
            }

            DisposableEffect(mapViewRef) {
                val mapView = mapViewRef
                if (mapView == null) {
                    onDispose {  }
                }
                else{
                    val longClickListener = OnMapLongClickListener { point ->
                        pendingPoint = point
                        locationName = ""
                        true
                    }

                    mapView.mapboxMap.addOnMapLongClickListener(longClickListener)

                    onDispose {
                        mapView.mapboxMap.removeOnMapLongClickListener(longClickListener)
                    }
                }
            }

            //Activates the blue location-dot
            //Rerun every time locationGranted changes
            DisposableEffect(mapViewRef, locationGranted) {
                val mapView = mapViewRef
                if(mapView == null){
                    onDispose {  }
                }
                else{
                    mapView.location.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = false)
                        enabled = locationGranted
                        pulsingEnabled = true
                    }

                    val locationListener = OnIndicatorPositionChangedListener { point ->
                        mapViewModel.updateUserLocation(point)
                    }

                    if(locationGranted) {
                        mapView.location.addOnIndicatorPositionChangedListener(locationListener)
                    }

                    onDispose {
                        mapView.location.removeOnIndicatorPositionChangedListener(locationListener)
                    }
                }
            }

            //Listens to selectedLocation and reruns when moveToUserLocation() is called
            LaunchedEffect(mapViewModel.selectedLocation) {
                mapViewModel.selectedLocation?.let {
                    viewportState.easeTo(
                        CameraOptions.Builder()
                            .center(it)
                            .zoom(13.0)
                            .build()
                    )
                }
            }

            MapThemeEffect(useDarkMap)

            DisposableEffect(mapViewRef) {
                val mapView = mapViewRef
                if (mapView == null) {
                    onDispose { }
                } else {
                    fun updateFromCamera() {
                        if (currentMapWidth <= 0 || currentMapHeight <= 0) return
                        val bounds = mapView.mapboxMap.coordinateBoundsForCamera(
                            mapView.mapboxMap.cameraState.toCameraOptions()
                        )
                        val bbox = listOf(
                            bounds.southwest.longitude(),
                            bounds.southwest.latitude(),
                            bounds.northeast.longitude(),
                            bounds.northeast.latitude()
                        )

                        // Changed this to take in the expanded bbox
                        val expandedBBox = expandBBox(bbox)

                        mapViewModel.updateOverlay(
                            bbox = expandedBBox,
                            width = currentMapWidth,
                            height = currentMapHeight
                        )
                    }

                    val listener = OnCameraChangeListener {
                        updateFromCamera()
                    }
                    mapView.mapboxMap.addOnCameraChangeListener(listener)
                    updateFromCamera()
                    onDispose {
                        mapView.mapboxMap.removeOnCameraChangeListener(listener)
                    }
                }
            }

            // Changed LaunchedEffect so it creates bitmaps as jobs
            LaunchedEffect(overlayURL) {
                imageJob?.cancel()

                if (overlayURL == null) {
                    bitmap = null
                    return@LaunchedEffect
                }

                imageJob = launch {
                    bitmap = makeBitmap(context = context, imageURL = overlayURL)
                }
            }

            MapEffect(Pair(bitmap, useDarkMap)) { mapView ->
                val currentBitmap = bitmap ?: return@MapEffect
                val style = mapView.mapboxMap.getStyle() ?: return@MapEffect

                // changed to so that the expanded box is rendered
                val bbox = overlayBBox ?: return@MapEffect
                val corners = cornersFromBBOX(bbox)

                if (style.getLayer("overlaylayer") != null) {
                    style.removeStyleLayer("overlaylayer")
                }

                if (style.getSource("overlaysource") != null) {
                    style.removeStyleSource("overlaysource")
                }

                val source = ImageSource.Builder("overlaysource")
                    .coordinates(corners)
                    .build()

                style.addSource(source)
                style.getSourceAs<ImageSource>("overlaysource")?.updateImage(currentBitmap)


                if (style.getLayer("overlaylayer") == null) {
                    style.addLayer(rasterLayer("overlaylayer", "overlaysource") {
                        rasterOpacity(0.5)
                    })
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { mapViewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search after place in Norway") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { mapViewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Empty search"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp)
            )
            if (isSearching || searchSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    if (isSearching) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn {
                            items(searchSuggestions) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            mapViewModel.selectSuggestion(item) { point ->
                                                moveToPoint(point, viewportState)
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    item.subtitle?.let {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay type selector
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OverlayType.entries.forEach { type ->
                FilterChip(
                    selected = mapViewModel.activeOverlay == type,
                    onClick = { mapViewModel.setOverlay(type) },
                    label = { Text(type.label, fontSize = 12.sp) }
                )
            }
        }

        MapZoomControls(
            onZoomIn = { zoomMap(mapViewRef, viewportState, 1.0) },
            onZoomOut = { zoomMap(mapViewRef, viewportState, -1.0) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        )

        //Locationbutton
        SmallFloatingActionButton(
            onClick = onLocationClick,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(
                imageVector = if (locationGranted)
                    Icons.Filled.LocationOn
                else
                    Icons.Outlined.Lock,
                contentDescription = stringResource(R.string.my_position),
                tint = if (locationGranted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Naming dialog for saved location
    pendingPoint?.let { point ->
        AlertDialog(
            onDismissRequest = { pendingPoint = null },
            title = { Text("Lagre posisjon") },
            text = {
                Column {
                    Text(
                        "%.4f, %.4f".format(point.latitude(), point.longitude()),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Navn") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    favoriteViewModel.addLocation(
                        lat = point.latitude(),
                        lng = point.longitude(),
                        label = locationName.trim()
                    )
                    pendingPoint = null
                }) {
                    Text("Lagre")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPoint = null }) {
                    Text("Avbryt")
                }
            }
        )
    }
}

suspend fun makeBitmap(context: Context, imageURL: String): Bitmap?{
    val request = ImageRequest.Builder(context)
        .data(imageURL)
        .allowHardware(false)
        .build()

    val result = ImageLoader(context).execute(request)

    return if(result is SuccessResult){
        result.drawable.toBitmap()
    }
    else{
        null
    }
}

// Function that should just expand the current bbox on the map
// factor is the expanding number, 1.5 might be good
fun expandBBox(bbox: List<Double>, factor: Double = 1.5): List<Double> {
    val minLon = bbox[0]
    val minLat = bbox[1]
    val maxLon = bbox[2]
    val maxLat = bbox[3]

    // expanding the bbox
    val lonExpand = (maxLon - minLon) * factor
    val latExpand = (maxLat - minLat) * factor

    return listOf(
        minLon - lonExpand,
        minLat - latExpand,
        maxLon + lonExpand,
        maxLat + latExpand
    )
}

fun cornersFromBBOX(bbox: List<Double>): List<List<Double>>{
    val minLon = bbox[0]
    val minLat = bbox[1]
    val maxLon = bbox[2]
    val maxLat = bbox[3]

    return listOf(
        listOf(minLon, maxLat),
        listOf(maxLon, maxLat),
        listOf(maxLon, minLat),
        listOf(minLon, minLat)
    )
}

@Composable
fun MapThemeEffect(useDarkMap: Boolean){
    MapEffect(useDarkMap) { mapView ->
        mapView.mapboxMap.loadStyle(
            if(useDarkMap) "mapbox://styles/mapbox/navigation-night-v1"
            else Style.STANDARD
        )
    }
}

private fun zoomMap(mapView: MapView?,
                    viewPortState: MapViewportState,
                    zoomChange: Double) {
    val currentZoom = mapView?.mapboxMap?.cameraState?.zoom ?: 11.5

    val camera = CameraOptions.Builder().zoom(
        (currentZoom + zoomChange).coerceIn(1.0, 20.0)
    )
        .build()

    viewPortState.easeTo(camera)
}

private fun moveToPoint(
    point: Point,
    viewPortState: MapViewportState
){
    val camera = CameraOptions.Builder()
        .center(point)
        .zoom(13.5)
        .build()

    viewPortState.easeTo(camera)
}