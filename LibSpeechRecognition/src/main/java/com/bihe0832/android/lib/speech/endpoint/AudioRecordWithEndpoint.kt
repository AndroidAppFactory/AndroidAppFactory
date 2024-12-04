package com.bihe0832.android.lib.speech.endpoint

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.audio.record.core.AudioChunk
import com.bihe0832.android.lib.audio.wav.WavHeader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizerResult
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
object AudioRecordWithEndpoint {

    private var scene = "AudioRecordWithEndpoint"
    private var samplesBuffer = arrayListOf<ShortArray>()
    private val mASREndpointCheck by lazy { ASREndpointCheck() }

    private var file: String = ""
    private var config: AudioRecordConfig? = null
    private var outputStream: OutputStream? = null

    fun init(context: Context, config: OnlineRecognizerConfig) {
        mASREndpointCheck.init(context, config)
    }

    fun init(config: OnlineRecognizerConfig) {
        mASREndpointCheck.init(config)
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
        file =
            FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
        if (outputStream == null) {
            outputStream = FileOutputStream(file)
        }
    }

    fun startDataRecord(
        callback: (audioRecordConfig: AudioRecordConfig, totalData: ShortArray?, result: OnlineRecognizerResult?) -> Unit,
    ) {
        startDataRecord(null, scene,"") { audioRecordConfig, stepData, result ->
            samplesBuffer.add(stepData.toShorts())
            if (result.isEnd){
                val totalData = samplesBuffer.flatMap { it.asList() }.toShortArray()
                ZLog.d("${AudioRecordManager.TAG} ASRWithEndpoint  processSamples isEndpoint totalData:" + totalData.size)
                samplesBuffer.clear()
                ThreadManager.getInstance().start {
                    callback.invoke(audioRecordConfig, totalData, result.result)
                }
            }
        }
    }

    fun startDataRecord(
        activity: Activity?,
        scene: String,
        notifyContent: String,
        callback: (audioRecordConfig: AudioRecordConfig, stepData: AudioChunk, result: ASREndpointCheck.CheckResult) -> Unit,
    ) {
        ZLog.d("${AudioRecordManager.TAG} AudioRecordWithEndpoint  startDataRecord")
        mASREndpointCheck.startCheck()
        AudioRecordManager.startRecord(
            activity, scene, notifyContent
        ) { audioRecordConfig, audioChunk, ret ->
            config = audioRecordConfig
            try {
                if (audioChunk != null && ret > 0) {
                    val temp = audioChunk.toShorts()
                    callback.invoke(
                        audioRecordConfig,
                        audioChunk,
                        mASREndpointCheck.check(audioRecordConfig.sampleRateInHz, temp, ret)
                    )
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
        ZLog.d("${AudioRecordManager.TAG} AudioRecordWithEndpoint startFileRecord: $fileFolder")
        if (!FileUtils.checkAndCreateFolder(fileFolder)) {
            return
        }
        try {
            newFile(fileFolder)
            startDataRecord(activity, scene, notifyContent) { audioRecordConfig, pcmData, result ->
                outputStream!!.write(pcmData.toBytes()) // 将数据写入文件
                if (result.isEnd){
                    val finalFile = file
                    ZLog.d("${AudioRecordManager.TAG} AudioRecordWithEndpoint  processSamples :$finalFile")
                    ZLog.d("${AudioRecordManager.TAG} AudioRecordWithEndpoint processSamples isEndpoint:" + result.result?.text)
                    writeFile(audioRecordConfig, finalFile)
                    newFile(fileFolder)
                    ThreadManager.getInstance().start {
                        callback.onSuccess(finalFile)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun forceEndCurrent() {
        mASREndpointCheck.forceEndCurrent()
    }

    fun pauseRecord() {
        AudioRecordManager.pauseRecord(scene)
        mASREndpointCheck.endCheck()
    }

    fun resumeRecord() {
        mASREndpointCheck.startCheck()
        AudioRecordManager.resumeRecord(scene)
    }


    fun stopRecord(context: Context) {
        AudioRecordManager.stopRecord(context, scene)
        if (config != null) {
            writeFile(config!!, file)
        }
        mASREndpointCheck.endCheck()
        samplesBuffer.clear()
    }

    fun release() {
        mASREndpointCheck.releaseCheck()
    }
}