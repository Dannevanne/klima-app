package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team45.data.map.OverlayType

@Composable
fun MapOverlaySelector(
    activeOverlay: OverlayType,
    onOverlaySelected: (OverlayType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Column {
                    OverlayType.entries.forEach { type ->
                        OverlayOptionRow(
                            type = type,
                            selected = type == activeOverlay,
                            onClick = {
                                onOverlaySelected(type)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        OverlayPillButton(
            activeOverlay = activeOverlay,
            expanded = expanded,
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun OverlayPillButton(
    activeOverlay: OverlayType,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(overlayEmoji(activeOverlay))
            Spacer(modifier = Modifier.width(8.dp))
            Text(activeOverlay.label)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowDown
                else Icons.Filled.KeyboardArrowUp,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun OverlayOptionRow(
    type: OverlayType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (selected) {
                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceContainer
                    else MaterialTheme.colorScheme.background
                } else MaterialTheme.colorScheme.surfaceContainerLow
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(overlayEmoji(type))
        Spacer(modifier = Modifier.width(12.dp))
        Text(type.label)
    }
}

private fun overlayEmoji(type: OverlayType): String = when (type) {
    OverlayType.FLOOD -> "\uD83C\uDF0A"
    OverlayType.QUICK_CLAY -> "\uD83E\uDEA8"
    OverlayType.RADON -> "\u2622\uFE0F"
    OverlayType.WIND -> "\uD83D\uDCA8"
    OverlayType.LANDSLIDE -> "\u26F0\uFE0F"
}
