package com.bihe0832.android.base.compose.debug.audio.wave

import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

data class AudioWaveStyle(
    val label: String,
    val style: DrawStyle
)

fun getMockStyles() = listOf(
    AudioWaveStyle("填充", Fill),
    AudioWaveStyle("描边", Stroke(width = 1f)),
    AudioWaveStyle(
        "圆点",
        Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5F, 5F)))
    )
)