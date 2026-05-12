package no.uio.ifi.in2000.team45.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team45.R

/**
 * Icon button that opens an overflow menu.
 *
 * @param onClick Callback invoked when the menu button is clicked.
 */
@Composable
fun OverflowMenuButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = stringResource(R.string.menu)
        )
    }
}

/**
 * Overflow dropdown menu containing actions such as settings, info, and help.
 *
 * @param expanded Whether the menu is currently visible.
 * @param onDismissRequest Callback invoked when the menu should be dismissed.
 * @param onSettingsClick Callback invoked when the settings screen is selected.
 * @param onInfoClick Callback invoked when the info screen is selected.
 * @param onHelpClick Callback invoked when the help screen is selected.
 */
@Composable
fun OverflowMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        DropdownMenuItem(
            onClick = onSettingsClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings)
                )
            },
            text = { Text(text = stringResource(R.string.settings)) }
        )
        DropdownMenuItem(
            onClick = onInfoClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.info)
                )
            },
            text = { Text(text = stringResource(R.string.info)) }
        )
        DropdownMenuItem(
            onClick = onHelpClick,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_help),
                    contentDescription = stringResource(R.string.help)
                )
            },
            text = { Text(text = stringResource(R.string.help)) }
        )
    }
}
