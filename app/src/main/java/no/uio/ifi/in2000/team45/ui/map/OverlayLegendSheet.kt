package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.map.OverlayType

// Bottom sheet that explains the active overlay's colors/patterns. Triggered
// by the (?) button next to the overlay pill. Tap outside or swipe down to
// dismiss.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayLegendSheet(
    overlay: OverlayType,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val title = stringResource(overlay.labelRes)
    val body = when (overlay) {
        OverlayType.NONE -> stringResource(R.string.legend_none_body)
        OverlayType.FLOOD -> stringResource(R.string.legend_flood_body)
        OverlayType.QUICK_CLAY -> stringResource(R.string.legend_quickclay_body)
        OverlayType.RADON -> stringResource(R.string.legend_radon_body)
        OverlayType.LANDSLIDE -> stringResource(R.string.legend_landslide_body)
        OverlayType.WIND -> stringResource(R.string.legend_wind_body)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.overlay_legend_title, title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
