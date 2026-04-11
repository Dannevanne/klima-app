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
sealed interface MapUiState {
    data class Success(
        val overlayURL: String,
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
    private val themePreferenceRepository: ThemePreferenceRepository)
    : ViewModel() {
    var mapUiState: MapUiState by mutableStateOf(MapUiState.Loading)
        private set

    var mapTheme by mutableStateOf(MapTheme.DEVICE)
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

    // On creation of the ViewModel, it will connect to the saved theme
    // from the theme repository.
    // By doing this, UI only needs to read the theme from the ViewModel.
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
        lastWidth = width
        lastHeight = height

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
        } // The theme gets updated instantly and also
          // saved in the theme repository
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

    // Main function for search in searchbar.
    // Text from the UI is saved here and is used to get suggestions.
    fun updateSearchQuery(query: String){
        searchQuery = query // As soon as the user has written something its saved
                            // in ViewModel so that the input field shows the right text.

        // Hanldes empty search. Especially important for when user has written
        // something previously, and now removes it.
        // Now it won't show the previous suggestions.
        if(query.isBlank()){
            searchSuggestions = emptyList()
            isSearching = false
            return
        }

        // .suggestions() is an asynchronous function so a coroutine is needed
        viewModelScope.launch {
            isSearching = true

            // API call, only results from Norway
            val response = placeAutocomplete.suggestions(
                query = query,
                options = PlaceAutocompleteOptions(
                    limit = 8,
                    countries = listOf(IsoCountryCode.NORWAY)
                )
            )

            // Function for when the request succeeds.
            // "suggestions" is the list that Mapbox returns.
            // Updates the mutableStateOf variable searchSuggestions with
            // SearchSuggestionUi objects, where the suggestion object is saved in suggestion
            response.onValue { suggestions ->
                searchSuggestions = suggestions.map { suggestion ->
                    SearchSuggestionUi(
                        suggestion = suggestion,
                        title = suggestion.name,
                        subtitle = suggestion.formattedAddress
                    )
                }
                isSearching = false
            }.onError{ // If request doesn't succeed
                searchSuggestions = emptyList()
                isSearching = false
            }
        }
    }

    // Function to clear search for when user clicks X or selects result
    fun clearSearch(){
        searchQuery = ""
        searchSuggestions = emptyList()
        isSearching = false
    }

    // Second main part of search logic, for when user selects a suggested place.
    // .select() returns more detailed information about the selected place
    // The ViewModel finds the coordinates and sends it back to the UI
    fun selectSuggestion(
        item: SearchSuggestionUi,
        onResult: (Point) -> Unit
    ){
        viewModelScope.launch {
            val selectionResponse = placeAutocomplete.select(item.suggestion)

            selectionResponse.onValue { result ->
                clearSearch()
                onResult(result.coordinate)
            }.onError{}
        }
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