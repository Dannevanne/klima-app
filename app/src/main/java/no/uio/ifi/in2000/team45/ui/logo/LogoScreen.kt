package no.uio.ifi.in2000.team45.ui.logo

import androidx.compose.foundation.Image
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R

@Composable
fun LogoScreen(modifier: Modifier = Modifier) {
    Image(painter = painterResource(R.drawable.tryggeiendom_sort),
        contentDescription = stringResource(R.string.trygg_eiendom)
    )
}

@Preview(showBackground = true)
@Composable
fun LogoScreenPreview() {
    Surface{
        LogoScreen()
    }
}