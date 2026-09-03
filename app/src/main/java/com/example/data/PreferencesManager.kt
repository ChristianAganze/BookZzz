package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bookzzz_settings")

class PreferencesManager(private val context: Context) {
    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SIMULATED_OFFLINE = booleanPreferencesKey("simulated_offline")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false
        }

    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE] ?: "Français"
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    val simulatedOffline: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SIMULATED_OFFLINE] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSimulatedOffline(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SIMULATED_OFFLINE] = enabled
        }
    }

    fun isDeviceOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return false
    }
}
