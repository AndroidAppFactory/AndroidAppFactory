package com.bihe0832.android.lib.speech.kws

import android.content.Context
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.log.ZLog
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.OnlineStream

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
public class KeywordSpotterManager {

    private lateinit var kws: KeywordSpotter
    private lateinit var stream: OnlineStream
    private var isReady = false

    fun initRecognizer(context: Context, config: KeywordSpotterConfig) {
        try {
            kws = KeywordSpotter(
                assetManager = context.assets,
                config = config,
            )
            stream = kws.createStream()
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun initRecognizer(config: KeywordSpotterConfig) {
        try {
            kws = KeywordSpotter(
                null,
                config = config,
            )
            stream = kws.createStream()
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun start(key: String) {
        ZLog.d(AudioRecordManager.TAG, key)
        var keywords = key.replace("\n", "/")
        keywords = keywords.trim()
        // If keywords is an empty string, it just resets the decoding stream
        // always returns true in this case.
        // If keywords is not empty, it will create a new decoding stream with
        // the given keywords appended to the default keywords.
        // Return false if errors occurred when adding keywords, true otherwise.
        if (keywords.isNotEmpty()) {
            stream.release()
            stream = kws.createStream(keywords)
            if (stream.ptr == 0L) {
                ZLog.d(AudioRecordManager.TAG, "Failed to create stream with keywords: $keywords")
            }
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
        ZLog.d(AudioRecordManager.TAG, "processing samples")
        if (buffer != null && buffer.isNotEmpty()) {
            stream.acceptWaveform(buffer, sampleRateInHz)
            while (kws.isReady(stream)) {
                kws.decode(stream)
            }
            val result = kws.getResult(stream)
            val text = result.keyword
            if (text.isNotBlank()) {
                kws.reset(stream)
            }
            return text
        }
        return ""
    }
}