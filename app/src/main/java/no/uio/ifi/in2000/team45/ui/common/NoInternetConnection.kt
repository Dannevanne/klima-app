package no.uio.ifi.in2000.team45.ui.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun IsOnline(): Boolean {

    // Bring Android-context to get ConnectivityManager later
    val context = LocalContext.current

    var isOnline by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {

        // Retrieves ConnectivityManager from the Android-system
        // so we are able to see internettinformation
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        // A callback when internettconnection changes
        val callback = object : ConnectivityManager.NetworkCallback() {

            // Sets isOnline to true when internett is available
            override fun onAvailable(network: Network) {
                isOnline = true
            }

            // Sets isOnline to false when internett not available
            override fun onLost(network: Network) {
                isOnline = false
            }
        }
        // Register callback on standard internettconnection
        // Sends a message to the callback when changes
        connectivityManager.registerDefaultNetworkCallback(callback)

        // Removes the callback when not
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    return isOnline
}