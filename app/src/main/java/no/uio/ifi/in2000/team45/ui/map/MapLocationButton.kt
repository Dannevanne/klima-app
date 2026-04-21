package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.uio.ifi.in2000.team45.R

// Locationbutton (only UI).
// What happens on click is decided based on callbacks.
@Composable
fun MapLocationButton(
    locationGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = MaterialTheme.colorScheme.surfaceContainerLow
    val content = MaterialTheme.colorScheme.onSurface
    SmallFloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier,
        containerColor = container,
        contentColor = content
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
