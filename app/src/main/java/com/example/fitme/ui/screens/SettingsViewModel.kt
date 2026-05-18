package com.example.fitme.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.User
import com.example.fitme.data.repositories.UserRepository
import com.example.fitme.ui.theme.ColorVariant
import com.example.fitme.ui.theme.ThemeMode
import com.example.fitme.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val themePreferences = ThemePreferences(application)
    private val db = AppDatabase.getInstance(application)
    private val userRepository = UserRepository(db.userDao())

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

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _user.value = userRepository.getUser()
        }
    }

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

    fun updateName(name: String) {
        viewModelScope.launch {
            userRepository.setName(name)
            loadUser()
        }
    }

    fun updateAge(age: Int) {
        viewModelScope.launch {
            userRepository.setAge(age)
            loadUser()
        }
    }

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            userRepository.setWeight(weight)
            loadUser()
        }
    }

    fun updateHeight(height: Float) {
        viewModelScope.launch {
            userRepository.setHeight(height)
            loadUser()
        }
    }
}
