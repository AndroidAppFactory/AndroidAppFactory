package com.bihe0832.android.base.compose.debug.audio.wave

data class AudioWaveState(
    val audioDisplayName: String = "",
    val amplitudes: List<Int> = emptyList(),
    val isPlaying: Boolean = false,
    val progress: Float = 0F
)