package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding")

class OnboardingPreferenceRepository(private val context: Context) {
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.onboardingDataStore.data.map {
        it[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setCompleted() {
        context.onboardingDataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }
}
