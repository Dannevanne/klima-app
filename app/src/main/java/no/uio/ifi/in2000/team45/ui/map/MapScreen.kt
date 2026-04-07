package no.uio.ifi.in2000.team45.ui.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.ImageSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.sources.updateImage
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.toCameraOptions
import no.uio.ifi.in2000.team45.R

@Composable
fun MapStateScreen(
    mapViewModel: MapViewModel,
    onSettingsClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = mapViewModel.mapUiState
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    //Shows module asking for permission in settings when user clicks the locationbutton without permission
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.locationsharing_required)) },
            text = { Text(stringResource(R.string.need_location_settings)) },
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
                    }
                    }
            }
    ) { innerPadding ->
        DataMap(
            mapViewModel = mapViewModel,
            locationGranted = locationGranted,
            onLocationClick = onLocationClick,
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState = mapViewModel.mapUiState
    val useDarkMap = useDarkThemeFrom(
        theme = mapViewModel.mapTheme,
        systemIsDark = isSystemInDarkTheme()
    )
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
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

    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    mapWidth = it.width
                    mapHeight = it.height
                },
            mapViewportState = viewportState
        ) {
            MapEffect(Unit) { mapView ->
                mapViewRef = mapView
            }

            //Activates the blue location-dot
            //Rerun every time locationGranted changes
            MapEffect(locationGranted) { mapView ->
                mapView.location.apply {

                    //On/Off based on permission
                    enabled = locationGranted

                    //Show pulsing animation around the blue dot
                    pulsingEnabled = true
                }
                //Listens to locationchanges from Mapbox and updates userLocation in viewmodel
                if (locationGranted) {
                    mapView.location.addOnIndicatorPositionChangedListener { point ->
                        mapViewModel.updateUserLocation(point)
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
                        mapViewModel.updateOverlay(
                            bbox = bbox,
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

            LaunchedEffect(overlayURL) {
                bitmap = if (overlayURL != null) {
                    makeBitmap(context = context, imageURL = overlayURL)
                } else {
                    null
                }
            }

            MapEffect(Pair(bitmap, useDarkMap)) { mapView ->
                val currentBitmap = bitmap ?: return@MapEffect
                val style = mapView.mapboxMap.getStyle() ?: return@MapEffect
                val bounds = mapView.mapboxMap.coordinateBoundsForCamera(
                    mapView.mapboxMap.cameraState.toCameraOptions()
                )
                val bbox = listOf(
                    bounds.southwest.longitude(),
                    bounds.southwest.latitude(),
                    bounds.northeast.longitude(),
                    bounds.northeast.latitude()
                )
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