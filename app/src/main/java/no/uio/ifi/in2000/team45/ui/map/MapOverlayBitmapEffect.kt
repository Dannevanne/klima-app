package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.ui.map.util.makeBitmap
import android.content.Context
import kotlinx.coroutines.Job
import android.graphics.Bitmap

@Composable
fun MapOverlayBitmapEffect(
    overlayURL: String?,
    context: Context,
    imageJob: Job?,
    onImageJobChange: (Job?) -> Unit,
    onBitmapLoaded: (Bitmap?) -> Unit
) {
    // When the ViewModel has a new overlayURL, the photo is downloaded
    // to Bitmap. This is because image source + raster layer requires
    // a Bitmap. This Bitmap can later be added as a raster layer on the map.
    LaunchedEffect(overlayURL) {
        imageJob?.cancel()

        if (overlayURL == null) {
            onBitmapLoaded(null)
            return@LaunchedEffect
        }

        val newJob = launch {
            onBitmapLoaded(makeBitmap(context = context, imageURL = overlayURL))
        }

        onImageJobChange(newJob)
    }
}