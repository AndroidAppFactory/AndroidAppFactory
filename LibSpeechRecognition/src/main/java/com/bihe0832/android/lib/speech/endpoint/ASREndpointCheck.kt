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
    private var onlineRecognizer: OnlineRecognizer? = null
    private var stream: OnlineStream? = null
    private var isReady = false
    private var isChecking = false


    fun init(context: Context, config: OnlineRecognizerConfig) {
        try {
            onlineRecognizer = OnlineRecognizer(assetManager = context.assets, config = config)
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            onlineRecognizer = null
        }
    }

    fun init(config: OnlineRecognizerConfig) {
        try {
            onlineRecognizer = OnlineRecognizer(null, config = config)
            isReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            onlineRecognizer = null
        }
    }

    class CheckResult(val isEnd: Boolean, val result: OnlineRecognizerResult?)

    @Synchronized
    fun check(
        sampleRateInHz: Int, buffer: ShortArray?, size: Int, autoReset: Boolean
    ): CheckResult {
        try {
            if (onlineRecognizer != null && stream != null && isReady && isChecking && buffer != null) {
                val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(buffer, size)
                stream!!.acceptWaveform(samples, sampleRate = sampleRateInHz)
                while (onlineRecognizer!!.isReady(stream!!)) {
                    onlineRecognizer!!.decode(stream!!)
                }
                if (needForceEnding || onlineRecognizer!!.isEndpoint(stream!!)) {
                    ZLog.d("${AudioRecordManager.TAG} AudioRecordWithEndpoint isEndpoint, needForceEnding:$needForceEnding")
                    ZLog.d(
                        "${AudioRecordManager.TAG} AudioRecordWithEndpoint processSamples isEndpoint:" + onlineRecognizer!!.getResult(
                            stream!!
                        ).text
                    )
                    if (needForceEnding) {
                        needForceEnding = false
                    }
                    val result = onlineRecognizer!!.getResult(stream!!)
                    if (autoReset) {
                        onlineRecognizer!!.reset(stream!!)
                    }
                    return CheckResult(true, result)
                } else {
                    return CheckResult(false, onlineRecognizer!!.getResult(stream!!))
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

    @Synchronized
    fun startCheck(): Boolean {
        try {
            if (isReady && onlineRecognizer != null) {
                stream = onlineRecognizer!!.createStream()
                isChecking = true
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            stream = null
        }
        return false
    }

    @Synchronized
    fun endCheck() {
        isChecking = false
        stream?.let {
            onlineRecognizer?.reset(it)
            it.release()
        }
        stream = null
    }

    @Synchronized
    fun releaseCheck() {
        isReady = false
        onlineRecognizer?.release()
        onlineRecognizer = null

    }
}