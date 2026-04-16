package no.uio.ifi.in2000.team45.ui.favorites.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.data.favorites.FavoriteLocation
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel

@Composable
fun FavoriteContent(
    stations: List<FavoriteStation>,
    locations: List<FavoriteLocation>,
    favoriteViewModel: FavoriteViewModel,
    modifier: Modifier
){
    LazyColumn(
        modifier = modifier
    ) {
        // Saved map locations section
        item {
            Text(
                text = "Lagrede kartposisjoner",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (locations.isEmpty()) {
            item {
                Text(
                    text = "Ingen lagrede posisjoner enda. Bruk lagre-knappen i kartet.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
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
            val name = location.label.ifBlank { "Kartposisjon" }
            if (status?.inFloodZone == true) {
                FloodZoneTips(name)
            }
            if (status?.inRadonZone == true) {
                RadonZoneTips(name)
            }
            if (status?.inQuickClayZone == true) {
                QuickClayZoneTips(name)
            }
            status?.avgWindSpeed?.let { speed ->
                WindZoneTips(name, speed)
            }
        }

        // Advice section (mocked)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rad og tips",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item { AdviceCard("Sjekk flomsonekart for eiendommen din.", Color(0xFFE3F2FD)) }
        item { AdviceCard("Hold deg oppdatert pa NVE sine varsler.", Color(0xFFFFF3E0)) }
        item { AdviceCard("Sjekk radonnivaer for din adresse hos NGU.", Color(0xFFE8F5E9)) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}