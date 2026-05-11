package no.uio.ifi.in2000.team45.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.savedplaces.models.SavedPlacesLocation
import no.uio.ifi.in2000.team45.ui.theme.LoadingBlue

@Composable
fun SavedPlacePopup(
    place: SavedPlacesLocation,
    onOpenSavedPlaces: () -> Unit,
    onOpenStatistics: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // onDismiss is the contract; the actual dismissal is wired to a tap on
    // the underlying map. Kept as a parameter so the caller controls the
    // lifetime of the popup state.
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = place.label.ifBlank {
                    stringResource(R.string.map_location_placeholder)
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${"%.4f".format(place.latitude)}, ${"%.4f".format(place.longitude)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // The app theme has primary == onPrimary (both black or both
             // white), which makes default Button text invisible. Pin colors
            // so the label is readable on both light and dark.
            Button(
                onClick = onOpenSavedPlaces,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoadingBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.open_in_saved_places))
            }
            Button(
                onClick = onOpenStatistics,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoadingBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.open_in_statistics))
            }
        }
    }
}
