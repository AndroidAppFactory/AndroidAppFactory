package com.bihe0832.android.base.debug.audio.asr

import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.device.cpu.CPUHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.speech.getAAFEndpointConfig
import com.bihe0832.android.lib.speech.getAAFFeatureConfig
import com.bihe0832.android.base.debug.audio.asr.ASRModelDownloadManager.TAG
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import com.k2fsa.sherpa.onnx.getFeatureConfig
import kotlin.math.max

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2024/12/6.
 * Description: https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models
 *
 */

const val SCENE = "debugRecord"

fun getASRModelRoot(): String {
    return AAFFileWrapper.getCacheFolder("model")
}

val modelDir_ASROfflineRecognizerConfig_paraformer = "sherpa-onnx-paraformer-zh-2024-03-09"
val url_ASROfflineRecognizerConfig_paraformer =
    "https://github.com/AndroidAppFactory/AAFASR/raw/refs/heads/main/model/sherpa-onnx-paraformer-zh-2024-03-09.zip"
val md5_ASROfflineRecognizerConfig_paraformer = "be5ed74c6b809552098ca95fdca0ab91"
fun getASROfflineRecognizerConfig_paraformer(): OfflineRecognizerConfig {
    ZLog.d(
        TAG,
        "path:" + getASRModelRoot() + "$modelDir_ASROfflineRecognizerConfig_paraformer/model.int8.onnx",
    )
    return OfflineRecognizerConfig(
        featConfig = getFeatureConfig(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, 80),
        modelConfig = OfflineModelConfig(
            paraformer = OfflineParaformerModelConfig(
                model = getASRModelRoot() + "$modelDir_ASROfflineRecognizerConfig_paraformer/model.int8.onnx",
            ),
            numThreads = max(CPUHelper.getNumberOfCores() / 2, 1),
            debug = true,
            tokens = getASRModelRoot() + "$modelDir_ASROfflineRecognizerConfig_paraformer/tokens.txt",
            modelType = "paraformer",
        )
    )
}

val modelDir_ASROfflineRecognizerConfig_paraformer_small =
    "sherpa-onnx-paraformer-zh-small-2024-03-09"
val url_ASROfflineRecognizerConfig_paraformer_small =
    "https://github.com/AndroidAppFactory/AAFASR/raw/refs/heads/main/model/sherpa-onnx-paraformer-zh-small-2024-03-09.zip"
val md5_ASROfflineRecognizerConfig_paraformer_small =
    "583c1c103b84fd6f67656445de41e45e"
fun getASROfflineRecognizerConfig_paraformer_small(): OfflineRecognizerConfig {
    ZLog.d(
        TAG,
        "path:" + getASRModelRoot() + "$modelDir_ASROfflineRecognizerConfig_paraformer_small/model.int8.onnx"
    )
    return OfflineRecognizerConfig(
        featConfig = getFeatureConfig(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, 80),
        modelConfig = OfflineModelConfig(
            paraformer = OfflineParaformerModelConfig(
                model = getASRModelRoot() + "$modelDir_ASROfflineRecognizerConfig_paraformer_small/model.int8.onnx",
            ),
            numThreads = max(CPUHelper.getNumberOfCores() / 2, 1),
            debug = true,
            tokens = getASRModelRoot() + "$modelDir_ASROfflineRecognizerConfig_paraformer_small/tokens.txt",
            modelType = "paraformer",
        )
    )
}

val modelDir_ASROnlineRecognizerConfig =
    "sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20"
val url_ASROnlineRecognizerConfig =
    "https://github.com/AndroidAppFactory/AAFASR/raw/refs/heads/main/model/sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20.zip"
val md5_ASROnlineRecognizerConfig =
    "1a93f5c3daa59fecd3de6fb758b4cc07"
fun getASROnlineRecognizerConfig(): OnlineRecognizerConfig {
    ZLog.d(
        TAG,
        "path:" + getASRModelRoot() + "$modelDir_ASROnlineRecognizerConfig/joiner-epoch-99-avg-1.int8.onnx",
    )
    return OnlineRecognizerConfig(
        featConfig = getAAFFeatureConfig(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ),
        modelConfig = OnlineModelConfig(
            transducer = OnlineTransducerModelConfig(
                encoder = "$modelDir_ASROnlineRecognizerConfig/encoder-epoch-99-avg-1.int8.onnx",
                decoder = "$modelDir_ASROnlineRecognizerConfig/decoder-epoch-99-avg-1.int8.onnx",
                joiner = "$modelDir_ASROnlineRecognizerConfig/joiner-epoch-99-avg-1.int8.onnx",
            ),
            tokens = "$modelDir_ASROnlineRecognizerConfig/tokens.txt",
            modelType = "zipformer",
        ),
        endpointConfig = getAAFEndpointConfig(2.4f, 1.4f, 30f),
        enableEndpoint = true,
    )
}


val modelDir_ASROnlineRecognizerConfig_small =
    "sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20-mobile"
val url_ASROnlineRecognizerConfig_small =
    "https://github.com/AndroidAppFactory/AAFASR/raw/refs/heads/main/model/sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20-mobile.zip"
val md5_ASROnlineRecognizerConfig_small =
    "848fa55c84d7a3f44aa7f616fc30229e"
fun getASROnlineRecognizerConfig_small(): OnlineRecognizerConfig {
    ZLog.d(
        TAG,
        "path:" + getASRModelRoot() + "$modelDir_ASROnlineRecognizerConfig_small/joiner-epoch-99-avg-1.int8.onnx",
    )
    return OnlineRecognizerConfig(
        featConfig = getAAFFeatureConfig(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ),
        modelConfig = OnlineModelConfig(
            transducer = OnlineTransducerModelConfig(
                encoder = "$modelDir_ASROnlineRecognizerConfig_small/encoder-epoch-99-avg-1.int8.onnx",
                decoder = "$modelDir_ASROnlineRecognizerConfig_small/decoder-epoch-99-avg-1.onnx",
                joiner = "$modelDir_ASROnlineRecognizerConfig_small/joiner-epoch-99-avg-1.int8.onnx",
            ),
            tokens = "$modelDir_ASROnlineRecognizerConfig_small/tokens.txt",
            modelType = "zipformer",
        ),
        endpointConfig = getAAFEndpointConfig(2.4f, 1.4f, 30f),
        enableEndpoint = true,
    )
}
