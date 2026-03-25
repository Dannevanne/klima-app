package no.uio.ifi.in2000.team45.data.statistic

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import no.uio.ifi.in2000.team45.network.KtorClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

private const val TAG = "StatisticDatasource"
private const val FROST_BASE = "https://frost.met.no"

data class WeatherStation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val municipality: String = "",
    val county: String = ""
)

data class ClimateNormal(
    val elementId: String,
    val month: Int,
    val normal: Double
)

data class MonthlyObservation(
    val elementId: String,
    val month: Int,
    val value: Double
)

class StatisticDatasource {

    private val json = Json { ignoreUnknownKeys = true }

    private fun authHeader(clientId: String): String {
        val encoded = android.util.Base64.encodeToString(
            "$clientId:".toByteArray(),
            android.util.Base64.NO_WRAP
        )
        return "Basic $encoded"
    }

    suspend fun searchStations(query: String, clientId: String): List<WeatherStation> {
        val encodedName = java.net.URLEncoder.encode(query.trim(), "UTF-8")
        val url = "$FROST_BASE/sources/v0.jsonld" +
                "?types=SensorSystem" +
                "&name=$encodedName*" +
                "&country=Norge"
        try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            val body: String = response.body()

            if (response.status.value != 200) {
                Log.e(TAG, "Station search failed: ${response.status.value}")
                return emptyList()
            }

            return parseStations(body)
        } catch (e: Exception) {
            Log.e(TAG, "Station search error", e)
            return emptyList()
        }
    }

    suspend fun getClimateNormals(
        stationId: String,
        clientId: String
    ): List<ClimateNormal> {
        val elements = listOf(
            "mean(air_temperature P1M)",
            "max(air_temperature P1M)",
            "min(air_temperature P1M)",
            "sum(precipitation_amount P1M)",
            "mean(wind_speed P1M)"
        ).joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }

        val url = "$FROST_BASE/climatenormals/v0.jsonld" +
                "?sources=$stationId" +
                "&elements=$elements"
        try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            val body: String = response.body()

            if (response.status.value != 200) {
                Log.e(TAG, "Climate normals failed: ${response.status.value}")
                return emptyList()
            }

            return parseClimateNormals(body)
        } catch (e: Exception) {
            Log.e(TAG, "Climate normals error", e)
            return emptyList()
        }
    }

    suspend fun getMonthlyObservations(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int
    ): List<MonthlyObservation> {
        val elements = listOf(
            "mean(air_temperature P1M)",
            "sum(precipitation_amount P1M)"
        ).joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
        val url = "$FROST_BASE/observations/v0.jsonld" +
                "?sources=$stationId" +
                "&referencetime=$yearFrom-01-01/$yearTo-12-31" +
                "&elements=$elements" +
                "&timeresolutions=P1M"
        try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            val body: String = response.body()

            if (response.status.value != 200) {
                Log.e(TAG, "Observations failed: ${response.status.value}")
                return emptyList()
            }

            return parseMonthlyObservations(body)
        } catch (e: Exception) {
            Log.e(TAG, "Observations error", e)
            return emptyList()
        }
    }

    private fun parseStations(body: String): List<WeatherStation> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyList()

        return data.mapNotNull { item ->
            val obj = item.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val geometry = obj["geometry"]?.jsonObject ?: return@mapNotNull null
            val coords = geometry["coordinates"]?.jsonArray ?: return@mapNotNull null
            if (coords.size < 2) return@mapNotNull null

            val lon = coords[0].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
            val lat = coords[1].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
            val municipality = obj["municipality"]?.jsonPrimitive?.content ?: ""
            val county = obj["county"]?.jsonPrimitive?.content ?: ""

            WeatherStation(
                id = id,
                name = name,
                latitude = lat,
                longitude = lon,
                municipality = municipality,
                county = county
            )
        }
    }

    private fun parseClimateNormals(body: String): List<ClimateNormal> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyList()

        return data.mapNotNull { item ->
            val obj = item.jsonObject
            val elementId = obj["elementId"]?.jsonPrimitive?.content
                ?: obj["element"]?.jsonPrimitive?.content
                ?: return@mapNotNull null
            val month = obj["month"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            val normal = obj["normal"]?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null

            ClimateNormal(
                elementId = elementId,
                month = month,
                normal = normal
            )
        }
    }

    private fun parseMonthlyObservations(body: String): List<MonthlyObservation> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyList()

        val results = mutableListOf<MonthlyObservation>()

        for (item in data) {
            val obj = item.jsonObject
            val refTime = obj["referenceTime"]?.jsonPrimitive?.content ?: continue
            val month = refTime.substring(5, 7).toIntOrNull() ?: continue
            val observations = obj["observations"]?.jsonArray ?: continue

            for (obs in observations) {
                val obsObj = obs.jsonObject
                val elementId = obsObj["elementId"]?.jsonPrimitive?.content ?: continue
                val value = obsObj["value"]?.jsonPrimitive?.doubleOrNull ?: continue
                results.add(MonthlyObservation(elementId, month, value))
            }
        }

        return results
    }
}
