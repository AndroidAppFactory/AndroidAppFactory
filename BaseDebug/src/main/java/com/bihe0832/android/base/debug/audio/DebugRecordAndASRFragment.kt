/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.audio

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import com.bihe0832.android.base.debug.audio.asr.ASRModelDownloadManager.checkAndDoAction
import com.bihe0832.android.base.debug.audio.asr.SCENE
import com.bihe0832.android.base.debug.audio.asr.getASRModelRoot
import com.bihe0832.android.base.debug.audio.asr.getASROfflineRecognizerConfig_paraformer_small
import com.bihe0832.android.base.debug.audio.asr.md5_ASROfflineRecognizerConfig_paraformer
import com.bihe0832.android.base.debug.audio.asr.md5_ASROfflineRecognizerConfig_paraformer_small
import com.bihe0832.android.base.debug.audio.asr.md5_ASROnlineRecognizerConfig
import com.bihe0832.android.base.debug.audio.asr.md5_ASROnlineRecognizerConfig_small
import com.bihe0832.android.base.debug.audio.asr.modelDir_ASROfflineRecognizerConfig_paraformer
import com.bihe0832.android.base.debug.audio.asr.modelDir_ASROfflineRecognizerConfig_paraformer_small
import com.bihe0832.android.base.debug.audio.asr.modelDir_ASROnlineRecognizerConfig
import com.bihe0832.android.base.debug.audio.asr.modelDir_ASROnlineRecognizerConfig_small
import com.bihe0832.android.base.debug.audio.asr.url_ASROfflineRecognizerConfig_paraformer
import com.bihe0832.android.base.debug.audio.asr.url_ASROfflineRecognizerConfig_paraformer_small
import com.bihe0832.android.base.debug.audio.asr.url_ASROnlineRecognizerConfig
import com.bihe0832.android.base.debug.audio.asr.url_ASROnlineRecognizerConfig_small
import com.bihe0832.android.common.debug.audio.DebugWAVListFragment
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
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
import com.bihe0832.android.lib.speech.DEFAULT_ENDPOINT_MODEL_DIR
import com.bihe0832.android.lib.speech.endpoint.AudioRecordWithEndpoint
import com.bihe0832.android.lib.speech.getDefaultOnlineRecognizerConfig
import com.bihe0832.android.lib.speech.recognition.ASROfflineManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools
import java.io.File

class DebugRecordAndASRFragment : DebugEnvFragment() {
    val TAG = this.javaClass.simpleName
    private val mAudioRecordFile = mutableListOf<AudioRecordFile>()
    private val mASROfflineManager by lazy { ASROfflineManager() }

