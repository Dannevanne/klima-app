package no.uio.ifi.in2000.team45.ui.map

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.sources.updateImage
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onSettingsClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState = mapViewModel.mapUiState

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = { Icon(Icons.Filled.Place, contentDescription = "") },
                title = {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "Map",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold)
                }
            )
        },
        bottomBar = {
                BottomAppBar{
                    Row(
                        modifier = Modifier
                            .padding(2.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,

                    ) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Outlined.Settings, contentDescription = "to settings")
                        }
                        IconButton({}) {
                            Icon(Icons.Filled.Place, contentDescription = "")
                        }
                        IconButton(onClick = onStatisticClick) {
                            Icon(Icons.Outlined.Info, contentDescription = "to statistics")
                        }
                    }
                    }
            }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            DataMap(mapViewModel = mapViewModel, modifier = Modifier.fillMaxSize())
            when(uiState){
                is MapUiState.Loading -> CircularProgressIndicator()
                is MapUiState.Error -> Text("Error", color = Color.Red)
                else -> {}
            }
        }
    }
}

@Composable
fun DataMap(
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState = mapViewModel.mapUiState
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
            modifier = Modifier.fillMaxSize()
                .onSizeChanged {
                    mapWidth = it.width
                    mapHeight = it.height
                },
            mapViewportState = viewportState
        ) {
            MapEffect(Unit) { mapView ->
                mapViewRef = mapView
            }

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

            MapEffect(bitmap) { mapView ->
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