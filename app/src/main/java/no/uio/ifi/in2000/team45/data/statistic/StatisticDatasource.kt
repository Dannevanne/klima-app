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
import no.uio.ifi.in2000.team45.data.statistic.models.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.models.ClimateNormal
import no.uio.ifi.in2000.team45.data.statistic.models.MonthlyObservation
import no.uio.ifi.in2000.team45.data.statistic.models.Place
import no.uio.ifi.in2000.team45.data.statistic.models.TimeSeriesAvailability
import no.uio.ifi.in2000.team45.data.statistic.models.WeatherStation

private const val TAG = "StatisticDatasource"
private const val FROST_BASE = "https://frost.met.no"
private const val MAPBOX_GEOCODE_BASE = "https://api.mapbox.com/search/geocode/v6/forward"

/**
 * Datasource responsible for fetching and parsing weather, climate, and geocoding
 * data from external APIs.
 */
class StatisticDatasource {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Creates a Authentication header for Frost API requests.
     *
     * @param clientId The Frost API client ID.
     * @return A formatted authentication header string.
     */
    private fun authHeader(clientId: String): String {
        val encoded = android.util.Base64.encodeToString(
            "$clientId:".toByteArray(),
            android.util.Base64.NO_WRAP
        )
        return "Basic $encoded"
    }

    /**
     * URL-encodes a string using UTF-8 encoding.
     *
     * @param s The string to encode.
     * @return The UTF-8 encoded string.
     */
    private fun encodeUtf8(s: String): String =
        java.net.URLEncoder.encode(s, Charsets.UTF_8.name())

    /**
     * Generates Norwegian language variants of a search query to improve matching
     * against station names.
     *
     * @param query The original search query.
     * @return A list of possible query variants.
     */
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

