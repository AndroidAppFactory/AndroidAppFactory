package com.bihe0832.android.common.compose.state

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ThemeState {

    // ===== 基础色值（直接引用你的资源）=====
    val primary = Color(0xFF3F51B5)          // <color name="primary">
    val primary_light = Color(0xFFC5CAE9)    // <color name="primary_light">
    val accent = Color(0xFF536DFE)           // <color name="accent">
    val primary_text = Color(0xFF212121)      // <color name="primary_text">
    val secondary_text = Color(0xFF8D8D8D)    // <color name="secondary_text">
    val icons = Color(0xFFFFFFFF)             // <color name="icons">
    val window_background = Color(0xFFFFFFFF) // <color name="window_background">

    // ===== 亮色主题 =====
    val AAFLightColorScheme = lightColorScheme(
        // 主色组
        primary = primary,
        onPrimary = icons,                    // 原 color_on_primary（图标/文字色）
        primaryContainer = primary_light,      // 原 primary_light
        onPrimaryContainer = primary_text,     // 原 primary_text（深色文本）

        // 次要色组 → 原 colorAccent
        secondary = accent,                   // 原 <color name="colorAccent">
        onSecondary = icons,                   // 白色文本（与 onPrimary 一致）
        secondaryContainer = accent.copy(alpha = 0.2f), // 半透明容器（M3 规范）

        // 表面色组
        surface = window_background,           // 原 window_background
        onSurface = primary_text,              // 主文本（原 textColorPrimary）
        onSurfaceVariant = secondary_text,     // 辅助文本（原 textColorSecondary）
        surfaceVariant = Color(0xFFEEEEEE),    // 浅灰容器（替代 divider）
        outline = Color(0xFFBDBDBD),           // 分割线（原 divider）
        outlineVariant = Color(0xFFE0E0E0),    // 弱化分割线

        // 背景色组
        background = window_background,        // 原 window_background
        onBackground = primary_text,           // 原 color_on_window → primary_text

        // 错误色组（补充 M3 必需项）
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF)
    )

    val AAFDarkColorScheme = darkColorScheme(
        primary = Color(0xFF9FA8DA),          // 提亮主色（深色背景需增强可见性）
        onPrimary = primary,                  // 原主色用作深色模式文本
        primaryContainer = primary_light.copy(alpha = 0.6f),
        onPrimaryContainer = Color(0xFFE0E0E0),// 浅灰文本（提高对比度）

        secondary = Color(0xFF7B9EF8),        // 提亮强调色
        onSecondary = Color(0xFF1A237E),       // 深蓝文本（避免浅色刺眼）

        surface = Color(0xFF121212),           // 深灰基底（M3 标准）
        onSurface = Color(0xFFE0E0E0),         // 浅灰主文本
        onSurfaceVariant = Color(0xFFB0B0B0),   // 中灰辅助文本
        surfaceVariant = Color(0xFF1E1E1E),    // 深灰容器

        background = Color(0xFF000000),         // 纯黑背景（强化层次感）
        onBackground = Color(0xFFE0E0E0)       // 浅灰背景文本
    )

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