package com.bihe0832.android.lib.speech.recognition

import android.content.Context
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.log.ZLog
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
    fun initRecognizer(context: Context, config: OnlineRecognizerConfig) {
        try {
            onlineRecognizer = OnlineRecognizer(assetManager = context.assets, config = config)
        } catch (e: Exception) {
            e.printStackTrace()
            onlineRecognizer = null
        }

    }

    fun initRecognizer(config: OnlineRecognizerConfig) {
        try {
            onlineRecognizer = OnlineRecognizer(null, config = config)
        } catch (e: Exception) {
            e.printStackTrace()
            onlineRecognizer = null
        }
    }

    fun destroy() {
        onlineRecognizer?.release()
        onlineRecognizer = null
    }

    fun getOnlineRecognizer(): OnlineRecognizer? {
        return onlineRecognizer
    }

    fun start(): OnlineStream? {
        ZLog.d(AudioRecordManager.TAG, "start")
        return onlineRecognizer?.createStream()
    }

    fun acceptWaveform(stream: OnlineStream, sampleRateInHz: Int, buffer: FloatArray?): String {
        ZLog.d(AudioRecordManager.TAG, "acceptWaveform")
        if (buffer != null && buffer.isNotEmpty()) {
            stream.acceptWaveform(buffer, sampleRateInHz)
            while (onlineRecognizer!!.isReady(stream)) {
                onlineRecognizer!!.decode(stream)
                ZLog.d(AudioRecordManager.TAG, onlineRecognizer!!.getResult(stream).text)
            }
            val result = onlineRecognizer!!.getResult(stream)
            return result.text
        }
        return ""
    }

    fun stop(stream: OnlineStream): String {
        ZLog.d(AudioRecordManager.TAG, "reset")
        stream.release()
        return ""
    }
}