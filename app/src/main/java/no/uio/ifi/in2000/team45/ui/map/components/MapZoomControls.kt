package no.uio.ifi.in2000.team45.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.UnscaledFont

/**
 * UI controls for zooming in and out on the map. This composable do not depend on Mapbox,
 * camera state, or ViewModel logic. It simply exposes zoom in/out actions via callbacks.
 *
 * @param onZoomIn Callback invoked when the zoom-in button is pressed.
 * @param onZoomOut Callback invoked when the zoom-out button is pressed.
 * @param modifier Optional modifier for layout and styling.
 */
@Composable
fun MapZoomControls(onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
){
    val container = MaterialTheme.colorScheme.surfaceContainerLow
    val content = MaterialTheme.colorScheme.onSurface
    val zoomInLabel = stringResource(R.string.zoom_in)
    val zoomOutLabel = stringResource(R.string.zoom_out)
    UnscaledFont {
        Column(modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledIconButton(
                onClick = onZoomIn,
                modifier = Modifier.semantics { contentDescription = zoomInLabel },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = container,
                    contentColor = content
                )
            ) {
                Text(text = "+",
                    fontSize = 20.sp)
            }
            FilledIconButton(
                onClick = onZoomOut,
                modifier = Modifier.semantics { contentDescription = zoomOutLabel },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = container,
                    contentColor = content
                )
            ) {
                Text(text = "-",
                    fontSize = 20.sp)
            }
        }
    }
}
