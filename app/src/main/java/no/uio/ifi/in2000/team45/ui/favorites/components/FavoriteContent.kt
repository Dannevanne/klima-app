package no.uio.ifi.in2000.team45.ui.favorites.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.favorites.FavoriteLocation
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel

@Composable
fun FavoriteContent(
    stations: List<FavoriteStation>,
    locations: List<FavoriteLocation>,
    favoriteViewModel: FavoriteViewModel,
    onMapClick: () -> Unit,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier
) {
    if (locations.isEmpty() && stations.isEmpty()) {
        EmptyFavoritesContent(
            onMapClick = onMapClick,
            topInset = topInset,
            bottomInset = bottomInset,
            modifier = modifier
        )
    } else {
        PopulatedFavoritesContent(
            locations = locations,
            favoriteViewModel = favoriteViewModel,
            topInset = topInset,
            bottomInset = bottomInset,
            modifier = modifier
        )
    }
}

@Composable
private fun FavoritesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_your_locations)) },
        trailingIcon = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmptyFavoritesContent(
    onMapClick: () -> Unit,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = topInset, bottom = bottomInset)
            .padding(16.dp)
    ) {
        FavoritesSearchBar(query = "", onQueryChange = {})
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.Place,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_saved_places_yet),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_saved_places_hint),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onMapClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.open_map))
            }
        }
        }
    }
}

@Composable
private fun PopulatedFavoritesContent(
    locations: List<FavoriteLocation>,
    favoriteViewModel: FavoriteViewModel,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) locations
    else locations.filter { it.label.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = topInset + 16.dp,
            bottom = bottomInset + 16.dp,
            start = 16.dp,
            end = 16.dp
        )
    ) {
        item {
            FavoritesSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(filtered) { location ->
            LaunchedEffect(location) {
                favoriteViewModel.fetchStatusForLocation(location)
            }
            val key = "${location.latitude},${location.longitude}"
            val status = favoriteViewModel.locationStatuses[key]
            FavoriteLocationCard(
                location = location,
                status = status,
                onRemove = { favoriteViewModel.removeLocation(location) }
            )
        }
    }
}