package no.uio.ifi.in2000.team45.ui.map.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads an image from a URL and converts it to a Bitmap.
 * Used for rendering raster overlays on the map.
 *
 * @param context Android context used by the image loader.
 * @param imageURL URL of the image to load.
 * @return Decoded Bitmap or null if loading fails.
 */
suspend fun makeBitmap(context: Context, imageURL: String): Bitmap? = withContext(Dispatchers.IO) {
    val request = ImageRequest.Builder(context)
        .data(imageURL)
        .allowHardware(false)
        .build()

    val result = context.imageLoader.execute(request)

    if(result is SuccessResult){
        result.drawable.toBitmap()
    }
    else{
        null
    }
}