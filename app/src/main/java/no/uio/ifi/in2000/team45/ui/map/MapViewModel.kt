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
import no.uio.ifi.in2000.team45.data.settings.LanguagePreferenceRepository
import no.uio.ifi.in2000.team45.data.settings.LanguageOption

sealed interface MapUiState {
    data class Success(
        val overlayURL: String,
        val bbox: List<Double>
    ): MapUiState
    object Error : MapUiState
    object Loading : MapUiState
    object NoOverlay : MapUiState
}

// UI model. suggestion gives access to the original object so
// it can be sent to placeAutocomplete.select()
data class SearchSuggestionUi(
    val suggestion: PlaceAutocompleteSuggestion,
    val title: String,
    val subtitle: String?
)

class MapViewModel(
    private val mapRepository: MapRepository = MapRepository(),
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val languagePreferenceRepository: LanguagePreferenceRepository
) : ViewModel() {
    var mapUiState: MapUiState by mutableStateOf(MapUiState.Loading)
        private set

    var mapTheme by mutableStateOf(MapTheme.DEVICE)
        private set
    var selectedLanguage by mutableStateOf(LanguageOption.ENGLISH)
        private set
    // Stays false until the DataStore flow emits the stored value.
    // Used to avoid applying the placeholder language on first launch.
    var isLanguageInitialized by mutableStateOf(false)
        private set
    var activeOverlay by mutableStateOf(OverlayType.FLOOD)
        private set

    // Functions to avoid unneeded new overlay requests when the camera
    // has changed too little or too often
    private var lastOverlayURL: String? = null
    private var lastUpdateTime = 0L
    private var throttleMs = 300L
    private var lastBBox: List<Double>? = null
    private var lastWidth = 0
    private var lastHeight = 0

    // Entry point for MapBox Place Autocomplete API.
    // This object is able to get suggestions with suggestions(),
    // and get detailed results with select().
    private val placeAutocomplete = PlaceAutocomplete.create()
    private var searchJob: Job? = null
    private var selectJob: Job? = null

    var searchQuery by mutableStateOf("")
        private set

    // List of suggestions that shows under the searchbar.
    var searchSuggestions by mutableStateOf<List<SearchSuggestionUi>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)

    // Last known current location from the location listener
    var userLocation by mutableStateOf<Point?>(null)
        private set

    // Own state for the selected posistion. Separated from userLocation
    // so the camera does not continuously follow the user.
    var selectedLocation by mutableStateOf<Point?>(null)
        private set

    // Holds a map-camera-position
    var lastCameraCenter by mutableStateOf(Point.fromLngLat(11.05, 60.5))
        private set
    var lastCameraZoom by mutableStateOf(11.5)
        private set

    // On creation, connect to the saved theme from the theme repository.
    init {
        viewModelScope.launch {
            themePreferenceRepository.mapThemeFlow.collect { savedTheme ->
                mapTheme = savedTheme
            }
        }
        viewModelScope.launch {
            languagePreferenceRepository.languageFlow.collect { savedLanguage ->
                selectedLanguage = savedLanguage
                isLanguageInitialized = true
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
        } // The theme gets updated instantly and also
          // saved in the theme repository
    }

    fun updateLanguage(language: LanguageOption) {
        selectedLanguage = language
        viewModelScope.launch {
            languagePreferenceRepository.saveLanguage(language)
        }
    }

    // Is called by Mapbox's locationlistener in DataMap.
    // Updates the location without moving the camera.
    fun updateUserLocation(point: Point) {
        userLocation = point
    }

    // The UI can call this when the user click "my position".
    // By copying userLocation to selectedLocation it triggers the camera to move.
    fun moveToUserLocation() {
        userLocation?.let { selectedLocation = it }
    }

    fun setOverlay(type: OverlayType) {
        activeOverlay = type
        lastOverlayURL = null
        // When the overlay type changes, the ViewModel will try to
        // fetch a new overlay based on the previous bbox,
        // so the user doesn't need to move around on the map to see the new overlay
        val bbox = lastBBox
        if (bbox != null && lastWidth > 0 && lastHeight > 0) {
            lastBBox = null
            lastUpdateTime = 0L
            updateOverlay(bbox, lastWidth, lastHeight)
        } else {
            mapUiState = MapUiState.Loading
        }
    }

    // Main search function. Saves query text and fetches suggestions.
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
                // API call, only results from Norway
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

    // Clear search for when user clicks X or selects a result
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

    // Select a suggested place and get its coordinates
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

    // Saves map-camera
    fun saveCamera(center: Point, zoom: Double){
        lastCameraCenter = center
        lastCameraZoom = zoom
    }

    // Resets SelectedLocation
    fun clearSelectedLocation(){
        selectedLocation = null
    }
}

// Function to say whether dark theme should be used or not.
// If the user has selected to follow device settings, the boolean
// that gets returned from the function will be the same as the boolean
// which says if the device is in dark theme.
fun useDarkThemeFrom(theme: MapTheme, systemIsDark: Boolean): Boolean{
    return when(theme) {
        MapTheme.DEVICE -> systemIsDark
        MapTheme.DARK -> true
        MapTheme.LIGHT -> false
    }
}