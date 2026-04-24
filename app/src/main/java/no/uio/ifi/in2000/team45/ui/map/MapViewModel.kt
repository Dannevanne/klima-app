package no.uio.ifi.in2000.team45.ui.map

import android.content.Context
import android.graphics.Bitmap
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team45.data.settings.LanguagePreferenceRepository
import no.uio.ifi.in2000.team45.data.settings.LanguageOption
import no.uio.ifi.in2000.team45.ui.map.util.expandBBox
import no.uio.ifi.in2000.team45.ui.map.util.makeBitmap
import kotlin.math.abs

sealed interface MapUiState {
    data class Success(
        val bitmap: Bitmap,
        val bbox: List<Double>
    ): MapUiState
    object Error : MapUiState
    object Loading : MapUiState
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
    private var throttleMs = 500L
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

    private var fetchOverlayJob: Job? = null

    private var lastExpandedBBox: List<Double>? = null
    private var lastExpandedBitmap: Bitmap? = null
    private var lastZoom: Double? = null

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
        height: Int,
        context: Context
    ) {
        val currentZoom = lastCameraZoom

        // Throttling, so it updates every X ms, not constantly
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < throttleMs) {
            return
        }
        lastUpdateTime = now
        if (lastBBox == bbox) {
            return
        }

        lastBBox = bbox
        lastWidth = width
        lastHeight = height

        if (bbox.size != 4 || width <= 0 || height <= 0) return
        fetchOverlayJob?.cancel()
        fetchOverlayJob = viewModelScope.launch {
            try {
                val factor = 1.5

                // No point in getting new overlay if we are in the expanded
                if (!isBBoxContained(inner = bbox, outer = lastExpandedBBox)) {
                    val multiplier = 1.0 + (2.0 * factor)

                    val url = mapRepository.getOverlayURL(
                        type = activeOverlay,
                        bbox = bbox,
                        width = (width / multiplier).toInt(),
                        height = (height / multiplier).toInt()
                    )
                    val overlayBitmap = makeBitmap(context, url) ?: return@launch

                    mapUiState = MapUiState.Success(overlayBitmap, bbox)
                } else {
                    if (mapUiState !is MapUiState.Success || (mapUiState as? MapUiState.Success)?.bitmap != lastExpandedBitmap) {
                        mapUiState = MapUiState.Success(lastExpandedBitmap!!, lastExpandedBBox!!)
                    }
                }

                val expandedBBox = expandBBox(bbox, factor)

                val expandedUrl = mapRepository.getOverlayURL(
                    type = activeOverlay,
                    bbox = expandedBBox,
                    width = width,
                    height = height
                )
                val expandedBitmap = makeBitmap(context, expandedUrl)
                if (expandedBitmap != null) {
                    mapUiState = MapUiState.Success(expandedBitmap, expandedBBox)
                    lastExpandedBBox = expandedBBox
                    lastExpandedBitmap = expandedBitmap
                }

            } catch (e: Exception) {} finally {
                lastZoom = currentZoom
            }
        }

    }

    private fun isBBoxContained(inner: List<Double>, outer: List<Double>?): Boolean {
        if (outer == null || lastExpandedBitmap == null) return false

        val threshold = 0.05
        lastZoom?.let {
            if (abs(lastCameraZoom - it) > threshold) {
                return false
            }
        }

        return inner[0] >= outer[0] && // Southwest Lon
                inner[1] >= outer[1] && // Southwest Lat
                inner[2] <= outer[2] && // Northeast Lon
                inner[3] <= outer[3]    // Northeast Lat
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

    fun setOverlay(type: OverlayType, context: Context) {
        activeOverlay = type
        lastOverlayURL = null
        // When the overlay type changes, the ViewModel will try to
        // fetch a new overlay based on the previous bbox,
        // so the user doesn't need to move around on the map to see the new overlay
        val bbox = lastBBox
        if (bbox != null && lastWidth > 0 && lastHeight > 0) {
            lastBBox = null
            lastUpdateTime = 0L
            lastExpandedBBox = null
            lastExpandedBitmap = null
            updateOverlay(bbox, lastWidth, lastHeight, context)
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

        lastBBox = null
        lastExpandedBBox = null
        lastExpandedBitmap = null
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