package com.bihe0832.android.lib.speech.endpoint

import android.content.Context
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.log.ZLog
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizerResult
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
class ASREndpointCheck {

    private var needForceEnding: Boolean = false
    private lateinit var onlineRecognizer: OnlineRecognizer
    private lateinit var stream: OnlineStream

    fun init(context: Context, config: OnlineRecognizerConfig) {
        onlineRecognizer = OnlineRecognizer(assetManager = context.assets, config = config)
    }

    fun init(config: OnlineRecognizerConfig) {
        onlineRecognizer = OnlineRecognizer(null, config = config)
    }

    class CheckResult(val isEnd: Boolean, val result: OnlineRecognizerResult?)

    fun check(sampleRateInHz: Int, buffer: ShortArray?, size: Int): CheckResult {
        try {
            if (buffer != null) {
                val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(buffer, size)
                stream.acceptWaveform(samples, sampleRate = sampleRateInHz)
                while (onlineRecognizer.isReady(stream)) {
                    onlineRecognizer.decode(stream)
                }
                if (needForceEnding || onlineRecognizer.isEndpoint(stream)) {
                    ZLog.d("${AudioRecordManager.TAG} AudioRecordWithEndpoint isEndpoint, needForceEnding:$needForceEnding")
                    ZLog.d(
                        "${AudioRecordManager.TAG} AudioRecordWithEndpoint processSamples isEndpoint:" + onlineRecognizer.getResult(
                            stream
                        ).text
                    )
                    if (needForceEnding) {
                        needForceEnding = false
                    }
                    val result = onlineRecognizer.getResult(stream)
                    onlineRecognizer.reset(stream)
                    return CheckResult(true, result)
                } else {
                    return CheckResult(false, onlineRecognizer.getResult(stream))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return CheckResult(false, null)
    }

    fun forceEndCurrent() {
        needForceEnding = true
    }

    fun startCheck() {
        stream = onlineRecognizer.createStream()
    }

    fun endCheck() {
        onlineRecognizer.reset(stream)
        stream.release()
    }

    fun releaseCheck() {
        onlineRecognizer.release()

    }
}