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
import androidx.core.content.ContextCompat.getSystemService

@Composable
fun IsOnline(): Boolean {

    // Retrieves the Android context needed to access system services
    val context = LocalContext.current

    // Retrieves the ConnectivityManager from the Android system
    // This is the class that provides access to network information
    val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager


    var isOnline by remember {

        // Retrieves the active network at startup
        // Returns null if no network is available
        val currentNetwork = connectivityManager.activeNetwork
        mutableStateOf(currentNetwork != null) }

    DisposableEffect(Unit) {

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