package com.bihe0832.android.lib.speech.endpoint

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.audio.wav.WavHeader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizerResult
import com.k2fsa.sherpa.onnx.OnlineStream
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
object ASRWithEndpoint {

    private var scene = "ASRWithEndpoint"
    private var needForceEnding: Boolean = false
    private lateinit var onlineRecognizer: OnlineRecognizer
    private lateinit var stream: OnlineStream

    private var samplesBuffer = arrayListOf<ShortArray>()

    private var file: String = ""
    private var config: AudioRecordConfig? = null
    private var outputStream: OutputStream? = null

    fun init(context: Context, config: OnlineRecognizerConfig) {
        onlineRecognizer = OnlineRecognizer(assetManager = context.assets, config = config)
    }

    fun init(config: OnlineRecognizerConfig) {
        onlineRecognizer = OnlineRecognizer(null, config = config)
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

    private fun writeFile(config: AudioRecordConfig, filePath: String) {
        closeOutput()
        val file = File(filePath)
        ZLog.d(AudioRecordManager.TAG, "$scene writeFile:$scene $file ${file.length()}")
        FileUtils.writeDataToFile(filePath, 0, WavHeader(config, file.length()).toBytes(), false)
        ZLog.d(AudioRecordManager.TAG, "$scene writeFile:$scene $file ${file.length()}")
    }

    private fun newFile(fileFolder: String) {
        file = FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
        if (outputStream == null) {
            outputStream = FileOutputStream(file)
        }
    }

    fun startDataRecord(
        callback: (audioRecordConfig: AudioRecordConfig, totalData: ShortArray?, result: OnlineRecognizerResult) -> Unit,
    ) {
        startDataRecord(null, "", callback)
    }

    fun startDataRecord(
        activity: Activity?,
        notifyContent: String,
        callback: (audioRecordConfig: AudioRecordConfig, totalData: ShortArray?, result: OnlineRecognizerResult) -> Unit,
    ) {
        ZLog.d("${AudioRecordManager.TAG} ASRWithEndpoint  startDataRecord")
        stream = onlineRecognizer.createStream()
        AudioRecordManager.startRecord(activity, scene, notifyContent) { audioRecordConfig, audioChunk, ret ->
            try {
                if (audioChunk != null && ret > 0) {
                    val temp = audioChunk.toShorts()
                    samplesBuffer.add(temp)
                    val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(temp, ret)
                    stream.acceptWaveform(samples, sampleRate = audioRecordConfig.sampleRateInHz)
                    while (onlineRecognizer.isReady(stream)) {
                        onlineRecognizer.decode(stream)
                    }
                    if (needForceEnding || onlineRecognizer.isEndpoint(stream)) {
                        ZLog.d("${AudioRecordManager.TAG} ASRWithEndpoint isEndpoint, needForceEnding:$needForceEnding")
                        ZLog.d(
                            "${AudioRecordManager.TAG} ASRWithEndpoint processSamples isEndpoint:" + onlineRecognizer.getResult(
                                stream
                            ).text
                        )
                        if (needForceEnding) {
                            needForceEnding = false
                        }
                        val totalData = samplesBuffer.flatMap { it.asList() }.toShortArray()
                        ZLog.d("${AudioRecordManager.TAG} ASRWithEndpoint  processSamples isEndpoint totalData:" + totalData.size)
                        samplesBuffer.clear()
                        ThreadManager.getInstance().start {
                            callback.invoke(audioRecordConfig, totalData, onlineRecognizer.getResult(stream))
                        }
                        onlineRecognizer.reset(stream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun startFileRecord(
        fileFolder: String,
        callback: AAFDataCallback<String>,
    ) {
        startFileRecord(null, "", fileFolder, callback)
    }

    fun startFileRecord(
        fileFolder: String,
        max: Short,
        callback: AAFDataCallback<String>,
    ) {
        startFileRecord(null, "", fileFolder, max, callback)
    }

    fun startFileRecord(
        activity: Activity?,
        notifyContent: String,
        fileFolder: String,
        max: Short,
        callback: AAFDataCallback<String>,
    ) {
        startFileRecord(activity, notifyContent, fileFolder, object : AAFDataCallback<String>() {
            override fun onSuccess(result: String?) {
                ZLog.d(AudioRecordManager.TAG, "record data:${result}")
                result?.let {
                    SherpaAudioConvertTools.readWavAudioToSherpaArray(result)?.let {
                        if (SherpaAudioConvertTools.isOverSilence(it, max)) {
                            callback.onSuccess(result)
                        } else {
                            ZLog.d(AudioRecordManager.TAG, "无效音频，无有效内容:$result")
                            callback.onError(-1, "无效音频，无有效内容:$result")
                            FileUtils.deleteFile(result)
                        }
                    }
                }
            }
        })
    }

    @Synchronized
    fun startFileRecord(
        activity: Activity?,
        notifyContent: String,
        fileFolder: String,
        callback: AAFDataCallback<String>,
    ) {
        ZLog.d("${AudioRecordManager.TAG} ASRWithEndpoint startFileRecord: $fileFolder")
        if (!FileUtils.checkAndCreateFolder(fileFolder)) {
            return
        }
        try {
            newFile(fileFolder)
            stream = onlineRecognizer.createStream()
            AudioRecordManager.startRecord(
                activity, scene, notifyContent
            ) { audioRecordConfig, audioChunk, ret ->
                try {
                    if (audioChunk != null && ret > 0) {
                        config = audioRecordConfig
                        outputStream!!.write(audioChunk.toBytes()) // 将数据写入文件
                        val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(audioChunk.toShorts(), ret)
                        stream.acceptWaveform(
                            samples, sampleRate = audioRecordConfig.sampleRateInHz
                        )
                        while (onlineRecognizer.isReady(stream)) {
                            onlineRecognizer.decode(stream)
                        }
                        if (needForceEnding || onlineRecognizer.isEndpoint(stream)) {
                            val finalFile = file
                            ZLog.d("${AudioRecordManager.TAG} ASRWithEndpoint isEndpoint, needForceEnding:$needForceEnding")
                            ZLog.d("${AudioRecordManager.TAG} ASRWithEndpoint  processSamples :$finalFile")
                            ZLog.d(
                                "${AudioRecordManager.TAG} ASRWithEndpoint processSamples isEndpoint:" + onlineRecognizer.getResult(
                                    stream
                                ).text
                            )
                            if (needForceEnding) {
                                needForceEnding = false
                            }
                            writeFile(audioRecordConfig, finalFile)
                            newFile(fileFolder)
                            ThreadManager.getInstance().start {
                                callback.onSuccess(finalFile)
                            }
                            onlineRecognizer.reset(stream)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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


    fun stopRecord(context: Context) {
        AudioRecordManager.stopRecord(context, scene)
        if (config != null) {
            writeFile(config!!, file)
        }
        stream.release()
        samplesBuffer.clear()
    }

    fun release() {
        onlineRecognizer.release()
    }
}