package no.uio.ifi.in2000.team45.ui.common

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
import no.uio.ifi.in2000.team45.R

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
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        DropdownMenuItem(
            onClick = onSettingsClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.settings)) }
        )
        DropdownMenuItem(
            onClick = onInfoClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.info)) }
        )
        DropdownMenuItem(
            onClick = onHelpClick,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_help),
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.help)) }
        )
    }
}
