package no.uio.ifi.in2000.team45.ui.map.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.SearchSuggestionDropdown
import no.uio.ifi.in2000.team45.ui.common.models.SearchSuggestionUi

/**
 * Search section for the map screen, including a text input field and suggestion dropdown.
 *
 * @param searchQuery Current text entered in the search field.
 * @param searchSuggestions List of search suggestions to display.
 * @param isSearching Whether a search request is currently in progress.
 * @param onSearchQueryChange Callback invoked when the search query changes.
 * @param onClearSearch Callback invoked when the clear button is pressed.
 * @param onSuggestionClick Callback invoked when a search suggestion is selected.
 * @param modifier Optional modifier for layout and styling.
 */
@Composable
fun MapSearchSection(
    searchQuery: String,
    searchSuggestions: List<SearchSuggestionUi>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSuggestionClick: (SearchSuggestionUi) -> Unit,
    modifier: Modifier = Modifier
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    if (!state.isFocused) keyboardController?.hide()
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            // Hint shown only when the field is idle and empty; once you tap it
            // there is nothing to read, matching the statistics search bar.
            placeholder = {
                if (!isFocused) {
                    Text(text = stringResource(R.string.search_after_place_norway))
                }
            },
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                } else {
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
        SearchSuggestionDropdown(
            searchSuggestions = searchSuggestions,
            isSearching = isSearching,
            onSuggestionClick = { item ->
                keyboardController?.hide()
                focusManager.clearFocus()
                onSuggestionClick(item)
            }
        )
    }
}
