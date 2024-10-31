package com.bihe0832.android.lib.speech.recognition

import android.content.Context
import android.util.Log
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

    private lateinit var offlineRecognizer: OfflineRecognizer

    fun initRecognizer(context: Context, config: OfflineRecognizerConfig) {
        ZLog.d(AudioRecordManager.TAG,  "initRecognizer start")
        offlineRecognizer = OfflineRecognizer(
            assetManager = context.assets,
            config = config,
        )
        ZLog.d(AudioRecordManager.TAG,  "initRecognizer end")
    }

    fun initRecognizer(config: OfflineRecognizerConfig) {
        ZLog.d(AudioRecordManager.TAG,  "initRecognizer start")
        offlineRecognizer = OfflineRecognizer(
            null,
            config = config,
        )
        ZLog.d(AudioRecordManager.TAG,  "initRecognizer end")
    }

    fun startRecognizer(sampleRateInHz: Int, buffer: FloatArray?): String {
        ZLog.d(AudioRecordManager.TAG,  "processing samples")
        if (buffer != null && buffer.isNotEmpty()) {
            val stream = offlineRecognizer.createStream()
            stream.acceptWaveform(buffer, sampleRateInHz)
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