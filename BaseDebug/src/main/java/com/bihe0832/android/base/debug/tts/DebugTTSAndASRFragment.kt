/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.tts

import android.media.AudioFormat
import android.media.MediaRecorder
import android.util.Log
import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.audio.wav.AudioUtils
import com.bihe0832.android.lib.audio.wav.PcmToWav
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.speech.kws.KeywordSpotterManager
import com.bihe0832.android.lib.speech.recognition.ASRManager
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools
import java.io.File

class DebugTTSAndASRFragment : DebugEnvFragment() {
    val TAG = this.javaClass.simpleName
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRateInHz = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO

    // Note: We don't use AudioFormat.ENCODING_PCM_FLOAT
    // since the AudioRecord.read(float[]) needs API level >= 23
    // but we are targeting API level >= 21
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val mASRManager by lazy { ASRManager() }
    private val mKeywordSpotterManager by lazy { KeywordSpotterManager() }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("TTS 调试", DebugTTSFragment::class.java))
            add(
                DebugItemData("清空缓存",
                    View.OnClickListener { FileUtils.deleteDirectory(File(AAFFileWrapper.getMediaTempFolder())) })
            )
            add(DebugItemData("ARS 初始化", View.OnClickListener { init() }))
            add(DebugItemData("ARS 开始实时直接识别", View.OnClickListener { startReal() }))
            add(DebugItemData("ARS 实时立刻打断", View.OnClickListener { forceEndCurrent() }))
            add(DebugItemData("ARS 开始实时识别后读取部分区间", View.OnClickListener { testSplit() }))
            add(DebugItemData("ARS 开始实时基于文件识别", View.OnClickListener { startRealFile() }))
            add(DebugItemData("ARS 结束实时识别", View.OnClickListener { stop() }))
            add(DebugItemData("ARS 开始文件识别", View.OnClickListener { startFile() }))
        }
    }

    override fun initView(view: View) {
        super.initView(view)
    }

    fun init() {
        AudioRecordManager.init(context!!, sampleRateInHz, 2.4f, 1.4f, 30f)
        mASRManager.initRecognizer(context!!, "sherpa-onnx-paraformer-zh-2023-09-14", sampleRateInHz)
        mKeywordSpotterManager.initRecognizer(
            context!!, "sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01", "keywords.txt", sampleRateInHz
        )
    }

    fun startReal() {
        AudioRecordManager.startRecord(audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            object : AAFDataCallback<ShortArray>() {
                override fun onSuccess(result: ShortArray?) {
                    result?.let { data ->
                        Log.i(TAG, "====================")
                        val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(data, data.size)
                        recognise(samples)
                        AudioUtils.shortArrayToByteArray(data).let {
                            SherpaAudioConvertTools.byteArrayToSherpaArray(it).let { float ->
                                recognise(float)
                            }

                            AudioUtils.byteArrayToShortArray(it, it.size).let { short ->
                                val erer = SherpaAudioConvertTools.shortArrayToSherpaArray(short, short.size)
                                recognise(erer)
                            }
                        }
                        Log.i(TAG, "====================")
                    }
                }
            })
    }

    var lastArray: FloatArray? = null
    fun startRealFile() {
        AudioRecordManager.startRecord(audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            AAFFileWrapper.getMediaTempFolder(),
            object : AAFDataCallback<String>() {
                override fun onSuccess(result: String?) {
                    ZLog.d(TAG, "record data:${result}")
                    SherpaAudioConvertTools.readWavAudioToSherpaArray(result)?.let {
                        val combinedFloatArray = concatenate(lastArray, it)
                        recognise(combinedFloatArray)
                        lastArray = it
                    }
                }
            })
    }

    fun concatenate(first: FloatArray?, second: FloatArray?): FloatArray? {
        val result = FloatArray((first?.size ?: 0) + (second?.size ?: 0))
        first?.copyInto(result, 0)
        second?.copyInto(result, first?.size ?: 0)
        return result
    }


    fun stop() {
        AudioRecordManager.stopRecord()
    }


    fun forceEndCurrent() {
        AudioRecordManager.forceEndCurrent().let {
            ZLog.d(TAG, "forceEndCurrent:$it")
        }
    }

    fun testSplit() {
        AudioRecordManager.startRecord(audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            object : AAFDataCallback<ShortArray>() {
                override fun onSuccess(pcmData: ShortArray?) {
                    ZLog.d(TAG, "Started recording callback:${pcmData?.size}")
                    val fileFolder = AAFFileWrapper.getMediaTempFolder()
                    pcmData?.let {
                        if (FileUtils.checkAndCreateFolder(fileFolder)) {
                            val file =
                                FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
                            ZLog.d(TAG, "record data:${file}")
                            val byteArray = AudioUtils.shortArrayToByteArray(pcmData)
                            recognise(SherpaAudioConvertTools.byteArrayToSherpaArray(byteArray))
                            PcmToWav(
                                sampleRateInHz,
                                channelConfig,
                                audioFormat
                            ).convertToFile(byteArray, file)

                            val length = it.size * 1.0f / sampleRateInHz
                            if (length > 3.2) {
                                val shortArray = ShortArray((sampleRateInHz * 3.2).toInt() + sampleRateInHz)
                                System.arraycopy(
                                    it,
                                    it.size - (sampleRateInHz * 3.2).toInt(),
                                    shortArray,
                                    sampleRateInHz,
                                    (sampleRateInHz * 3.2).toInt()
                                )
                                val file2 =
                                    FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
                                val byteArray2 = AudioUtils.shortArrayToByteArray(shortArray)
                                recognise(SherpaAudioConvertTools.byteArrayToSherpaArray(byteArray2))
                                PcmToWav(
                                    sampleRateInHz,
                                    channelConfig,
                                    audioFormat
                                ).convertToFile(byteArray2, file2)
                                ZLog.d(TAG, "record data:${file2}")
                            }

                        }
                    }
                }
            })
    }

    fun recognise(data: FloatArray?) {
        data?.let { samples ->
            Log.i(TAG, "record data:${samples.size}")
            mKeywordSpotterManager.doRecognizer(sampleRateInHz, samples).let {
                Log.i(TAG, "mKeywordSpotterManager Start to recognizer:$it")
            }
            mASRManager.startRecognizer(sampleRateInHz, samples).let {
                Log.i(TAG, "mRecognizerManager Start to recognizer:$it")
            }
        }
    }

    fun startFile() {
        val file = AAFFileWrapper.getMediaTempFolder() + "0.wav"
        FileUtils.copyAssetsFileToPath(context!!, "0.wav", file)
        SherpaAudioConvertTools.readWavAudioToSherpaArray(file)?.let {
            recognise(it)
        }
    }
}
