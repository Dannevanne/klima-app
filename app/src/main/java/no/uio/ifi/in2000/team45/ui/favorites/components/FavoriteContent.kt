package no.uio.ifi.in2000.team45.ui.favorites.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
    modifier: Modifier
) {
    if (locations.isEmpty() && stations.isEmpty()) {
        EmptyFavoritesContent(onMapClick = onMapClick, modifier = modifier)
    } else {
        PopulatedFavoritesContent(
            locations = locations,
            favoriteViewModel = favoriteViewModel,
            modifier = modifier
        )
    }
}

@Composable
private fun FavoritesSearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text(stringResource(R.string.place_or_station)) },
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
        readOnly = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmptyFavoritesContent(
    onMapClick: () -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        FavoritesSearchBar()
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
                text = "No saved places yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Go to the map, tap an area and save it to see risk analysis and climate data here.",
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
                Text("Open map")
            }
        }
        }
    }
}

@Composable
private fun PopulatedFavoritesContent(
    locations: List<FavoriteLocation>,
    favoriteViewModel: FavoriteViewModel,
    modifier: Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            FavoritesSearchBar()
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(locations) { location ->
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
        item {
            WhatCanIDoSection()
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}