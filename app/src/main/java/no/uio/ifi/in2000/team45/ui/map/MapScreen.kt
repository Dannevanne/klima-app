package no.uio.ifi.in2000.team45.ui.map

import android.graphics.Bitmap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.toCameraOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmap
import coil.request.SuccessResult
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.ImageSource
import com.mapbox.maps.extension.style.sources.getSource
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.sources.updateImage
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.map.OverlayType
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel
import no.uio.ifi.in2000.team45.ui.logo.LogoScreen
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SmallFloatingActionButton

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

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){

        MapScreen(
            mapViewModel = mapViewModel,
            onSettingsClick = onSettingsClick,
            onStatisticClick = onStatisticClick,
            onFavoritesClick = onFavoritesClick,
            favoriteViewModel = favoriteViewModel,
            modifier = modifier)
        when(uiState){
            is MapUiState.Loading -> CircularProgressIndicator()
            is MapUiState.Error -> Text("Error", color = Color.Red)
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
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
                            Icon(Icons.Filled.Favorite,
                                contentDescription = "Favoritter")
                        }
                    }
                    }
            }
    ) { innerPadding ->
        DataMap(
            mapViewModel = mapViewModel,
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

    // saving the bitmap creating as a job, so it is cancellable
    // but this "DataMap" should have its own viewmodel for all of this state hosting
    var imageJob by remember {mutableStateOf<Job?>(null)}

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

                imageJob = launch {
                    bitmap = overlayURL?.let {
                        makeBitmap(context = context, imageURL = it)
                    }
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

        // Save current map position
        SmallFloatingActionButton(
            onClick = {
                val center = mapViewRef?.mapboxMap?.cameraState?.center
                if (center != null) {
                    favoriteViewModel.addLocation(
                        lat = center.latitude(),
                        lng = center.longitude(),
                        label = ""
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                Icons.Filled.FavoriteBorder,
                contentDescription = "Lagre posisjon"
            )
        }

        MapZoomControls(
            onZoomIn = { zoomMap(mapViewRef, viewportState, 1.0) },
            onZoomOut = { zoomMap(mapViewRef, viewportState, -1.0) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
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