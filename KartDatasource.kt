package no.uio.ifi.in2000.team45.data.kart

import android.util.Log
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import no.uio.ifi.in2000.team45.network.KtorClient

class KartDatasource {

    suspend fun getKart(): String{
        try {
            val httpResponse = KtorClient.client.get(
                "https://kart.nve.no/enterprise/rest/services/Flomsoner2/MapServer/16/query"
            ) {
                parameter("geometry", "10.7472,59.9089,10.7572,59.9189")
                parameter("geometryType", "esriGeometryEnvelope")
                parameter("inSR", "4326")
                parameter("spatialRel", "esriSpatialRelIntersects")
                parameter("outFields", "*")
                parameter("returnGeometry", "false")
                parameter("f", "json")
            }
            if (httpResponse.status.value != 200) {
                throw Exception("Failed with HTTP ${httpResponse.status.value}")
            }
            return httpResponse.bodyAsText()
        }
        catch (e: Exception){
            Log.e("Kart", "Couldn't get map", e)
            throw e
        }
    }
}