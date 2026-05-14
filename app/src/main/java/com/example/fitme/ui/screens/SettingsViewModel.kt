package com.example.fitme.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.ui.theme.ColorVariant
import com.example.fitme.ui.theme.ThemeMode
import com.example.fitme.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class gSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val themePreferences = ThemePreferences(application)

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val colorVariant: StateFlow<ColorVariant> = themePreferences.colorVariant.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ColorVariant.PURPLE
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.saveThemeMode(mode)
        }
    }

    fun setColorVariant(variant: ColorVariant) {
        viewModelScope.launch {
            themePreferences.saveColorVariant(variant)
        }
    }
}
