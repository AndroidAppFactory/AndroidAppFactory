package com.bihe0832.android.common.compose.state

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ThemeState {

    private var _theme: ColorScheme by mutableStateOf(lightColorScheme())

    fun init(colors: ColorScheme) {
        _theme = colors
    }

    fun getCurrentThemeState(): ColorScheme {
        return _theme
    }

    fun changeTheme(colors: ColorScheme) {
        _theme = colors
    }
}