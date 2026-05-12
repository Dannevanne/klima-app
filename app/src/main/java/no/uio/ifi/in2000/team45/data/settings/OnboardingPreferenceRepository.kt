package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding")

/**
 * Repository responsible for storing and retrieving onboarding completion state
 * using DataStore.
 */
class OnboardingPreferenceRepository(private val context: Context) {
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    /**
     * Flow indicating whether the onboarding process has been completed.
     *
     * @return A Flow emitting true if onboarding is completed, otherwise false.
     */
    val onboardingCompletedFlow: Flow<Boolean> = context.onboardingDataStore.data.map {
        it[ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Marks the onboarding process as completed and persists the state.
     */
    suspend fun setCompleted() {
        context.onboardingDataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }
}
