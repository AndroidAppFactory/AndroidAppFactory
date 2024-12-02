package com.bihe0832.android.lib.speech

import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.EndpointRule
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/10/14.
 * Description:
 *
 */
const val DEFAULT_KWS_MODEL_DIR = "sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01"
const val DEFAULT_ENDPOINT_MODEL_DIR = "sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23"
const val DEFAULT_WHISPER_TINY_MODEL_DIR = ""
const val DEFAULT_WHISPER_BASE_MODEL_DIR = ""

fun getAAFFeatureConfig(sampleRateInHz: Int): FeatureConfig {
    return FeatureConfig(
        sampleRate = sampleRateInHz, featureDim = 80
    )
}

fun getAAFEndpointConfig(
    minTrailingForNone: Float,
    minTrailingForBetween: Float,
    maxUtteranceLength: Float,
): EndpointConfig {
    return EndpointConfig(
        rule1 = EndpointRule(false, minTrailingForNone, 0.0f),
        rule2 = EndpointRule(true, minTrailingForBetween, 0.0f),
        rule3 = EndpointRule(false, 0.0f, maxUtteranceLength)
    )
}

fun getAAFOnlineModelConfig(modelAssetsDir: String): OnlineModelConfig {
    return OnlineModelConfig(
        transducer = OnlineTransducerModelConfig(
            encoder = "$modelAssetsDir/encoder-epoch-99-avg-1.int8.onnx",
            decoder = "$modelAssetsDir/decoder-epoch-99-avg-1.onnx",
            joiner = "$modelAssetsDir/joiner-epoch-99-avg-1.int8.onnx",
        ),
        tokens = "$modelAssetsDir/tokens.txt",
        modelType = "zipformer",
    )
}

fun getDefaultOnlineRecognizerConfig(sampleRateInHz: Int, modelAssetsDir: String): OnlineRecognizerConfig {
    return getAAFOnlineRecognizerConfig(sampleRateInHz, modelAssetsDir, getDefaultEndpointConfig())
}

fun getAAFOnlineRecognizerConfig(
    sampleRateInHz: Int, modelAssetsDir: String, endpointConfig: EndpointConfig,
): OnlineRecognizerConfig {
    return OnlineRecognizerConfig(
        featConfig = getAAFFeatureConfig(sampleRateInHz),
        modelConfig = getAAFOnlineModelConfig(modelAssetsDir),
        endpointConfig = endpointConfig,
        enableEndpoint = true,
    )
}

fun getDefaultEndpointConfig(): EndpointConfig {
    return getAAFEndpointConfig(2.4f, 1.4f, 30f)
}

fun getDefaultKeywordSpotterConfig(sampleRateInHz: Int, modelAssetsDir: String): KeywordSpotterConfig {
    return KeywordSpotterConfig(
        featConfig = getAAFFeatureConfig(sampleRateInHz),
        modelConfig = OnlineModelConfig(
            transducer = OnlineTransducerModelConfig(
                encoder = "$modelAssetsDir/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
                decoder = "$modelAssetsDir/decoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
                joiner = "$modelAssetsDir/joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
            ),
            tokens = "$modelAssetsDir/tokens.txt",
            modelType = "zipformer2",
        ),
        keywordsFile = "$modelAssetsDir/keywords.txt",
    )
}