package no.uio.ifi.in2000.team45.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import androidx.compose.material3.Text
import androidx.compose.material3.Switch

@Composable
fun MusicSetting(
    musicEnabled: Boolean,
    onMusicEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.background_music),
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = musicEnabled,
            onCheckedChange = onMusicEnabledChanged
        )
    }
}