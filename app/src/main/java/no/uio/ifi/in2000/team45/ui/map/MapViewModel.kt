package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.renderer.widget.Widget
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.common.IsoCountryCode
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.map.MapRepository
import no.uio.ifi.in2000.team45.data.map.OverlayType
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository
import kotlinx.coroutines.Job
sealed interface MapUiState {
    data class Success(
        val overlayURL: String,
        val bbox: List<Double>
    ): MapUiState
    object Error : MapUiState
    object Loading : MapUiState
}

data class SearchSuggestionUi(
    val suggestion: PlaceAutocompleteSuggestion,
    val title: String,
    val subtitle: String?
)

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
    private var lastWidth = 0
    private var lastHeight = 0

    private val placeAutocomplete = PlaceAutocomplete.create()
    private var searchJob: Job? = null
    private var selectJob: Job? = null

    var searchQuery by mutableStateOf("")
        private set

    var searchSuggestions by mutableStateOf<List<SearchSuggestionUi>>(emptyList())
        private set

    var isSearching by mutableStateOf(false)

    //User current location
    var userLocation by mutableStateOf<Point?>(null)
        private set

    //Separated from userLocation so the camera does not continuously follow the user
    var selectedLocation by mutableStateOf<Point?>(null)
        private set

    init {
        viewModelScope.launch {
            themePreferenceRepository.mapThemeFlow.collect { savedTheme ->
                mapTheme = savedTheme
            }
        }
    }

    fun updateOverlay(
        bbox: List<Double>,
        width: Int,
        height: Int
    ) {

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
        lastWidth = width
        lastHeight = height

        if (bbox.size != 4 || width <= 0 || height <= 0) return
        try {
            val url = mapRepository.getOverlayURL(
                type = activeOverlay,
                bbox = bbox,
                width = width,
                height = height
            )
            if (url != lastOverlayURL) {
                lastOverlayURL = url
                mapUiState = MapUiState.Success(url, bbox)
            }
        } catch (e: Exception) {
            mapUiState = MapUiState.Error
        }
    }

    // Helper function to decide if it should fetch
    private fun hasMovedEnough(newBBox: List<Double>): Boolean {
        val old = lastBBox ?: return true
        // moving threshold, can change but should be kept low I think
        val threshold = 0.01
        // calculates if new bbox is moved enough to trigger fetch
        return (0..3).any { i ->
            kotlin.math.abs(old[i] - newBBox[i]) > threshold
        }
    }

    fun updateMapTheme(theme: MapTheme) {
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

    fun setOverlay(type: OverlayType) {
        activeOverlay = type
        lastOverlayURL = null
        // Re-fetch immediately using the last known bbox
        val bbox = lastBBox
        if (bbox != null && lastWidth > 0 && lastHeight > 0) {
            lastBBox = null
            lastUpdateTime = 0L
            updateOverlay(bbox, lastWidth, lastHeight)
        } else {
            mapUiState = MapUiState.Loading
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query

        if (query.isBlank()) {
            searchJob?.cancel()
            searchSuggestions = emptyList()
            isSearching = false
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            isSearching = true
            try {
                val response = placeAutocomplete.suggestions(
                    query = query,
                    options = PlaceAutocompleteOptions(
                        limit = 8,
                        countries = listOf(IsoCountryCode.NORWAY)
                    )
                )

                response.onValue { suggestions ->
                    if (searchQuery != query) return@onValue

                    searchSuggestions = suggestions.map { suggestion ->
                        SearchSuggestionUi(
                            suggestion = suggestion,
                            title = suggestion.name,
                            subtitle = suggestion.formattedAddress
                        )
                    }
                    isSearching = false
                }.onError {
                    if (searchQuery == query) {
                        searchSuggestions = emptyList()
                        isSearching = false
                    }
                }
            } catch (_: Exception) {
                if (searchQuery == query) {
                    searchSuggestions = emptyList()
                    isSearching = false
                }
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        searchQuery = ""
        searchSuggestions = emptyList()
        isSearching = false
    }

    fun cancelMapScreenWork() {
        searchJob?.cancel()
        selectJob?.cancel()
        searchSuggestions = emptyList()
        isSearching = false
    }

    fun selectSuggestion(
        item: SearchSuggestionUi,
        onResult: (Point) -> Unit
    ) {
        selectJob?.cancel()
        selectJob = viewModelScope.launch {
            try {
                val selectionResponse = placeAutocomplete.select(item.suggestion)

                selectionResponse.onValue { result ->
                    clearSearch()
                    onResult(result.coordinate)
                }.onError {}
            } catch (_: Exception) {
            }
        }
    }

    override fun onCleared() {
        searchJob?.cancel()
        selectJob?.cancel()
        super.onCleared()
    }
}

fun useDarkThemeFrom(theme: MapTheme, systemIsDark: Boolean): Boolean{
    return when(theme) {
        MapTheme.DEVICE -> systemIsDark
        MapTheme.DARK -> true
        MapTheme.LIGHT -> false
    }
}