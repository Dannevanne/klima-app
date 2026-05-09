package no.uio.ifi.in2000.team45.ui.savedplaces.components

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.savedplaces.SavedPlacesLocation
import no.uio.ifi.in2000.team45.data.savedplaces.FavoriteStation
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel

@Composable
fun SavedPlacesContent(
    stations: List<FavoriteStation>,
    locations: List<SavedPlacesLocation>,
    savedPlacesViewModel: SavedPlacesViewModel,
    onMapClick: () -> Unit,
    onShowOnMap: (lat: Double, lon: Double) -> Unit,
    topInset: Dp,
    bottomInset: Dp,
    scrollToTopTick: Int,
    modifier: Modifier
) {
    if (locations.isEmpty()) {
        EmptySavedPlacesContent(
            onMapClick = onMapClick,
            topInset = topInset,
            bottomInset = bottomInset,
            modifier = modifier
        )
    } else {
        PopulatedSavedPlacesContent(
            locations = locations,
            savedPlacesViewModel = savedPlacesViewModel,
            onShowOnMap = onShowOnMap,
            topInset = topInset,
            bottomInset = bottomInset,
            scrollToTopTick = scrollToTopTick,
            modifier = modifier
        )
    }
}

@Composable
private fun EmptySavedPlacesContent(
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
            ) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = stringResource(R.string.no_saved_places_yet),
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
private fun PopulatedSavedPlacesContent(
    locations: List<SavedPlacesLocation>,
    savedPlacesViewModel: SavedPlacesViewModel,
    onShowOnMap: (lat: Double, lon: Double) -> Unit,
    topInset: Dp,
    bottomInset: Dp,
    scrollToTopTick: Int,
    modifier: Modifier
) {
    val listState = rememberLazyListState()
    val pendingFocus = savedPlacesViewModel.pendingFocus

    LaunchedEffect(pendingFocus, locations) {
        val target = pendingFocus ?: return@LaunchedEffect
        val idx = locations.indexOf(target)
        if (idx >= 0) {
            listState.animateScrollToItem(idx)
            savedPlacesViewModel.clearPendingFocus()
        }
    }

    // Skip the first emission so we don't animate a no-op on first composition.
    LaunchedEffect(scrollToTopTick) {
        if (scrollToTopTick > 0) listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(
            top = topInset + 16.dp,
            bottom = bottomInset + 10.dp,
            start = 16.dp,
            end = 16.dp
        )
    ) {
        items(locations) { location ->
            LaunchedEffect(location) {
                savedPlacesViewModel.fetchStatusForLocation(location)
            }
            val key = "${location.latitude},${location.longitude}"
            val status = savedPlacesViewModel.locationStatuses[key]
            SavedPlacesLocationCard(
                location = location,
                status = status,
                onRemove = { savedPlacesViewModel.removeLocation(location) },
                onShowOnMap = { onShowOnMap(location.latitude, location.longitude) }
            )
        }
    }
}
