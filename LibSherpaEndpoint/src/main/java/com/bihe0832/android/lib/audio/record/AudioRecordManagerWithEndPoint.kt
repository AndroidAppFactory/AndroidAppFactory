package com.bihe0832.android.lib.audio.record

import android.content.Context
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.record.common.AudioChunk
import com.bihe0832.android.lib.audio.wav.WavHeader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.EndpointRule
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
object AudioRecordManagerWithEndPoint {

    private var scene = "AudioRecordFileWithEndPoint"
    private var needForceEnding: Boolean = false
    private lateinit var onlineRecognizer: OnlineRecognizer
    private lateinit var stream: OnlineStream

    private var samplesBuffer = arrayListOf<ShortArray>()

    private var file: String = ""
    private var config: AudioRecordConfig? = null
    private var outputStream: OutputStream? = null

    fun init(
        context: Context,
        sampleRateInHz: Int,
        minTrailingForNone: Float,
        minTrailingForBetween: Float,
        maxUtteranceLength: Float,
    ) {
        val modelAssetsDir = "sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23"
        val config = OnlineRecognizerConfig(
            featConfig = FeatureConfig(
                sampleRate = sampleRateInHz, featureDim = 80
            ),
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

    fun closeOutput() {
        if (outputStream != null) {
            try {
                outputStream!!.flush()
                outputStream!!.close()
                outputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun writeFile(config: AudioRecordConfig, file: String) {
        closeOutput()
        if (FileUtils.checkFileExist(file)) {
            FileUtils.writeDataToFile(file, 0, WavHeader(config, File(file).length()).toBytes())
        }
    }

    private fun newFile(fileFolder: String) {
        this.file = FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
        if (outputStream == null) {
            outputStream = FileOutputStream(file)
        }
    }

    fun startDataRecord(callback: (audioRecordConfig: AudioRecordConfig, totalData: ShortArray?) -> Unit): Boolean {
        ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint  startDataRecord")
        stream = onlineRecognizer.createStream()
        return AudioRecordManager.startRecord(scene, object : AudioChunk.OnAudioChunkPulledListener {
            override fun onAudioChunkPulled(audioRecordConfig: AudioRecordConfig, audioChunk: AudioChunk?, ret: Int) {
                if (audioChunk != null && ret > 0) {
                    val temp = audioChunk.toShorts()
                    samplesBuffer.add(temp)
                    if (needForceEnding) {
                        needForceEnding = false
                        onlineRecognizer.reset(stream)
                        val totalData = samplesBuffer.flatMap { it.asList() }.toShortArray()
                        ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint  processSamples isEndpoint totalData:" + totalData.size)
                        samplesBuffer.clear()
                        ThreadManager.getInstance().start {
                            callback.invoke(audioRecordConfig, totalData)
                        }
                    } else {
                        val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(audioChunk.toShorts(), ret)
                        stream.acceptWaveform(
                            samples, sampleRate = audioRecordConfig.sampleRateInHz
                        )
                        while (onlineRecognizer.isReady(stream)) {
                            onlineRecognizer.decode(stream)
                        }
                        val isEndpoint = onlineRecognizer.isEndpoint(stream)
                        if (isEndpoint) {
                            ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint processSamples isEndpoint")
                            ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint processSamples isEndpoint:" + onlineRecognizer.getResult(stream).text)
                            onlineRecognizer.reset(stream)
                            val totalData = samplesBuffer.flatMap { it.asList() }.toShortArray()
                            ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint processSamples isEndpoint totalData:" + totalData.size)
                            samplesBuffer.clear()
                            ThreadManager.getInstance().start {
                                callback.invoke(audioRecordConfig, totalData)
                            }
                        }
                    }
                }
            }

        })
    }

    fun startFileRecord(fileFolder: String, callback: AAFDataCallback<String>): Boolean {
        ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint startFileRecord: $fileFolder")
        try {
            if (!FileUtils.checkAndCreateFolder(fileFolder)) {
                return false
            }
            newFile(fileFolder)
            stream = onlineRecognizer.createStream()
            return AudioRecordManager.startRecord(scene, object : AudioChunk.OnAudioChunkPulledListener {
                override fun onAudioChunkPulled(
                    audioRecordConfig: AudioRecordConfig,
                    audioChunk: AudioChunk?,
                    ret: Int,
                ) {
                    if (audioChunk != null && ret > 0) {
                        config = audioRecordConfig
                        outputStream!!.write(audioChunk.toBytes()) // 将数据写入文件
                        if (needForceEnding) {
                            needForceEnding = false
                            val finalFile = file
                            writeFile(audioRecordConfig, finalFile)
                            newFile(fileFolder)
                            ThreadManager.getInstance().start {
                                callback.onSuccess(finalFile)
                            }
                            onlineRecognizer.reset(stream)
                        } else {
                            val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(audioChunk.toShorts(), ret)
                            stream.acceptWaveform(
                                samples, sampleRate = audioRecordConfig.sampleRateInHz
                            )
                            while (onlineRecognizer.isReady(stream)) {
                                onlineRecognizer.decode(stream)
                            }
                            val isEndpoint = onlineRecognizer.isEndpoint(stream)
                            if (isEndpoint) {
                                ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint  processSamples isEndpoint")
                                ZLog.d("${AudioRecordManager.TAG} AudioRecordManagerWithEndPoint processSamples isEndpoint:" + onlineRecognizer.getResult(stream).text)
                                val finalFile = file
                                writeFile(audioRecordConfig, finalFile)
                                newFile(fileFolder)
                                ThreadManager.getInstance().start {
                                    callback.onSuccess(finalFile)
                                }
                                onlineRecognizer.reset(stream)

                            }
                        }
                    }
                }

            })
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun forceEndCurrent() {
        needForceEnding = true
    }

    fun pauseRecord() {
        AudioRecordManager.pauseRecord(scene)
        stream.release()
    }

    fun resumeRecord() {
        AudioRecordManager.resumeRecord(scene)
    }


    fun stopRecord() {
        AudioRecordManager.stopRecord(scene)
        if (config != null) {
            writeFile(config!!, file)
        }
        stream.release()
        samplesBuffer.clear()
        onlineRecognizer.release()
    }
}