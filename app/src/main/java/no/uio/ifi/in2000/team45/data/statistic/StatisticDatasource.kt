package no.uio.ifi.in2000.team45.data.statistic

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import no.uio.ifi.in2000.team45.network.KtorClient

class StatisticDatasource {

    suspend fun getStatistic(): String {
        val url = ""
        try {
            val httpResponse = KtorClient.client.get(url)
            val body: String = httpResponse.body()

            if (httpResponse.status.value != 200) {
                Log.e("Statistic", "HTTP error ${httpResponse.status.value}: $body")
                throw Exception("Failed with HTTP ${httpResponse.status.value}")
            }

            println(body)
            return body
        } catch (e: Exception) {
            Log.e("Statistic", "Couldn't get statistic", e)
            throw e
        }
    }
}