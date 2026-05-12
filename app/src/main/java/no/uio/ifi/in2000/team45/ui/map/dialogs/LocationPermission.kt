package no.uio.ifi.in2000.team45.ui.map.dialogs

import android.Manifest
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Wrapper composable that handles runtime location permission request
 * and exposes the result to its content.
 *
 * @param content UI content that receives the current permission state.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocationPermission(
    content: @Composable (locationGranted: Boolean) -> Unit
){
    //Remember state for both permits
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    //Ask for permission the first time
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    //Gives content a boolean based on whether both permissions are granted
    content(permissionState.allPermissionsGranted)
}