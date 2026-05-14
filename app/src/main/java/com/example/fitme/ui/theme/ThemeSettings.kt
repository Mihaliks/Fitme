package com.example.fitme.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ColorVariant {
    PURPLE, GREEN, BLUE, ORANGE
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {
    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val COLOR_VARIANT_KEY = stringPreferencesKey("color_variant")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val name = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try { ThemeMode.valueOf(name) } catch (e: Exception) { ThemeMode.SYSTEM }
    }

    val colorVariant: Flow<ColorVariant> = context.dataStore.data.map { preferences ->
        val name = preferences[COLOR_VARIANT_KEY] ?: ColorVariant.PURPLE.name
        try { ColorVariant.valueOf(name) } catch (e: Exception) { ColorVariant.PURPLE }
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
