package com.anam145.wallet.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.miniAppDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mini_app_preferences"
)

@Singleton
class MiniAppDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val MINI_APPS_INITIALIZED = booleanPreferencesKey("mini_apps_initialized")
    }
    
    private val dataStore = context.miniAppDataStore
    
    val isMiniAppsInitialized: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[MINI_APPS_INITIALIZED] ?: false
    }
    
    suspend fun setMiniAppsInitialized(initialized: Boolean) {
        dataStore.edit { preferences ->
            preferences[MINI_APPS_INITIALIZED] = initialized
        }
    }
    
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}