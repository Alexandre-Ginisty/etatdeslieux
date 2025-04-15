package com.example.etatdeslieux.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etatdeslieux.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences
) : ViewModel() {

    val darkTheme = preferences.darkTheme
    val fontScale = preferences.fontScale

    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateDarkTheme(enabled)
        }
    }

    fun updateFontScale(scale: Float) {
        viewModelScope.launch {
            preferences.updateFontScale(scale)
        }
    }
}
