package no.uio.ifi.in2000.team45.ui.statistic.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.team45.R

/**
 * Button that requests the user's last known location and returns it via callback.
 *
 * @param onLocationFound callback invoked with coordinated when a location is retrieved.
 * @param modifier optional modifier for layout and styling.
 */
@Composable
fun FindNearbyButton(
    onLocationFound: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val noLocMsg = stringResource(R.string.location_unavailable)

    fun fetchLastKnownLocation() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            Toast.makeText(context, noLocMsg, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            val loc = providers.asSequence()
                .mapNotNull { p ->
                    if (lm.isProviderEnabled(p)) {
                        @Suppress("MissingPermission")
                        lm.getLastKnownLocation(p)
                    } else null
                }
                .maxByOrNull { it.time }
            if (loc != null) {
                onLocationFound(loc.latitude, loc.longitude)
            } else {
                Toast.makeText(context, noLocMsg, Toast.LENGTH_SHORT).show()
            }
        } catch (_: SecurityException) {
            Toast.makeText(context, noLocMsg, Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLastKnownLocation()
        else Toast.makeText(context, noLocMsg, Toast.LENGTH_SHORT).show()
    }

    OutlinedButton(
        onClick = {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) fetchLastKnownLocation()
            else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.MyLocation, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.find_nearby))
    }
}
