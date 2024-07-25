package com.bihe0832.android.lib.speech.recognition

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
public class ASRManager {

    private val TAG = "ASRManager"
    private lateinit var offlineRecognizer: OfflineRecognizer

    fun initRecognizer(context: Context,modelDir:String, sampleRateInHz: Int) {

        val config = OfflineRecognizerConfig(
            featConfig = FeatureConfig(sampleRate = sampleRateInHz, featureDim = 80),
            modelConfig = OfflineModelConfig(
                paraformer = OfflineParaformerModelConfig(
                    model = "$modelDir/model.int8.onnx",
                ),
                tokens = "$modelDir/tokens.txt",
                modelType = "paraformer",
            )
        )
        offlineRecognizer = OfflineRecognizer(
            assetManager = context.assets,
            config = config,
        )
    }

    fun startRecognizer(sampleRateInHz: Int, buffer: FloatArray?): String {
        Log.i(TAG, "processing samples")
        if (buffer != null && buffer.isNotEmpty()) {
            val n = maxOf(0, buffer.size - 8000)
            val stream = offlineRecognizer.createStream()
            stream.acceptWaveform(buffer.sliceArray(0..n), sampleRateInHz)
            offlineRecognizer.decode(stream)
            val result = offlineRecognizer.getResult(stream)
            stream.release()
            return result.text
        }
        return ""
    }

    fun destroy() {
        offlineRecognizer.release()
    }
}