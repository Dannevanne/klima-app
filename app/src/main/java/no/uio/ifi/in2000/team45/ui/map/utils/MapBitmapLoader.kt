package no.uio.ifi.in2000.team45.ui.map.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Makes Bitmap from the URL from the repository / datasource
// so it can be shown as an overlay on the map.
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