package no.uio.ifi.in2000.team45.data.statistic

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import no.uio.ifi.in2000.team45.network.KtorClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

private const val TAG = "StatisticDatasource"
private const val FROST_BASE = "https://frost.met.no"
private const val MAPBOX_GEOCODE_BASE = "https://api.mapbox.com/search/geocode/v6/forward"

class StatisticDatasource {

    private val json = Json { ignoreUnknownKeys = true }

    private fun authHeader(clientId: String): String {
        val encoded = android.util.Base64.encodeToString(
            "$clientId:".toByteArray(),
            android.util.Base64.NO_WRAP
        )
        return "Basic $encoded"
    }

    // Expand query to handle Norwegian letter substitutions (o->oe, a->aa, etc.)
    private fun norwegianVariants(query: String): List<String> {
        val trimmed = query.trim()
        val variants = mutableSetOf(trimmed)

        // Map common ASCII approximations to Norwegian letters
        val substitutions = mapOf(
            "ae" to "\u00e6", "\u00e6" to "ae",
            "oe" to "\u00f8", "\u00f8" to "oe", "\u00f6" to "\u00f8",
            "aa" to "\u00e5", "\u00e5" to "aa",
            "o" to "\u00f8", "a" to "\u00e5"
        )

        for ((from, to) in substitutions) {
            val lower = trimmed.lowercase()
            if (lower.contains(from)) {
                variants.add(trimmed.replace(from, to, ignoreCase = true))
            }
        }
        return variants.toList()
    }

    suspend fun searchStations(query: String, clientId: String): List<WeatherStation> {
        val allStations = mutableListOf<WeatherStation>()
        val seenIds = mutableSetOf<String>()

        for (variant in norwegianVariants(query)) {
            val encodedName = java.net.URLEncoder.encode(variant, "UTF-8")
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
                    continue
                }

                for (station in parseStations(body)) {
                    if (seenIds.add(station.id)) {
                        allStations.add(station)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Station search error", e)
            }
        }

        return allStations
    }

