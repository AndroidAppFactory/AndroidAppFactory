/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.audio

import android.annotation.SuppressLint
import android.view.View
import com.bihe0832.android.common.debug.audio.DebugWAVListFragment
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.AudioUtils
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.audio.record.wrapper.AAFAudioTools
import com.bihe0832.android.lib.audio.record.wrapper.AudioRecordFile
import com.bihe0832.android.lib.audio.wav.PcmToWav
import com.bihe0832.android.lib.audio.wav.WavHeader
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.pinyin.FilePinyinMapDict
import com.bihe0832.android.lib.pinyin.PinYinTools
import com.bihe0832.android.lib.speech.DEFAULT_ENDPOINT_MODEL_DIR
import com.bihe0832.android.lib.speech.endpoint.ASRWithEndpoint
import com.bihe0832.android.lib.speech.getDefaultOnlineRecognizerConfig
import java.io.File

class DebugRecordAndASRFragRecment : DebugEnvFragment() {
    val TAG = this.javaClass.simpleName
    private val mAudioRecordFile = mutableListOf<AudioRecordFile>()
    private val scene = "debugRecord"
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("文字转拼音", View.OnClickListener { testPinyin() }))
            add(
                getDebugItem("指定文件 WAV头 信息查看",
                    View.OnClickListener { readWavHead(preFile()) })
            )
            add(
                getDebugItem("空文件 WAV头 信息查看",
                    View.OnClickListener { readWavHead(preEmpty()) })
            )
            add(
                getDebugFragmentItemData("WAV查看", DebugWAVListFragment::class.java)
            )
            add(
                getDebugItem("ASR及语音转文字", View.OnClickListener {
                    showInfo(
                        "ASR及语音转文字",
                        "ASR及语音转文字，已独立为单独项目，请参考：https://github.com/AndroidAppFactory/AAFASR"
                    )
                })
            )

            add(getDebugItem("初始化", View.OnClickListener { init() }))
            add(
                getDebugItem("ARS 静音检测：结束实时识别及录制",
                    View.OnClickListener { ASRWithEndpoint.stopRecord(context!!) })
            )
            add(getDebugItem("ARS 实时打断", View.OnClickListener { forceEndCurrent() }))
            add(
                getDebugItem("ARS 静音检测：开启录制在回调保存为文件",
                    View.OnClickListener { startRecord() })
            )
            add(
                getDebugItem("ARS 静音检测：开启录制在回调识别部分文区间",
                    View.OnClickListener { testSplit() })
            )
            add(getDebugItem("WAV 录制测试：开始录制", View.OnClickListener { startWav() }))
            add(getDebugItem("WAV 录制测试：结束录制", View.OnClickListener { stopWav() }))
            add(
                getDebugItem("WAV 录制测试：录制文件开始（可同时多个）",
                    View.OnClickListener { startWaveFile() })
            )
            add(
                getDebugItem("WAV 录制测试：录制文件结束（关闭所有）",
                    View.OnClickListener { stopWaveFile() })
            )

            add(getDebugItem(
                "清空本地音频临时缓存"
            ) { FileUtils.deleteDirectory(File(AAFFileWrapper.getMediaTempFolder())) })

        }
    }

    private fun testPinyin() {
        mutableListOf("阿珂没有闪现", "妲己没闪现", "大司命没有技能").forEach {
            ZLog.d(it + " Convert to Pinyin：" + PinYinTools.toPinyin(it, "").toLowerCase())
            ZLog.d(it + " Convert to Pinyin：" + PinYinTools.toPinyin(it, " ").toLowerCase())
            ZLog.d(it + " Convert to Pinyin：" + PinYinTools.toPinyin(it, ",").toLowerCase())
            ZLog.d(it + " Convert to Pinyin：" + PinYinTools.toPinyin(it, "-").toLowerCase())
        }
    }

    @SuppressLint("MissingPermission")
    fun init() {
        val file = AAFFileWrapper.getTempFolder() + "dict.txt"
        FileUtils.copyAssetsFileToPath(context, "cncity.txt", file)
        PinYinTools.init(
            PinYinTools.newConfig().with(
                FilePinyinMapDict(
                    context!!, file
                )
            )
        )
        AAFAudioTools.addRecordScene(scene, "读取麦克风", "音频录制测试")
        AAFAudioTools.startRecordPermissionCheck(activity,
            scene,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    AAFAudioTools.init()
                    ASRWithEndpoint.init(
                        context!!, getDefaultOnlineRecognizerConfig(
                            AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, DEFAULT_ENDPOINT_MODEL_DIR
                        )
                    )
                }
            })
    }

    fun startWav() {
        AAFAudioTools.startRecordPermissionCheck(activity,
            scene,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    AudioRecordManager.startRecord(
                        activity, scene, "rere"
                    ) { config, audioChunk, dataLength ->
                        ZLog.d(
                            TAG, "Started recording callback:${dataLength}"
                        )
                    }
                }
            })
    }

    fun stopWav() {
        AudioRecordManager.stopRecord(context!!, scene)
    }

    fun startWaveFile() {
        val file = AAFFileWrapper.getMediaTempFolder() + System.currentTimeMillis() + ".wav"
        AAFAudioTools.startRecordPermissionCheck(activity,
            scene,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    AudioRecordFile(System.currentTimeMillis().toString(), File(file)).let {
                        mAudioRecordFile.add(it)
                        it.startRecord(activity, "特惠")
                    }
                }
            })
    }

    fun stopWaveFile() {
        mAudioRecordFile.forEach {
            it.stopRecord(context!!)
        }
    }

    fun preFile(): String {
        val file = AAFFileWrapper.getMediaTempFolder() + "temp.wav"
        FileUtils.copyAssetsFileToPath(context!!, "0.wav", file)
        return file
    }

    fun preEmpty(): String {
        val file = AAFFileWrapper.getMediaTempFolder() + "temp.wav"
        File(file).let {
            FileUtils.deleteFile(file)
            it.createNewFile()
            FileUtils.writeDataToFile(
                file, 0, WavHeader(
                    AudioRecordConfig(
                        AudioRecordConfig.DEFAULT_AUDIO_SOURCE,
                        AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ,
                        AudioRecordConfig.DEFAULT_CHANNEL_CONFIG,
                        AudioRecordConfig.DEFAULT_AUDIO_FORMAT
                    ), it.length()
                ).toBytes(), false
            )
        }
        return file
    }

    fun readWavHead(filePath: String) {
        val wavFileReader = WaveFileReader(filePath)
        ZLog.d(wavFileReader.toString())
    }

    fun startRecord() {
        AAFAudioTools.startRecordPermissionCheck(activity,
            scene,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    ASRWithEndpoint.startDataRecord() { audioRecordConfig, pcmData, result ->
                        ZLog.d(TAG, "Started recording callback:${pcmData?.size}")
                        val fileFolder = AAFFileWrapper.getMediaTempFolder()
                        pcmData?.let {
                            if (FileUtils.checkAndCreateFolder(fileFolder)) {
                                val file =
                                    FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
                                ZLog.d(TAG, "record data:${file}")
                                val byteArray = AudioUtils.shortArrayToByteArray(pcmData)
                                PcmToWav(
                                    audioRecordConfig.sampleRateInHz,
                                    audioRecordConfig.channelConfig,
                                    audioRecordConfig.audioFormat
                                ).convertToFile(byteArray, file)
                                ZLog.d(TAG, "record data:${file}")
                            }
                        }
                    }
                }
            })
    }

    fun forceEndCurrent() {
        ASRWithEndpoint.forceEndCurrent().let {
            ZLog.d(TAG, "forceEndCurrent:$it")
        }
    }

    fun testSplit() {
        ASRWithEndpoint.startDataRecord { audioRecordConfig, pcmData, result ->
            ZLog.d(TAG, "Started recording callback:${pcmData?.size}")
            val fileFolder = AAFFileWrapper.getMediaTempFolder()
            pcmData?.let {
                if (FileUtils.checkAndCreateFolder(fileFolder)) {
                    val file =
                        FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
                    ZLog.d(TAG, "record data:${file}")
                    val byteArray = AudioUtils.shortArrayToByteArray(pcmData)
                    PcmToWav(
                        audioRecordConfig.sampleRateInHz,
                        audioRecordConfig.channelConfig,
                        audioRecordConfig.audioFormat
                    ).convertToFile(byteArray, file)

                    val length = it.size * 1.0f / audioRecordConfig.sampleRateInHz
                    if (length > 3.2) {
                        val shortArray =
                            ShortArray((audioRecordConfig.sampleRateInHz * 3.2).toInt() + audioRecordConfig.sampleRateInHz)
                        System.arraycopy(
                            it,
                            it.size - (audioRecordConfig.sampleRateInHz * 3.2).toInt(),
                            shortArray,
                            audioRecordConfig.sampleRateInHz,
                            (audioRecordConfig.sampleRateInHz * 3.2).toInt()
                        )
                        val file2 =
                            FileUtils.getFolderPathWithSeparator(fileFolder) + System.currentTimeMillis() + ".wav"
                        val byteArray2 = AudioUtils.shortArrayToByteArray(shortArray)
                        PcmToWav(
                            audioRecordConfig.sampleRateInHz,
                            audioRecordConfig.channelConfig,
                            audioRecordConfig.audioFormat
                        ).convertToFile(byteArray2, file2)
                        ZLog.d(TAG, "record data:${file2}")
                    }

                }
            }
        }
    }


    fun concatenate(first: FloatArray?, second: FloatArray?): FloatArray {
        val result = FloatArray((first?.size ?: 0) + (second?.size ?: 0))
        first?.copyInto(result, 0)
        second?.copyInto(result, first?.size ?: 0)
        return result
    }


}


