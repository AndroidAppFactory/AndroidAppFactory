package com.bihe0832.android.lib.speech.recognition

import android.content.Context
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.log.ZLog
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
public class ASROfflineManager {

    private var offlineRecognizer: OfflineRecognizer? = null
    private var isReady = false

    fun initRecognizer(context: Context, config: OfflineRecognizerConfig) {
        ZLog.d(AudioRecordManager.TAG, "initRecognizer start")
        try {
            offlineRecognizer = OfflineRecognizer(
                assetManager = context.assets,
                config = config,
            )
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            offlineRecognizer = null
        }
        ZLog.d(AudioRecordManager.TAG, "initRecognizer end")
    }

    fun initRecognizer(config: OfflineRecognizerConfig) {
        ZLog.d(AudioRecordManager.TAG, "initRecognizer start")
        try {
            offlineRecognizer = OfflineRecognizer(
                null,
                config = config,
            )
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            offlineRecognizer = null
        }

        ZLog.d(AudioRecordManager.TAG, "initRecognizer end")
    }

    @Synchronized
    fun startRecognizer(sampleRateInHz: Int, buffer: FloatArray?): String {
        ZLog.d(AudioRecordManager.TAG, "processing samples")
        try {
            if (offlineRecognizer != null && isReady && buffer != null && buffer.isNotEmpty()) {
                val stream = offlineRecognizer!!.createStream()
                stream.acceptWaveform(buffer, sampleRateInHz)
                offlineRecognizer!!.decode(stream)
                val result = offlineRecognizer!!.getResult(stream)
                stream.release()
                return result.text
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun isReady(): Boolean {
        return isReady
    }

    fun destroy() {
        isReady = false
        offlineRecognizer?.release()
        offlineRecognizer = null
    }
}