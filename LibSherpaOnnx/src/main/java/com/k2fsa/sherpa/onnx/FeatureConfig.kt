package com.k2fsa.sherpa.onnx

import com.bihe0832.android.lib.audio.AudioRecordConfig

data class FeatureConfig(
    var sampleRate: Int = AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ,
    var featureDim: Int = 80,
    var dither: Float = 0.0f
)

fun getFeatureConfig(sampleRate: Int, featureDim: Int): FeatureConfig {
    return FeatureConfig(sampleRate = sampleRate, featureDim = featureDim)
}