    /**
     * Searches for weather stations matching the given query.
     *
     * @param query The station search query.
     * @param clientId The Frost API client ID.
     * @return A list of matching WeatherStation objects.
     */
    suspend fun searchStations(query: String, clientId: String): List<WeatherStation> {
        val allStations = mutableListOf<WeatherStation>()
        val seenIds = mutableSetOf<String>()

        for (variant in norwegianVariants(query)) {
            val encodedName = encodeUtf8(variant)
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

    /**
     * Retrieves available time series data for given stations and elements.
     *
     * @param stationIds List of station ids.
     * @param elementIds List of climate element ids.
     * @param clientId The Frost API client ID.
     * @return A list of available time series.
     */
    suspend fun getAvailableTimeSeries(
        stationIds: List<String>,
        elementIds: List<String>,
        clientId: String
    ): List<TimeSeriesAvailability> {
        if (stationIds.isEmpty() || elementIds.isEmpty()) return emptyList()
        val sources = stationIds.joinToString(",")
        val elements = elementIds.joinToString(",") {
            encodeUtf8(it)
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

    /**
     * Parses available time series response into structured objects.
     *
     * @param body Raw Json response body.
     * @return Parsed list of TimeSeriesAvailability objects.
     */
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

    /**
     * Retrieves climate normals for a given station.
     *
     * @param stationId The station identifier.
     * @param clientId The Frost API client ID.
     * @return A list of climate normals.
     */
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
        ).joinToString(",") { encodeUtf8(it) }

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

    /**
     * Retrieves monthly climate observations for a station within a time range.
     *
     * @param stationId The station identifier.
     * @param clientId The Frost API client ID.
     * @param yearFrom Start year.
     * @param yearTo End year.
     * @return A list of monthly observations.
     */
    suspend fun getMonthlyObservations(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int
    ): List<MonthlyObservation> {
        val elements = listOf(
            "mean(air_temperature P1M)",
            "sum(precipitation_amount P1M)"
        ).joinToString(",") { encodeUtf8(it) }
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

    /**
     * Searches for places using Mapbox geocoding API.
     *
     * @param query The search query.
     * @param mapboxToken Mapbox API access token.
     * @return A list of matching Place objects.
     */
    suspend fun geocodePlaces(query: String, mapboxToken: String): List<Place> {
        val encoded = encodeUtf8(query.trim())
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

    /**
     * Parses Mapbox geocoding response into Place objects.
     *
     * @param body Raw JSON response body.
     * @return A list of parsed places.
     */
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

    /**
     * Parses Frost station response into WeatherStation objects.
     *
     * @param body Raw JSON response body.
     * @return A list of parsed weather stations.
     */
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

    /**
     * Parses climate normals response into structured climate data.
     *
     * @param body Raw JSON response body.
     * @return A list of ClimateNormal objects.
     */
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

    /**
     * Finds the nearest weather stations to a given geographic coordinate.
     *
     * @param lat Latitude coordinate.
     * @param lon Longitude coordinate.
     * @param clientId The Frost API client ID.
     * @param count Maximum number of stations to return.
     * @return A list of nearest weather stations.
     */
    suspend fun findNearestStations(
        lat: Double, lon: Double, clientId: String, count: Int = 5
    ): List<WeatherStation> {
        val geometry = encodeUtf8("nearest(POINT($lon $lat))")
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

    /**
     * Retrieves the latest available observations for a weather station.
     *
     * @param stationId The station identifier.
     * @param clientId The Frost API client ID.
     * @return A map of element IDs to their latest observed values.
     */
    suspend fun getLatestObservations(
        stationId: String, clientId: String
    ): Map<String, Double> {
        val elements = "air_temperature,wind_speed,sum(precipitation_amount PT1H)"
        val url = "$FROST_BASE/observations/v0.jsonld" +
                "?sources=$stationId" +
                "&referencetime=latest" +
                "&elements=${encodeUtf8(elements)}"
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

    /**
     * Retrieves annual aggregated weather observations for a station.
     *
     * @param stationId The station identifier.
     * @param clientId The Frost API client ID.
     * @param yearFrom Start year.
     * @param yearTo End year .
     * @return A list of annual observations.
     */
    suspend fun getAnnualObservations(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int
    ): List<AnnualObservation> {
        val elements = listOf(
            "mean(air_temperature P1Y)",
            "sum(precipitation_amount P1Y)"
        ).joinToString(",") { encodeUtf8(it) }
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

    /**
     * Retrieves monthly wind and snow observations for a station.
     *
     * @param stationId The station identifier.
     * @param clientId The Frost API client ID.
     * @param yearFrom Start year.
     * @param yearTo End year.
     * @param wantedElements Set of climate elements to fetch.
     * @return A list of monthly observations.
     */
    suspend fun getMonthlyWindAndSnow(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int,
        wantedElements: Set<String> = setOf(
            "max(wind_speed_of_gust P1M)",
            "max(surface_snow_thickness P1M)"
        )
    ): List<MonthlyObservation> {
        val toFetch = wantedElements.filter { it.contains("P1M") }
        if (toFetch.isEmpty()) return emptyList()
        val elements = toFetch.joinToString(",") { encodeUtf8(it) }
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

    /**
     * Retrieves daily precipitation values grouped by year for a station.
     *
     * @param stationId The station identifier.
     * @param clientId The Frost API client ID.
     * @param yearFrom Start year (inclusive).
     * @param yearTo End year (inclusive).
     * @return A map of year to daily precipitation values.
     */
    suspend fun getDailyPrecipitation(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int
    ): Map<Int, List<Double>> {
        val element = encodeUtf8("sum(precipitation_amount P1D)")
        val url = "$FROST_BASE/observations/v0.jsonld" +
                "?sources=$stationId" +
                "&referencetime=$yearFrom-01-01/$yearTo-12-31" +
                "&elements=$element" +
                "&timeresolutions=P1D"
        return try {
            val response = KtorClient.client.get(url) {
                header("Authorization", authHeader(clientId))
            }
            if (response.status.value != 200) {
                Log.e(TAG, "Daily precip fetch failed: ${response.status.value}")
                return emptyMap()
            }
            parseDailyPrecipitation(response.body())
        } catch (e: Exception) {
            Log.e(TAG, "Daily precip error", e)
            emptyMap()
        }
    }

    /**
     * Parses daily precipitation response into a year-grouped structure.
     *
     * @param body Raw JSON response body.
     * @return A map of year to precipitation values.
     */
    private fun parseDailyPrecipitation(body: String): Map<Int, List<Double>> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyMap()
        val byYear = mutableMapOf<Int, MutableList<Double>>()
        for (item in data) {
            val obj = item.jsonObject
            val refTime = obj["referenceTime"]?.jsonPrimitive?.contentOrNull ?: continue
            val year = refTime.substring(0, 4).toIntOrNull() ?: continue
            val observations = obj["observations"]?.jsonArray ?: continue
            for (obs in observations) {
                val value = obs.jsonObject["value"]?.jsonPrimitive?.doubleOrNull ?: continue
                byYear.getOrPut(year) { mutableListOf() }.add(value)
            }
        }
        return byYear
    }

    /**
     * Parses monthly observations that include year information packed into the month field.
     *
     * @param body Raw JSON response body.
     * @return A list of monthly observations with year-month encoding.
     */
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

    /**
     * Parses annual observations into structured yearly aggregates.
     *
     * @param body Raw JSON response body.
     * @return A list of annual observations.
     */
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

    /**
     * Parses monthly climate observations from Frost API response.
     *
     * @param body Raw JSON response body.
     * @return A list of monthly observations.
     */
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
