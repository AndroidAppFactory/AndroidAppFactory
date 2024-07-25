package com.bihe0832.android.lib.speech.kws

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
public class KeywordSpotterManager {

    private val TAG = "ASRManager"
    private lateinit var kws: KeywordSpotter
    private lateinit var stream: OnlineStream

    fun initRecognizer(context: Context, modelDir: String, keywordsFile: String, sampleRateInHz: Int) {
        val config = KeywordSpotterConfig(
            featConfig = FeatureConfig(sampleRate = sampleRateInHz, featureDim = 80),
            modelConfig = OnlineModelConfig(
                transducer = OnlineTransducerModelConfig(
                    encoder = "$modelDir/encoder-epoch-12-avg-2-chunk-16-left-64.onnx",
                    decoder = "$modelDir/decoder-epoch-12-avg-2-chunk-16-left-64.onnx",
                    joiner = "$modelDir/joiner-epoch-12-avg-2-chunk-16-left-64.onnx",
                ),
                tokens = "$modelDir/tokens.txt",
                modelType = "zipformer2",
            ),
            keywordsFile = "$modelDir/$keywordsFile",
        )
        kws = KeywordSpotter(
            assetManager = context.assets,
            config = config,
        )
        stream = kws.createStream()

    }

    fun start(key: String): Boolean {
        Log.i(TAG, key)
        stream.release()
        var keywords = key.replace("\n", "/")
        keywords = keywords.trim()
        // If keywords is an empty string, it just resets the decoding stream
        // always returns true in this case.
        // If keywords is not empty, it will create a new decoding stream with
        // the given keywords appended to the default keywords.
        // Return false if errors occurred when adding keywords, true otherwise.
        return if (keywords.isNotEmpty()) {
            stream = kws.createStream(keywords)
            if (stream.ptr == 0L) {
                Log.i(TAG, "Failed to create stream with keywords: $keywords")
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    fun stop() {
        stream.release()
    }

    fun destroy() {
        stop()
        kws.release()
    }

    fun doRecognizer(sampleRateInHz: Int, buffer: FloatArray?): String {
        Log.i(TAG, "processing samples")
        if (buffer != null && buffer.isNotEmpty()) {
            stream.acceptWaveform(buffer, sampleRateInHz)
            while (kws.isReady(stream)) {
                kws.decode(stream)
            }
            val result = kws.getResult(stream)
            return result.keyword
        }
        return ""
    }
}