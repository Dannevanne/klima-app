package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.maps.renderer.widget.Widget
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.map.MapRepository
import no.uio.ifi.in2000.team45.data.map.OverlayType
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository

sealed interface MapUiState {
    data class Success(
        val overlayURL: String,
        val bbox: List<Double>
    ): MapUiState
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
    var activeOverlay by mutableStateOf(OverlayType.FLOOD)
        private set
    private var lastOverlayURL: String? = null
    private var lastUpdateTime = 0L
    private var throttleMs = 300L
    private var lastBBox: List<Double>? = null

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

        // Throttling, so it updates every X ms, not constantly
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < throttleMs) {
            return
        }
        lastUpdateTime = now

        // Check if user has moved the camera enough to trigger new fetch
        if (!hasMovedEnough(bbox)) {
            return
        }
        lastBBox = bbox

        if(bbox.size != 4 || width <= 0 || height <= 0) return
        try{
            val url = mapRepository.getOverlayURL(
                type = activeOverlay,
                bbox = bbox,
                width = width,
                height = height)
            if(url != lastOverlayURL){
                lastOverlayURL = url
                mapUiState = MapUiState.Success(url, bbox)
            }
        }
        catch(e: Exception){
            mapUiState = MapUiState.Error
        }
    }

    // Helper function to decide if it should fetch
    private fun hasMovedEnough(newBBox: List<Double>): Boolean {
        val old = lastBBox ?: return true
        // moving threshold, can change but should be kept low I think
        val threshold = 0.01
        // calculates if new bbox is moved enough to trigger fetch
        return (0..3).any {
            i -> kotlin.math.abs(old[i] - newBBox[i]) > threshold
        }
    }

    fun updateMapTheme(theme: MapTheme){
        mapTheme = theme
        viewModelScope.launch {
            themePreferenceRepository.saveMapTheme(theme)
        }
    }

    fun setOverlay(type: OverlayType) {
        activeOverlay = type
        // Reset so next camera update triggers a fetch with the new overlay type
        lastOverlayURL = null
        lastBBox = null
        mapUiState = MapUiState.Loading
    }
}

fun useDarkThemeFrom(theme: MapTheme, systemIsDark: Boolean): Boolean{
    return when(theme) {
        MapTheme.DEVICE -> systemIsDark
        MapTheme.DARK -> true
        MapTheme.LIGHT -> false
    }
}