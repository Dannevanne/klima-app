package no.uio.ifi.in2000.team45.ui.map.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.uio.ifi.in2000.team45.R

/**
 * Map location button used to show or request access to the user's location.
 *
 * @param locationGranted Whether location permission is currently granted.
 * @param onClick Callback invoked when the button is pressed.
 * @param modifier Optional modifier for layout and styling.
 */
@Composable
fun MapLocationButton(
    locationGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = MaterialTheme.colorScheme.surfaceContainerLow
    val content = MaterialTheme.colorScheme.onSurface
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = container,
            contentColor = content
        )
    ) {
        Icon(
            imageVector = if (locationGranted) {
                Icons.Filled.LocationOn
            } else {
                Icons.Outlined.Lock
            },
            contentDescription = stringResource(R.string.my_position)
        )
    }
}
