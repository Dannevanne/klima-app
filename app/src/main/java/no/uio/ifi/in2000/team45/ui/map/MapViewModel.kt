package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.renderer.widget.Widget
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.map.MapDatasource
import no.uio.ifi.in2000.team45.data.map.MapRepository
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository
import no.uio.ifi.in2000.team45.ui.map.MapTheme
import okhttp3.internal.http2.Header

sealed interface MapUiState {
    data class Success(val overlayURL: String): MapUiState
    object Error : MapUiState
    object Loading : MapUiState
}

class MapViewModel(
    private val mapRepository: MapRepository = MapRepository(),
    private val themePreferenceRepository: ThemePreferenceRepository)
    : ViewModel() {
    var mapUiState: MapUiState by mutableStateOf(MapUiState.Loading)
        private set

    var mapTheme by mutableStateOf(MapTheme.DEVICE)
        private set
    private var lastOverlayURL: String? = null

    //User current location
    var userLocation by mutableStateOf<Point?>(null)
        private set

    //Separated from userLocation so the camera does not continuously follow the user
    var selectedLocation by mutableStateOf<Point?>(null)
        private set

    init{
        viewModelScope.launch {
            themePreferenceRepository.mapThemeFlow.collect { savedTheme ->
                mapTheme = savedTheme
            }
        }
    }

    fun updateOverlay(bbox: List<Double>,
                      width: Int,
                      height: Int){
        if(bbox.size != 4 || width <= 0 || height <= 0) return
        try{
            val url = mapRepository.getOverlayURL(bbox = bbox,
                width = width,
                height = height)
            if(url != lastOverlayURL){
                lastOverlayURL = url
                mapUiState = MapUiState.Success(url)
            }
        }
        catch(e: Exception){
            mapUiState = MapUiState.Error
        }
    }

    fun updateMapTheme(theme: MapTheme){
        mapTheme = theme
        viewModelScope.launch {
            themePreferenceRepository.saveMapTheme(theme)
        }
    }

    //Is called by Mapbox's locationlistener in DataMap
    //Updates the location without moving the camera
    fun updateUserLocation(point: Point) {
        userLocation = point
    }

    //Copies userLocation to selectedLocation which trigger the camera to move
    fun moveToUserLocation() {
        userLocation?.let { selectedLocation = it }
    }
}

fun useDarkThemeFrom(theme: MapTheme, systemIsDark: Boolean): Boolean{
    return when(theme) {
        MapTheme.DEVICE -> systemIsDark
        MapTheme.DARK -> true
        MapTheme.LIGHT -> false
    }
}