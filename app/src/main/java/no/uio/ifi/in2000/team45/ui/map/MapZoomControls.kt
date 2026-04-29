package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.ui.common.UnscaledFont

// This composable doesn't know about MapBox, the camera or the ViewModel
// it just sends the buttons to whoeever uses the component
@Composable
fun MapZoomControls(onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
){
    val container = MaterialTheme.colorScheme.surfaceContainerLow
    val content = MaterialTheme.colorScheme.onSurface
    UnscaledFont {
        Column(modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledIconButton(
                onClick = onZoomIn,
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
