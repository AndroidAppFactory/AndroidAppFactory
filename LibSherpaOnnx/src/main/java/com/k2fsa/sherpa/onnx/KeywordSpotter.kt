// Copyright (c)  2024  Xiaomi Corporation
package com.k2fsa.sherpa.onnx

import android.content.res.AssetManager

data class KeywordSpotterConfig(
    var featConfig: FeatureConfig = FeatureConfig(),
    var modelConfig: OnlineModelConfig = OnlineModelConfig(),
    var maxActivePaths: Int = 4,
    var keywordsFile: String = "keywords.txt",
    var keywordsScore: Float = 1.5f,
    var keywordsThreshold: Float = 0.25f,
    var numTrailingBlanks: Int = 2,
)

data class KeywordSpotterResult(
    val keyword: String,
    val tokens: Array<String>,
    val timestamps: FloatArray,
)

class KeywordSpotter(
    assetManager: AssetManager? = null,
    val config: KeywordSpotterConfig,
) {
    private var ptr: Long

    init {
        ptr = if (assetManager != null) {
            newFromAsset(assetManager, config)
        } else {
            newFromFile(config)
        }
    }

    protected fun finalize() {
        if (ptr != 0L) {
            delete(ptr)
            ptr = 0
        }
    }

    fun release() = finalize()

    fun createStream(keywords: String = ""): OnlineStream {
        val p = createStream(ptr, keywords)
        return OnlineStream(p)
    }

    fun decode(stream: OnlineStream) = decode(ptr, stream.ptr)
    fun reset(stream: OnlineStream) = reset(ptr, stream.ptr)
    fun isReady(stream: OnlineStream) = isReady(ptr, stream.ptr)
    fun getResult(stream: OnlineStream): KeywordSpotterResult {
        val objArray = getResult(ptr, stream.ptr)

        val keyword = objArray[0] as String
        val tokens = objArray[1] as Array<String>
        val timestamps = objArray[2] as FloatArray

        return KeywordSpotterResult(keyword = keyword, tokens = tokens, timestamps = timestamps)
    }

    private external fun delete(ptr: Long)

    private external fun newFromAsset(
        assetManager: AssetManager,
        config: KeywordSpotterConfig,
    ): Long

    private external fun newFromFile(
        config: KeywordSpotterConfig,
    ): Long

    private external fun createStream(ptr: Long, keywords: String): Long
    private external fun isReady(ptr: Long, streamPtr: Long): Boolean
    private external fun decode(ptr: Long, streamPtr: Long)
    private external fun reset(ptr: Long, streamPtr: Long)
    private external fun getResult(ptr: Long, streamPtr: Long): Array<Any>

    companion object {
        init {
            System.loadLibrary("sherpa-onnx-jni")
        }
    }
}