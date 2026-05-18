package com.example.fitme.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ColorVariant {
    PURPLE, GREEN, BLUE, ORANGE
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val COLOR_VARIANT_KEY = stringPreferencesKey("color_variant")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val name = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(name)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    val colorVariant: Flow<ColorVariant> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val name = preferences[COLOR_VARIANT_KEY] ?: ColorVariant.PURPLE.name
            try {
                ColorVariant.valueOf(name)
            } catch (e: IllegalArgumentException) {
                ColorVariant.PURPLE
            }
        }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun saveColorVariant(variant: ColorVariant) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_VARIANT_KEY] = variant.name
        }
    }
}
