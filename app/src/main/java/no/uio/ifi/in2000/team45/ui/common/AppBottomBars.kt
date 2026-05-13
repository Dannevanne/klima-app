package no.uio.ifi.in2000.team45.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp


/**
 * Data model representing an item in the application's bottom navigation bar.
 *
 * @property label Text label displayed under the icon.
 * @property onClick Callback invoked when the item is clicked.
 * @property icon Composable function that renders the item's icon.
 */
data class BottomBarItemData(
    val label: String,
    val onClick: () -> Unit,
    val icon: @Composable () -> Unit
)

/**
 * Bottom navigation bar used for primary app navigation.
 *
 * @param items List of navigation items to display in the bottom bar.
 * @param modifier Optional modifier for styling or layout adjustments.
 */
@Composable
fun AppBottomBar(
    items: List<BottomBarItemData>,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                NavBarItem(
                    onClick = item.onClick,
                    icon = item.icon,
                    label = item.label,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}