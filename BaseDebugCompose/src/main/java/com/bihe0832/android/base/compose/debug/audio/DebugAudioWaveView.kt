package com.bihe0832.android.base.compose.debug.audio


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.bihe0832.android.base.compose.debug.audio.wave.AudioWaveDebugView
import com.bihe0832.android.base.compose.debug.audio.wave.AudioWaveState
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.MathUtils


@Composable
fun DebugAudioWaveView() {
    val initialUiState = AudioWaveState(
        audioDisplayName = "这是一个音频波形的演示",
//        amplitudes = mutableListOf(29, 24, 91, 60, 78, 19, 62, 13, 25, 70, 18, 6, 26, 97, 13, 55, 33, 27, 13, 32, 96, 14, 54, 66, 6, 47, 89, 51, 38, 56, 18, 47, 95, 4, 54, 16, 18, 81, 23, 22, 29, 41, 30, 42, 31, 95, 75, 43, 66, 90),
        amplitudes = mutableListOf<Int>().apply {
            for (i in 1..50) {
                add(MathUtils.getRandNumByLimit(0, 100))
            }
        },
        progress = 0.5f
    )
    ZLog.d("initialUiState:" + initialUiState.amplitudes.toString())
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