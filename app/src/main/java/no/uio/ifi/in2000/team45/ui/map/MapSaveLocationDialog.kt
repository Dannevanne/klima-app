package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel

// Naming dialog for saved location
@Composable
fun MapSaveLocationDialog(
    point: Point,
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    savedPlacesViewModel: SavedPlacesViewModel
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lagre posisjon") },
        text = {
            Column {
                Text(
                    "%.4f, %.4f".format(point.latitude(), point.longitude()),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = locationName,
                    onValueChange = onLocationNameChange,
                    label = { Text("Navn") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    savedPlacesViewModel.addLocation(
                        lat = point.latitude(),
                        lng = point.longitude(),
                        label = locationName.trim()
                    )
                    onDismiss()
                },
                enabled = locationName.trim().isNotBlank()
            ) {
                Text("Lagre")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
}