    // Single batch probe of which (station, element) combinations Frost actually
    // serves and the time range for each. Lets us pick the canonical station
    // for a place (Blindern for Oslo) instead of guessing by distance and then
    // discovering after the climate fetch that the picked station has nothing.
    suspend fun getAvailableTimeSeries(
        stationIds: List<String>,
        elementIds: List<String>,
        clientId: String
    ): List<TimeSeriesAvailability> {
        if (stationIds.isEmpty() || elementIds.isEmpty()) return emptyList()
        val sources = stationIds.joinToString(",")
        val elements = elementIds.joinToString(",") {
            java.net.URLEncoder.encode(it, "UTF-8")
        }
        val url = "$FROST_BASE/observations/availableTimeSeries/v0.jsonld" +
            "?sources=$sources" +
            "&elements=$elements"
        return try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            if (response.status.value != 200) {
                Log.e(TAG, "AvailableTimeSeries failed: ${response.status.value}")
                return emptyList()
            }
            val body: String = response.body()
            parseAvailableTimeSeries(body)
        } catch (e: Exception) {
            Log.e(TAG, "AvailableTimeSeries error", e)
            emptyList()
        }
    }

    private fun parseAvailableTimeSeries(body: String): List<TimeSeriesAvailability> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyList()
        return data.mapNotNull { item ->
            val obj = item.jsonObject
            val sourceId = obj["sourceId"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val elementId = obj["elementId"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val validFrom = obj["validFrom"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val validTo = obj["validTo"]?.jsonPrimitive?.contentOrNull
            val baseStationId = sourceId.substringBefore(":")
            val fromYear = validFrom.take(4).toIntOrNull() ?: return@mapNotNull null
            val toYear = validTo?.take(4)?.toIntOrNull()
            TimeSeriesAvailability(
                stationId = baseStationId,
                elementId = elementId,
                validFromYear = fromYear,
                validToYear = toYear
            )
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
            "mean(wind_speed P1M)",
            "record_high(air_temperature P1M)",
            "record_low(air_temperature P1M)",
            "mean(surface_snow_thickness P1M)",
            "max(wind_speed_of_gust P1M)",
            "mean(cloud_area_fraction P1M)"
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

    // Mapbox Geocoding v6, restricted to Norwegian admin units. The PlaceAutocomplete
    // SDK with limit=1 ranked phonetic mismatches (Åbestad for Ibestad) above the
    // real place; v6 with the type filter resolves Ibestad correctly.
    suspend fun geocodePlaces(query: String, mapboxToken: String): List<Place> {
        val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
        val url = MAPBOX_GEOCODE_BASE +
                "?q=$encoded" +
                "&country=no" +
                "&types=place,locality,region,district" +
                "&limit=5" +
                "&language=no" +
                "&access_token=$mapboxToken"
        return try {
            val response = KtorClient.client.get(url)
            if (response.status.value != 200) {
                Log.e(TAG, "Geocode failed: ${response.status.value}")
                return emptyList()
            }
            parseGeocodeFeatures(response.body())
        } catch (e: Exception) {
            Log.e(TAG, "Geocode error", e)
            emptyList()
        }
    }

    fun parseGeocodeFeatures(body: String): List<Place> {
        val root = json.parseToJsonElement(body).jsonObject
        val features = root["features"]?.jsonArray ?: return emptyList()
        return features.mapNotNull { feature ->
            val obj = feature.jsonObject
            val props = obj["properties"]?.jsonObject ?: return@mapNotNull null
            val name = props["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val placeFormatted = props["place_formatted"]?.jsonPrimitive?.contentOrNull ?: ""
            val geom = obj["geometry"]?.jsonObject ?: return@mapNotNull null
            val coords = geom["coordinates"]?.jsonArray ?: return@mapNotNull null
            if (coords.size < 2) return@mapNotNull null
            val lon = coords[0].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
            val lat = coords[1].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
            Place(name = name, subtitle = placeFormatted, latitude = lat, longitude = lon)
        }
    }

    fun parseStations(body: String): List<WeatherStation> {
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

    // Find nearest station to given coordinates
    suspend fun findNearestStation(
        lat: Double, lon: Double, clientId: String
    ): WeatherStation? {
        return findNearestStations(lat, lon, clientId, count = 1).firstOrNull()
    }

    // Find N nearest stations to given coordinates
    suspend fun findNearestStations(
        lat: Double, lon: Double, clientId: String, count: Int = 5
    ): List<WeatherStation> {
        val geometry = java.net.URLEncoder.encode("nearest(POINT($lon $lat))", "UTF-8")
        val url = "$FROST_BASE/sources/v0.jsonld" +
                "?types=SensorSystem" +
                "&geometry=$geometry" +
                "&nearestmaxcount=$count"
        return try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            val body: String = response.body()
            if (response.status.value != 200) {
                Log.e(TAG, "Nearest stations failed: ${response.status.value}")
                return emptyList()
            }
            parseStations(body)
        } catch (e: Exception) {
            Log.e(TAG, "Nearest stations error", e)
            emptyList()
        }
    }

    // Get latest observations for a station
    suspend fun getLatestObservations(
        stationId: String, clientId: String
    ): Map<String, Double> {
        val elements = "air_temperature,wind_speed,sum(precipitation_amount PT1H)"
        val url = "$FROST_BASE/observations/v0.jsonld" +
                "?sources=$stationId" +
                "&referencetime=latest" +
                "&elements=${java.net.URLEncoder.encode(elements, "UTF-8")}"
        return try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            val body: String = response.body()
            if (response.status.value != 200) return emptyMap()

            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonArray ?: return emptyMap()
            val result = mutableMapOf<String, Double>()
            for (item in data) {
                val obs = item.jsonObject["observations"]?.jsonArray ?: continue
                for (o in obs) {
                    val oid = o.jsonObject["elementId"]?.jsonPrimitive?.content ?: continue
                    val v = o.jsonObject["value"]?.jsonPrimitive?.doubleOrNull ?: continue
                    result[oid] = v
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Latest observations error", e)
            emptyMap()
        }
    }

    suspend fun getAnnualObservations(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int
    ): List<AnnualObservation> {
        val elements = listOf(
            "mean(air_temperature P1Y)",
            "sum(precipitation_amount P1Y)"
        ).joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
        val url = "$FROST_BASE/observations/v0.jsonld" +
                "?sources=$stationId" +
                "&referencetime=$yearFrom-01-01/$yearTo-12-31" +
                "&elements=$elements" +
                "&timeresolutions=P1Y"
        return try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            val body: String = response.body()
            if (response.status.value != 200) {
                Log.e(TAG, "Annual observations failed: ${response.status.value}")
                return emptyList()
            }
            parseAnnualObservations(body)
        } catch (e: Exception) {
            Log.e(TAG, "Annual observations error", e)
            emptyList()
        }
    }

    // Frost has no P1Y aggregations for wind/snow, so we pull monthly maxes
    // and the repository folds them up to per-year maximums.
    // Returns: list of (year, elementId, monthly value) tuples.
    suspend fun getMonthlyWindAndSnow(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int
    ): List<MonthlyObservation> {
        val elements = listOf(
            "max(wind_speed_of_gust P1M)",
            "max(wind_speed P1M)",
            "max(surface_snow_thickness P1M)"
        ).joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
        val url = "$FROST_BASE/observations/v0.jsonld" +
                "?sources=$stationId" +
                "&referencetime=$yearFrom-01-01/$yearTo-12-31" +
                "&elements=$elements" +
                "&timeresolutions=P1M"
        return try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            if (response.status.value != 200) {
                Log.e(TAG, "Monthly wind/snow fetch failed: ${response.status.value}")
                return emptyList()
            }
            parseMonthlyObservationsWithYear(response.body())
        } catch (e: Exception) {
            Log.e(TAG, "Monthly wind/snow error", e)
            emptyList()
        }
    }

    // Like parseMonthlyObservations but uses the year-month (YYYYMM) packed
    // into the month field so the repository can group by year.
    private fun parseMonthlyObservationsWithYear(body: String): List<MonthlyObservation> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyList()
        val results = mutableListOf<MonthlyObservation>()
        for (item in data) {
            val obj = item.jsonObject
            val refTime = obj["referenceTime"]?.jsonPrimitive?.content ?: continue
            val year = refTime.substring(0, 4).toIntOrNull() ?: continue
            val month = refTime.substring(5, 7).toIntOrNull() ?: continue
            val packed = year * 100 + month
            val observations = obj["observations"]?.jsonArray ?: continue
            for (obs in observations) {
                val obsObj = obs.jsonObject
                val elementId = obsObj["elementId"]?.jsonPrimitive?.content ?: continue
                val value = obsObj["value"]?.jsonPrimitive?.doubleOrNull ?: continue
                results.add(MonthlyObservation(elementId, packed, value))
            }
        }
        return results
    }

    private fun parseAnnualObservations(body: String): List<AnnualObservation> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyList()

        val byYear = mutableMapOf<Int, MutableMap<String, Double>>()

        for (item in data) {
            val obj = item.jsonObject
            val refTime = obj["referenceTime"]?.jsonPrimitive?.content ?: continue
            val year = refTime.substring(0, 4).toIntOrNull() ?: continue
            val observations = obj["observations"]?.jsonArray ?: continue

            val yearMap = byYear.getOrPut(year) { mutableMapOf() }
            for (obs in observations) {
                val obsObj = obs.jsonObject
                val elementId = obsObj["elementId"]?.jsonPrimitive?.content ?: continue
                val value = obsObj["value"]?.jsonPrimitive?.doubleOrNull ?: continue
                yearMap[elementId] = value
            }
        }

        return byYear.entries.sortedBy { it.key }.map { (year, values) ->
            AnnualObservation(
                year = year,
                meanTemp = values["mean(air_temperature P1Y)"],
                totalPrecip = values["sum(precipitation_amount P1Y)"]
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
