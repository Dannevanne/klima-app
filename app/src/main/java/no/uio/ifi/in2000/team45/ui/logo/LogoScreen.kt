package no.uio.ifi.in2000.team45.ui.logo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import no.uio.ifi.in2000.team45.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team45.ui.theme.loadingColor

/**
 * Logo screen displaying the app logo and a loading indicator.
 * Right now just used for the aesthetics and look of the app
 *
 * @param modifier Optional modifier for layout.
 */
@Composable
fun LogoScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.safeproperty_logo_v6),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(350.dp)
            )
            CircularProgressIndicator(
                color = loadingColor()
            )
        }
    }
}