    override fun initView(view: View) {
        super.initView(view)
        AAFAudioTools.addRecordScene(SCENE, "读取麦克风", "音频录制测试")
        AAFAudioTools.startRecordPermissionCheck(activity,
            SCENE,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    AAFAudioTools.init()
                }
            })

    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            addAll(getWavList())
            addAll(getASRPreList())
            addAll(getASRList())
        }
    }

    private fun getWavList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getTipsItem("WAV 文件处理相关"))
            add(
                getDebugFragmentItemData(
                    "<font color ='#3AC8EF'><b>本地 WAV 查看及识别</b></font>",
                    DebugLocalWAVListWithASRFragment::class.java
                )
            )
            add(
                getDebugItem("指定文件 WAV头 信息查看",
                    View.OnClickListener { readWavHead(preFile()) })
            )
            add(
                getDebugItem("空文件 WAV头 信息查看",
                    View.OnClickListener { readWavHead(preEmpty()) })
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
            add(
                getDebugFragmentItemData("本地 WAV 查看", DebugWAVListFragment::class.java)
            )

        }
    }

    private fun getASRPreList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getTipsItem("语音识别模型相关"))
            add(getDebugItem("清理模型缓存", View.OnClickListener {
                ThreadManager.getInstance().start {
                    FileUtils.deleteDirectory(File(getASRModelRoot()))
                    ZixieContext.showToast("清理完成")
                }
            }))
            add(getDebugItem(
                "清空本地音频临时缓存"
            ) { FileUtils.deleteDirectory(File(AAFFileWrapper.getMediaTempFolder())) })
            add(
                getDebugItem(
                    "下载并准备非流式模型：sherpa-onnx-paraformer-zh-small",
                    View.OnClickListener {
                        checkAndDoAction(activity!!,
                            modelDir_ASROfflineRecognizerConfig_paraformer_small,
                            url_ASROfflineRecognizerConfig_paraformer_small,
                            md5_ASROfflineRecognizerConfig_paraformer_small,
                            {})
                    })
            )

            add(
                getDebugItem(
                    "下载并准备非流式模型：sherpa-onnx-paraformer-zh-2023-09-14",
                    View.OnClickListener {
                        checkAndDoAction(
                            activity!!,
                            modelDir_ASROfflineRecognizerConfig_paraformer,
                            url_ASROfflineRecognizerConfig_paraformer,
                            md5_ASROfflineRecognizerConfig_paraformer
                        ) {}
                    })
            )

            add(
                getDebugItem("下载并准备流式模型：sherpa-onnx-streaming-zipformer-bilingual-zh-en-mobile",
                    View.OnClickListener {
                        checkAndDoAction(
                            activity!!,
                            modelDir_ASROnlineRecognizerConfig_small,
                            url_ASROnlineRecognizerConfig_small,
                            md5_ASROnlineRecognizerConfig_small
                        ) {}
                    })
            )
        }
    }

    private fun getASRList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getTipsItem("ASR 语音识别相关"))
            add(getDebugItem("录音及识别初始化", View.OnClickListener { initRecordAndASR() }))
            add(
                getDebugItem("ARS 静音检测：结束实时识别及录制",
                    View.OnClickListener { AudioRecordWithEndpoint.stopRecord(context!!) })
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
            add(
                getDebugItem("ARS 识别：开启录制识别静音检测回调数据",
                    View.OnClickListener { startReal() })
            )
            add(
                getDebugItem("ARS 识别：开启录制识别静音检测回调文件",
                    View.OnClickListener { startRealFile() })
            )

            add(
                getDebugItem("ARS 识别：基于本地文件识别",
                    View.OnClickListener { startFile(preFile()) })
            )

        }
    }

    @SuppressLint("MissingPermission")
    fun initRecordAndASR() {
        AAFAudioTools.startRecordPermissionCheck(activity,
            SCENE,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    AudioRecordWithEndpoint.init(
                        context!!, getDefaultOnlineRecognizerConfig(
                            AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, DEFAULT_ENDPOINT_MODEL_DIR
                        )
                    )
                    checkAndDoAction(
                        activity!!, modelDir_ASROfflineRecognizerConfig_paraformer_small,
                        url_ASROfflineRecognizerConfig_paraformer_small,
                        md5_ASROfflineRecognizerConfig_paraformer_small,
                    ) {
                        mASROfflineManager.initRecognizer(
                            getASROfflineRecognizerConfig_paraformer_small()
                        )
                    }
                }
            })
    }

    fun startWav() {
        AAFAudioTools.startRecordPermissionCheck(activity,
            SCENE,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    AudioRecordManager.startRecord(
                        activity, SCENE, "rere"
                    ) { config, audioChunk, dataLength ->
                        ZLog.d(
                            TAG, "Started recording callback:${dataLength}"
                        )
                    }
                }
            })
    }

    fun stopWav() {
        AudioRecordManager.stopRecord(context!!, SCENE)
    }

    fun startWaveFile() {
        val file = AAFFileWrapper.getMediaTempFolder() + System.currentTimeMillis() + ".wav"
        AAFAudioTools.startRecordPermissionCheck(activity,
            SCENE,
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
            SCENE,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    AudioRecordWithEndpoint.startDataRecord { audioRecordConfig, pcmData, result ->
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
        AudioRecordWithEndpoint.forceEndCurrent().let {
            ZLog.d(TAG, "forceEndCurrent:$it")
        }
    }

    fun testSplit() {
        AudioRecordWithEndpoint.startDataRecord { audioRecordConfig, pcmData, result ->
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

    fun startReal() {
        AudioRecordWithEndpoint.startDataRecord { audioRecordConfig, pcmData, result ->
            pcmData?.let { data ->
                Log.i(TAG, "====================")
                val max = (Short.MAX_VALUE * 0.1f).toInt().toShort()
                Log.i(
                    TAG, "record data size:${pcmData.size} max:$max, audioMax: ${pcmData.max()}"
                )
                if (SherpaAudioConvertTools.isOverSilence(pcmData, max)) {
                    val samples = SherpaAudioConvertTools.shortArrayToSherpaArray(data, data.size)
                    recognise(audioRecordConfig.sampleRateInHz, samples)
//                    AudioUtils.shortArrayToByteArray(data).let {
//                        SherpaAudioConvertTools.byteArrayToSherpaArray(it).let { float ->
//                            recognise(audioRecordConfig.sampleRateInHz, float)
//                        }
//
//                        AudioUtils.byteArrayToShortArray(it, it.size).let { short ->
//                            val erer = SherpaAudioConvertTools.shortArrayToSherpaArray(short, short.size)
//                            recognise(audioRecordConfig.sampleRateInHz, erer)
//                        }
//                    }
                } else {
                    Log.i(TAG, "无效音频，无有效内容")
                }
                Log.i(TAG, "====================")
            }
        }
    }

    fun startRealFile() {
        val max = (Short.MAX_VALUE * 0.1f).toInt().toShort()
        AudioRecordWithEndpoint.startFileRecord(activity,
            "File",
            AAFFileWrapper.getMediaTempFolder(),
            max,
            object : AAFDataCallback<String>() {
                override fun onSuccess(result: String?) {
                    ZLog.d(TAG, "record data:${result}")
                    result?.let {
                        readWavHead(it)
                        recognise(
                            WaveFileReader(result).sampleRate.toInt(),
                            SherpaAudioConvertTools.readWavAudioToSherpaArray(result)
                        )
                    }
                }
            })
    }

    fun recognise(sampleRateInHz: Int, data: FloatArray?) {
        data?.let { samples ->
            Log.i(TAG, "record data:${samples.size}")
//            mKeywordSpotterManager.doRecognizer(sampleRateInHz, samples).let {
//                Log.i(TAG, "mKeywordSpotterManager Start to recognizer:$it")
//            }
            val time = System.currentTimeMillis()
            mASROfflineManager.startRecognizer(sampleRateInHz, samples).let {
                Log.i(
                    TAG,
                    "mRecognizerManager cost ${System.currentTimeMillis() - time} to recognizer:$it"
                )

                showResult(it)
            }
        }
    }

    fun startFile(file: String) {
        readWavHead(file)
        SherpaAudioConvertTools.readWavAudioToSherpaArray(file)?.let {
            recognise(WaveFileReader(file).sampleRate.toInt(), it)
        }
    }

}



