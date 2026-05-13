package no.uio.ifi.in2000.team45.ui.common.models

import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion

/**
 * UI model representing a place search suggestion.
 *
 * @property suggestion The Mapbox autocomplete suggestion.
 * @property title Primary text for the suggestion.
 * @property subtitle Secondary text meant for area/area-code.
 */
data class SearchSuggestionUi(
    val suggestion: PlaceAutocompleteSuggestion,
    val title: String,
    val subtitle: String?
)