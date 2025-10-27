package com.bihe0832.android.base.compose.debug.audio


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.bihe0832.android.base.compose.debug.audio.wave.AudioWaveDebugView
import com.bihe0832.android.base.compose.debug.audio.wave.AudioWaveState
import com.bihe0832.android.lib.utils.MathUtils


@Composable
fun DebugAudioWaveView() {
    val initialUiState = AudioWaveState(
        audioDisplayName = "这是一个音频波形的演示", amplitudes = mutableListOf<Int>().apply {
            repeat(50) { add(MathUtils.getRandNumByLimit(0, 100)) }
        }, progress = 0.5f
    )
    val state = remember { mutableStateOf(initialUiState) }
    AudioWaveDebugView(uiState = state.value, onPlayClicked = {
        // 修改状态容器的 value（触发重组）
        state.value = state.value.copy(
            isPlaying = !state.value.isPlaying, progress = state.value.progress + 0.05F
        )
    }, onProgressChange = { value ->
        state.value = state.value.copy(
            progress = value
        )
    })
}