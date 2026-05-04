package no.uio.ifi.in2000.team45.data.map

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Datastore for context
private val Context.cameraDataStore by preferencesDataStore(name = "mapCamera")

class MapCameraRepository(private val context: Context){

    // Keys to save and get cameraposition from DataStore
    private val keyLat = doublePreferencesKey("camera_lat")
    private val keyLng = doublePreferencesKey("camera_lng")
    private val keyZoom = doublePreferencesKey("camera_zoom")

    // A flow reading camerapositon from disk
    // If no positionvalue is saved, it uses standardvalues (showing norway)
    val cameraFlow: Flow<Triple<Double, Double, Double>> =
        context.cameraDataStore.data.map { preferences ->
            Triple(
                preferences[keyLat] ?: 65.0,
                preferences[keyLng] ?: 15.0,
                preferences[keyZoom] ?: 3.75
                
            )
        }

    // Saves the cameraposition to disk
    // Is called every time the camera moves
    suspend fun saveCamera(lat: Double, lng: Double, zoom: Double) {
        context.cameraDataStore.edit { preferences ->
            preferences[keyLat] = lat
            preferences[keyLng] = lng
            preferences[keyZoom] = zoom
        }
    }
}