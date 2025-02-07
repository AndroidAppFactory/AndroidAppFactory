package com.bihe0832.android.base.debug.audio.asr

import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.device.cpu.CPUHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.speech.getAAFEndpointConfig
import com.bihe0832.android.lib.speech.getAAFFeatureConfig
import com.bihe0832.android.base.debug.audio.asr.ASRModelDownloadManager.TAG
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
val md5_ASROfflineRecognizerConfig_paraformer = "e91e6fd014eb61a5f48f9788c17c10a4"
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
    "aeb33738b897c5d1352ec14145b1c696"
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

val modelDir_ASROnlieRecognizerConfig =
    "sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20-mobile"
val url_ASROnlieRecognizerConfig =
    "https://github.com/AndroidAppFactory/AAFASR/raw/refs/heads/main/model/sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20-mobile.zip"
val md5_ASROnlieRecognizerConfig =
    "c169f65f3c09629a357342b03bb70c20"
fun getASROnlieRecognizerConfig(): OnlineRecognizerConfig {
    ZLog.d(
        TAG,
        "path:" + getASRModelRoot() + "$modelDir_ASROnlieRecognizerConfig/joiner-epoch-99-avg-1.int8.onnx",
    )
    return OnlineRecognizerConfig(
        featConfig = getAAFFeatureConfig(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ),
        modelConfig = OnlineModelConfig(
            transducer = OnlineTransducerModelConfig(
                encoder = "$modelDir_ASROnlieRecognizerConfig/encoder-epoch-99-avg-1.int8.onnx",
                decoder = "$modelDir_ASROnlieRecognizerConfig/decoder-epoch-99-avg-1.onnx",
                joiner = "$modelDir_ASROnlieRecognizerConfig/joiner-epoch-99-avg-1.int8.onnx",
            ),
            tokens = "$modelDir_ASROnlieRecognizerConfig/tokens.txt",
            modelType = "zipformer",
        ),
        endpointConfig = getAAFEndpointConfig(2.4f, 1.4f, 30f),
        enableEndpoint = true,
    )
}


