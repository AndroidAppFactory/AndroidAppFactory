package com.bihe0832.android.base.compose.debug.color

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

data class DebugColor(
    val label: String,
    val firstColor: Brush,
    val secondColor: Brush
)

@JvmName("getPalettesFun")
@Composable
fun getDebugColorPalettes() = listOf(
    DebugColor("Gray", SolidColor(Color.DarkGray), SolidColor(Color.LightGray)),
    DebugColor("Blue/Yellow", SolidColor(Color.Blue), SolidColor(Color.Yellow)),
    DebugColor("White/Red", SolidColor(Color.White), SolidColor(Color.Red)),
    DebugColor(
        "ComposeColors",
        Brush.horizontalGradient(listOf(Color(0xFF136FC3), Color(0xFF76EF66))),
        SolidColor(Color.LightGray)
    ),
    DebugColor(
        "Frozen",
        Brush.verticalGradient(listOf(Color(0xFF403B4A), Color(0xFFE7E9BB))),
        SolidColor(Color.LightGray)
    ),
    DebugColor(
        "IntuitivePurple",
        Brush.linearGradient(listOf(Color(0xFFDA22FF), Color(0xFF9733EE))),
        SolidColor(Color.LightGray)
    )
)