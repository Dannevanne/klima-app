package no.uio.ifi.in2000.team45.data.kart

import android.util.Log
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import no.uio.ifi.in2000.team45.network.KtorClient

class KartDatasource {

    suspend fun getKart(): String {
        try {
            val httpResponse = KtorClient.client.get(
                "https://nve.geodataonline.no/arcgis/rest/services/Flomsoner2/MapServer" +
                        "/export?bbox=10,59,11,60&bboxSR=4326&size=800,600&format=png&f=image"
            )
            val body = httpResponse.bodyAsText()

            if (httpResponse.status.value != 200) {
                Log.e("Kart", "HTTP error ${httpResponse.status.value}: $body")
                throw Exception("Failed with HTTP ${httpResponse.status.value}")
            }

            println(body)
            return body
        } catch (e: Exception) {
            Log.e("Kart", "Couldn't get map", e)
            throw e
        }
    }
}