package com.bihe0832.android.lib.voice.record

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioRecord
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.voice.record.utils.PcmToWav
import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.EndpointRule
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools
import kotlin.concurrent.thread

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
object AudioRecordManager {

    private const val TAG = "AudioRecordManager"

    private lateinit var onlineRecognizer: OnlineRecognizer
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var samplesBuffer = arrayListOf<ShortArray>()

    @Volatile
    private var isRecording: Boolean = false

    fun init(
        context: Context,
        sampleRateInHz: Int,
        minTrailingForNone: Float,
        minTrailingForBetween: Float,
        maxUtteranceLength: Float,
    ) {
        val modelAssetsDir = "sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23"
        val config = OnlineRecognizerConfig(
            featConfig = FeatureConfig(sampleRate = sampleRateInHz, featureDim = 80),
            modelConfig = OnlineModelConfig(
                transducer = OnlineTransducerModelConfig(
                    encoder = "$modelAssetsDir/encoder-epoch-99-avg-1.int8.onnx",
                    decoder = "$modelAssetsDir/decoder-epoch-99-avg-1.onnx",
                    joiner = "$modelAssetsDir/joiner-epoch-99-avg-1.int8.onnx",
                ),
                tokens = "$modelAssetsDir/tokens.txt",
                modelType = "zipformer",
            ),
            endpointConfig = EndpointConfig(
                rule1 = EndpointRule(false, minTrailingForNone, 0.0f),
                rule2 = EndpointRule(true, minTrailingForBetween, 0.0f),
                rule3 = EndpointRule(false, 0.0f, maxUtteranceLength)
            ),
            enableEndpoint = true,
        )
        onlineRecognizer = OnlineRecognizer(assetManager = context.assets, config = config)
    }

    private fun splitVoiceByEndpoint(sampleRateInHz: Int, callback: AAFDataCallback<ShortArray>) {
        ZLog.d(TAG, "splitVoiceByEndpoint")
        val stream = onlineRecognizer.createStream()
        val interval = 0.1
        val bufferSize = (interval * sampleRateInHz).toInt()
        val buffer = ShortArray(bufferSize)
        while (isRecording) {
            val ret = audioRecord?.read(buffer, 0, buffer.size)
            if (ret != null && ret > 0) {
                val temp = ShortArray(ret)
                System.arraycopy(buffer, 0, temp, 0, ret)
                samplesBuffer.add(temp)
                val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(buffer, ret)
                stream.acceptWaveform(samples, sampleRate = sampleRateInHz)
                while (onlineRecognizer.isReady(stream)) {
                    onlineRecognizer.decode(stream)
                }
                val isEndpoint = onlineRecognizer.isEndpoint(stream)
                if (isEndpoint) {
                    ZLog.d(TAG, "processSamples isEndpoint")
                    onlineRecognizer.reset(stream)
                    val totalData = samplesBuffer.flatMap { it.asList() }.toShortArray()
                    ZLog.d(TAG, "processSamples isEndpoint totalData:" + totalData.size)
                    samplesBuffer.clear()
                    ThreadManager.getInstance().start {
                        callback.onSuccess(totalData)
                    }
                }
            }
        }
        stream.release()
    }

    fun stopRecord() {
        ZLog.d(TAG, "Stopped recording start")
        isRecording = false
        audioRecord?.let {
            it.stop()
            it.release()
        }
        audioRecord = null
        ZLog.d(TAG, "Stopped recording end")
    }

    fun destroy() {
        samplesBuffer.clear()
        stopRecord()
        onlineRecognizer.release()
    }

    @SuppressLint("MissingPermission")
    fun startRecord(
        audioSource: Int,
        sampleRateInHz: Int,
        channelConfig: Int,
        audioFormat: Int,
        callback: AAFDataCallback<ShortArray>,
    ): Boolean {
        ZLog.d(TAG, "Started recording call")
        if (isRecording) {
            ZLog.d(TAG, "recording has start before")
            return false
        }
        val numBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        audioRecord = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, numBytes * 2)
        audioRecord?.let {
            ZLog.d(TAG, "state: ${it.state}")
            it.startRecording()
            isRecording = true
            recordingThread = thread(true) {
                splitVoiceByEndpoint(sampleRateInHz, callback)
            }
            ZLog.d(TAG, "Start recording")
            return true
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun startRecord(
        audioSource: Int,
        sampleRateInHz: Int,
        channelConfig: Int,
        audioFormat: Int,
        fileFolder: String,
        callback: AAFDataCallback<String>,
    ): Boolean {
        return startRecord(audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            object : AAFDataCallback<ShortArray>() {
                override fun onSuccess(pcmData: ShortArray?) {
                    ZLog.d(TAG, "Started recording callback:${pcmData?.size}")
                    pcmData?.let {
                        if (FileUtils.checkAndCreateFolder(fileFolder)) {
                            val file =
                                FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
                            val byteArray = SherpaAudioConvertTools.shortArrayToByteArray(pcmData)
                            PcmToWav(sampleRateInHz, channelConfig, audioFormat).convert(byteArray, file)
                            callback.onSuccess(file)
                        } else {
                            callback.onError(-1, "File folder is bad")
                        }
                    }
                }
            })
    }
}