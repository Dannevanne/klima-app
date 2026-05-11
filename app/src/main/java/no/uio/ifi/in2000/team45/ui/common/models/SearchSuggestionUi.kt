package no.uio.ifi.in2000.team45.ui.common.models

import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion

// Shared UI model for a Mapbox place suggestion. The raw suggestion is kept
// so the caller can feed it back into placeAutocomplete.select() to resolve
// the chosen entry to coordinates.
data class SearchSuggestionUi(
    val suggestion: PlaceAutocompleteSuggestion,
    val title: String,
    val subtitle: String?
)