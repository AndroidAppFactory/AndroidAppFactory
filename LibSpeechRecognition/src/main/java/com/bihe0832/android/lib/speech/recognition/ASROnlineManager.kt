package com.bihe0832.android.lib.speech.recognition

import android.content.Context
import android.util.Log
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.log.ZLog
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
public class ASROnlineManager {

    private lateinit var onlineRecognizer: OnlineRecognizer
    fun initRecognizer(context: Context, config: OnlineRecognizerConfig) {
        onlineRecognizer = OnlineRecognizer(assetManager = context.assets, config = config)
    }

    fun initRecognizer(config: OnlineRecognizerConfig) {
        onlineRecognizer = OnlineRecognizer(null, config = config)
    }

    fun destroy() {
        onlineRecognizer.release()
    }

    fun getOnlineRecognizer(): OnlineRecognizer {
        return onlineRecognizer
    }

    fun startRecognizer(sampleRateInHz: Int, buffer: FloatArray?): String {
        ZLog.d(AudioRecordManager.TAG, "processing samples")
        if (buffer != null && buffer.isNotEmpty()) {
            val stream = onlineRecognizer.createStream()
            stream.acceptWaveform(buffer, sampleRateInHz)
            while (onlineRecognizer.isReady(stream)) {
                onlineRecognizer.decode(stream)
                ZLog.d(AudioRecordManager.TAG, onlineRecognizer.getResult(stream).text)
            }
            val result = onlineRecognizer.getResult(stream)
            stream.release()
            return result.text
        }
        return ""
    }
}