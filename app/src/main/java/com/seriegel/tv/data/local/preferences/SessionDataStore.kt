package com.seriegel.tv.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionDataStore(
    private val context: Context,
) {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val token: Flow<String?> = context.sessionDataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { preferences -> preferences[tokenKey] }

    val onboardingCompleted: Flow<Boolean> = context.sessionDataStore.data
        .map { preferences -> preferences[onboardingCompletedKey] ?: false }

    suspend fun saveToken(value: String) {
        context.sessionDataStore.edit { preferences ->
            preferences[tokenKey] = value
        }
    }

    suspend fun clearToken() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.sessionDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = value
        }
    }
}
