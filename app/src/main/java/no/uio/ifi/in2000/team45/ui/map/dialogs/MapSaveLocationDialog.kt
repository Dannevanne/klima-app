package no.uio.ifi.in2000.team45.ui.map.dialogs

import android.content.Context
import android.media.MediaPlayer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.savedplaces.utils.NorwayLocationValidator
import no.uio.ifi.in2000.team45.ui.common.AnimatedConfirmDialog
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel

/**
 * Dialog flow for saving a map location.
 *
 * @param point Selected map coordinate to save, or null if no selection exists.
 * @param locationName Current text input for the place name.
 * @param onLocationNameChange Callback for updating the name input state.
 * @param onDismiss Callback invoked when any dialog is dismissed or completed.
 * @param savedPlacesViewModel ViewModel responsible for storing the saved locations.
 */
@Composable
fun MapSaveLocationDialog(
    point: Point?,
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    savedPlacesViewModel: SavedPlacesViewModel
) {
    val limitReached = !savedPlacesViewModel.canAddLocation()
    val context = LocalContext.current
    val outsideNorway = point?.let {
        !NorwayLocationValidator.isInsideNorway(it.latitude(), it.longitude())
    } == true

    val soundEnabled = context
        .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getBoolean("sound_enabled", true)

    AnimatedConfirmDialog(
        visible = point != null && outsideNorway,
        onDismissed = onDismiss,
        title = stringResource(R.string.save_location),
        text = stringResource(R.string.you_can_not_save_a_place_outside_of_norway),
        confirmText = stringResource(R.string.ok)
    )

    AnimatedConfirmDialog(
        visible = point != null && !outsideNorway && limitReached,
        onDismissed = onDismiss,
        title = stringResource(R.string.save_location),
        text = stringResource(
            R.string.max_saved_places_message,
            SavedPlacesViewModel.MAX_SAVED_PLACES
        ),
        confirmText = stringResource(R.string.ok)
    )

    if (point != null && !outsideNorway && !limitReached) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.save_location)) },
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
                        label = { Text(text = stringResource(R.string.place_name)) },
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
                        if (soundEnabled) {
                            MediaPlayer.create(context, R.raw.ding).apply {
                                setOnCompletionListener { it.release() }
                                start()
                            }
                        }
                        onDismiss()
                    },
                    enabled = locationName.trim().isNotBlank()
                ) {
                    Text(text = stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}
