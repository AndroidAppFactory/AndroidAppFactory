package com.bihe0832.android.lib.speech.recognition

import android.content.Context
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.speech.endpoint.ASREndpointCheck.CheckResult
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineStream

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
public class ASROnlineManager {

    private var onlineRecognizer: OnlineRecognizer? = null
    private var isReady = false

    fun initRecognizer(context: Context, config: OnlineRecognizerConfig) {
        try {
            onlineRecognizer = OnlineRecognizer(assetManager = context.assets, config = config)
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            onlineRecognizer = null
        }
    }

    fun initRecognizer(config: OnlineRecognizerConfig) {
        try {
            onlineRecognizer = OnlineRecognizer(null, config = config)
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            onlineRecognizer = null
        }
    }


    fun getOnlineRecognizer(): OnlineRecognizer? {
        return onlineRecognizer
    }

    fun start(): OnlineStream? {
        ZLog.d(AudioRecordManager.TAG, "start")
        return onlineRecognizer?.createStream()
    }

    @Synchronized
    fun acceptWaveform(
        stream: OnlineStream, sampleRateInHz: Int, buffer: FloatArray?
    ): CheckResult {
        ZLog.d(AudioRecordManager.TAG, "acceptWaveform")
        if (isReady && buffer != null && buffer.isNotEmpty()) {
            stream.acceptWaveform(buffer, sampleRateInHz)
            while (onlineRecognizer!!.isReady(stream)) {
                onlineRecognizer!!.decode(stream)
                ZLog.d(AudioRecordManager.TAG, onlineRecognizer!!.getResult(stream).text)
            }
            val result = onlineRecognizer!!.getResult(stream)
            return if (onlineRecognizer!!.isEndpoint(stream)) {
                CheckResult(true, result)
            } else {
                CheckResult(false, result)
            }
        }
        return CheckResult(false, null)
    }

    fun resetStream(stream: OnlineStream) {
        onlineRecognizer?.reset(stream)
    }

    fun stop(stream: OnlineStream) {
        ZLog.d(AudioRecordManager.TAG, "reset")
        resetStream(stream)
        stream.release()
    }

    fun isReady(): Boolean {
        return isReady
    }

    fun destroy() {
        isReady = false
        onlineRecognizer?.release()
        onlineRecognizer = null
    }
}