package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.data.map.OverlayType
import androidx.compose.ui.Modifier

// Overlay type selector.
// Selected overlay gets sent to the ViewModel.
// ViewModel is responsible for triggering the right
// new overlay-fetching from the repository.
@Composable
fun MapOverlaySelector(
    activeOverlay: OverlayType,
    onOverlaySelected: (OverlayType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OverlayType.entries.forEach { type ->
            FilterChip(
                selected = activeOverlay == type,
                onClick = { onOverlaySelected(type) },
                label = { Text(type.label, fontSize = 12.sp) }
            )
        }
    }
}