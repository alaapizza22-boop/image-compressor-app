package com.imagecompressor.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val languageTag: String = "en",
    val defaultQuality: Int = 80,
    val keepOriginalDimensionsDefault: Boolean = true
)

/** Persists user-facing preferences: UI language (en/ar), default compression quality. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_tag")
        val QUALITY = intPreferencesKey("default_quality")
        val KEEP_DIMENSIONS = booleanPreferencesKey("keep_original_dimensions")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            languageTag = prefs[Keys.LANGUAGE] ?: "en",
            defaultQuality = prefs[Keys.QUALITY] ?: 80,
            keepOriginalDimensionsDefault = prefs[Keys.KEEP_DIMENSIONS] ?: true
        )
    }

    suspend fun setLanguage(tag: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = tag }
    }

    suspend fun setDefaultQuality(quality: Int) {
        context.dataStore.edit { it[Keys.QUALITY] = quality }
    }

    suspend fun setKeepOriginalDimensionsDefault(value: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_DIMENSIONS] = value }
    }
}
