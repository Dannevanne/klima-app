package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import no.uio.ifi.in2000.team45.network.KtorClient

class MapDatasource {

    suspend fun getOverlay(): String {
        try {
            val url = ""
            val httpResponse = KtorClient.client.get(url)
            val body: String = httpResponse.body()

            if (httpResponse.status.value != 200) {
                Log.e("Map", "HTTP error ${httpResponse.status.value}: $body")
                throw Exception("Failed with HTTP ${httpResponse.status.value}")
            }

            println(body)
            return body
        } catch (e: Exception) {
            Log.e("Map", "Couldn't get map", e)
            throw e
        }
    }
}