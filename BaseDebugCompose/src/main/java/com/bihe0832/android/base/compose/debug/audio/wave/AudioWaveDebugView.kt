package com.bihe0832.android.base.compose.debug.audio.wave

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.base.compose.debug.color.getDebugColorPalettes
import com.bihe0832.android.lib.audio.wave.AAFAudioWaveView
import com.bihe0832.android.lib.audio.wave.model.AmplitudeType
import com.bihe0832.android.lib.audio.wave.model.WaveformAlignment


@Composable
fun AudioWaveDebugView(
    uiState: AudioWaveState,
    onPlayClicked: () -> Unit,
    onProgressChange: (Float) -> Unit,
) {
    val colorPalettes = getDebugColorPalettes()
    val waveformStyles = getMockStyles()
    var colorPaletteIndex by remember { mutableStateOf(0) }
    var waveformStyle by remember { mutableStateOf(waveformStyles.first()) }
    var waveformAlignment by remember { mutableStateOf(WaveformAlignment.Center) }
    var amplitudeType by remember { mutableStateOf(AmplitudeType.Avg) }
    var spikeWidth by remember { mutableStateOf(2F) }
    var spikePadding by remember { mutableStateOf(4F) }
    var spikeCornerRadius by remember { mutableStateOf(1F) }
    var scrollEnabled by remember { mutableStateOf(true) }
    val playButtonIcon by remember(uiState.isPlaying) {
        mutableStateOf(if (uiState.isPlaying) R.drawable.icon_pause else R.drawable.icon_start_fill)
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .verticalScroll(state = rememberScrollState(), enabled = scrollEnabled)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = uiState.audioDisplayName,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                AAFAudioWaveView(modifier = Modifier
                    .width(200.dp)
                    .height(20.dp),
                    style = waveformStyle.style,
                    waveformAlignment = waveformAlignment,
                    amplitudeType = amplitudeType,
                    progressBrush = colorPalettes[colorPaletteIndex].firstColor,
                    waveformBrush = colorPalettes[colorPaletteIndex].secondColor,
                    spikeWidth = Dp(spikeWidth),
                    spikePadding = Dp(spikePadding),
                    spikeRadius = Dp(spikeCornerRadius),
                    progress = uiState.progress,
                    amplitudes = uiState.amplitudes,
                    onProgressChange = {
                        scrollEnabled = false
                        onProgressChange(it)
                    },
                    canScroll = uiState.progress > 0.3f,
                    onProgressChangeFinished = {
                        scrollEnabled = true
                    })
                Spacer(modifier = Modifier.padding(vertical = 16.dp))
                AAFAudioWaveView(modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                    style = waveformStyle.style,
                    waveformAlignment = waveformAlignment,
                    amplitudeType = amplitudeType,
                    progressBrush = colorPalettes[colorPaletteIndex].firstColor,
                    waveformBrush = colorPalettes[colorPaletteIndex].secondColor,
                    spikeWidth = Dp(spikeWidth),
                    spikePadding = Dp(spikePadding),
                    spikeRadius = Dp(spikeCornerRadius),
                    progress = uiState.progress,
                    amplitudes = uiState.amplitudes,
                    onProgressChange = {
                        scrollEnabled = false
                        onProgressChange(it)
                    },
                    canScroll = uiState.progress > 0.3f,
                    onProgressChangeFinished = {
                        scrollEnabled = true
                    })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                LabelSlider(
                    text = "波形宽度：$spikeWidth",
                    value = spikeWidth,
                    onValueChange = { spikeWidth = it },
                    valueRange = 1.dp.value..24.dp.value
                )
                LabelSlider(
                    text = "波形间距：$spikePadding",
                    value = spikePadding,
                    onValueChange = { spikePadding = it },
                    valueRange = 0.dp.value..12.dp.value
                )
                LabelSlider(
                    text = "波形圆角：$spikeCornerRadius",
                    value = spikeCornerRadius,
                    onValueChange = { spikeCornerRadius = it },
                    valueRange = 0.dp.value..12.dp.value
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(modifier = Modifier.align(Alignment.CenterVertically), text = "对齐")
                    WaveformAlignment.values().forEach {
                        RadioGroupItem(text = it.name,
                            selected = waveformAlignment == it,
                            onClick = { waveformAlignment = it })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(modifier = Modifier.align(Alignment.CenterVertically), text = "振幅")
                    AmplitudeType.values().forEach {
                        RadioGroupItem(text = it.name,
                            selected = amplitudeType == it,
                            onClick = { amplitudeType = it })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(modifier = Modifier.align(Alignment.CenterVertically), text = "样式")
                    waveformStyles.forEach {
                        RadioGroupItem(
                            text = it.label,
                            selected = it.label == waveformStyle.label,
                            onClick = {
                                waveformStyle = it
                            })
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text(modifier = Modifier.align(Alignment.CenterHorizontally), text = "波形颜色方案")
                colorPalettes.forEach {
                    ColorPaletteItem(
                        selected = it.label == colorPalettes[colorPaletteIndex].label,
                        progressColor = it.firstColor,
                        waveformColor = it.secondColor
                    ) {
                        colorPaletteIndex = colorPalettes.indexOf(it)
                    }
                }
            }
            FloatingActionButton(
                modifier = Modifier.align(Alignment.BottomEnd), onClick = onPlayClicked
            ) {
                Icon(
                    painter = painterResource(id = playButtonIcon), contentDescription = null
                )
            }
        }
    }
}

@Composable
fun LabelSlider(
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    text: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text)
        Slider(
            value = value, onValueChange = onValueChange, valueRange = valueRange
        )
    }
}

@Composable
fun RadioGroupItem(
    modifier: Modifier = Modifier, text: String, selected: Boolean, onClick: () -> Unit
) {
    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text)
        RadioButton(
            selected = selected, onClick = onClick
        )
    }
}

@Composable
fun ColorPaletteItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    progressColor: Brush,
    waveformColor: Brush,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Box(
            modifier = Modifier
                .height(24.dp)
                .weight(1F)
                .background(progressColor)
        )
        Box(
            modifier = Modifier
                .height(24.dp)
                .weight(1F)
                .background(waveformColor)
        )
    }
}

@Preview
@Composable
private fun AudioWaveformScreenPreview() {
    AudioWaveDebugView(uiState = AudioWaveState(), onPlayClicked = {}, onProgressChange = {})
}