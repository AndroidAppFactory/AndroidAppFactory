/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.audio

import android.util.Log
import android.view.View
import com.bihe0832.android.base.debug.audio.asr.AudioDataFactoryWithASR
import com.bihe0832.android.common.debug.audio.DebugAudioDataFactory
import com.bihe0832.android.common.debug.audio.DebugWAVListWithProcessFragment
import com.bihe0832.android.common.debug.audio.card.AudioData
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools
import java.io.File

class DebugLocalWAVListWithASRFragment : DebugWAVListWithProcessFragment() {
    private var hasInitSuccess = false
    private val mAudioDataFactoryWithASR = AudioDataFactoryWithASR(getAudioDataFactoryCallback())

    fun logInfo(logFile: String, msg: String) {
        LoggerFile.logH5(logFile, "", msg)
    }

    override fun initView(view: View) {
        super.initView(view)
        startInit()
    }
    fun startInit(){
        ThreadManager.getInstance().start {
            mAudioDataFactoryWithASR.init(activity!!)
            showResult("")
            hasInitSuccess = true
        }

        TaskManager.getInstance().addTask(object : BaseTask() {
            val name = "DebugLocalWAVListInit"
            override fun getMyInterval(): Int {
                return 1
            }

            override fun getNextEarlyRunTime(): Int {
                return 0
            }

            override fun doTask() {
                if (mAudioDataFactoryWithASR.isReady()) {
                    TaskManager.getInstance().removeTask(name)
                }
            }

            override fun getTaskName(): String {
                return name
            }

        })
    }

    override fun itemClickAction(data: AudioData, play: Boolean) {
        if (hasInitSuccess) {
            ThreadManager.getInstance().start {
                super.itemClickAction(data, play)
            }
            Log.e(TAG, "-----------------------------\n识别测试 ${data.filePath}")
            SherpaAudioConvertTools.readWavAudioToSherpaArray(data.filePath)?.let { audioData ->
                val max = (Short.MAX_VALUE * 0f).toInt().toShort()
                data.amplitude =
                    "最大振幅：" + (audioData.max() * Byte.MAX_VALUE * 2.0F).toInt() + ", 基准：$max"
                Log.e(
                    TAG,
                    "record data size:${audioData.size} max:$max, audioMax: ${audioData.max() * Byte.MAX_VALUE * 2.0F}"
                )

                var msg = "未能识别数据"
                if (SherpaAudioConvertTools.isOverSilence(audioData, max)) {
                    var time = System.currentTimeMillis()
//                    mAudioDataFactoryWithASR.mParaformerASROfflineManager.startRecognizer(
//                        AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData
//                    ).let { result ->
//                        "非流式识别1：$result 用时：（${System.currentTimeMillis() - time}）模型：paraformer-zh-2023-09-14".let { log ->
//                            Log.e(TAG, log)
//                            msg = "$msg\n$log"
//                        }
//                    }
//                    time = System.currentTimeMillis()
                    mAudioDataFactoryWithASR.mSmallParaformerASROfflineManager.startRecognizer(
                        AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData
                    ).let { result ->
                        "非流式识别2：$result 用时：（${System.currentTimeMillis() - time}）模型：paraformer-zh-small-2023-09-14".let { log ->
                            Log.e(TAG, log)
                            msg = "$msg\n$log"
                        }
                    }
                    data.recogniseResult = msg
                } else {
                    msg = "无效音频，无有效内容"
                    FileUtils.deleteFile(data.filePath)
                }
            }
        } else {
            showResult("初始化未完成，请稍候")
        }

    }

    override fun getAudioDataProcessInterface(): DebugAudioDataFactory {
        return object : DebugAudioDataFactory(getAudioDataFactoryCallback()) {
            override fun processAudioList(logFile: String, folder: String, fileList: List<File>) {
                ZLog.d(folder)
                if (hasInitSuccess) {
                    LoggerFile.initFile(
                        getLogFile(),
                        LoggerFile.getH5LogHeader("<title>本地音频批量处理结果展示</title>\n") +
                                "<div><font color='#3AC8EF'>文件目录：</font><BR>" +
                                "${
                                    folder.replace(context!!.packageName, " * ").replace("/", " / ")
                                }<BR>" +
                                "<font color='#3AC8EF'>模型信息：</font><BR>" +
                                "流式模型1（31M）：sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23<BR>" +
                                "流式模型2（122.3M）：streaming-zipformer-bilingual-zh-en-2023-02-20-mobile<BR>" +
                                "非流式模型1（243.4M）：paraformer-zh-2023-09-14<BR>" +
                                "非流式模型2（81.8M）：paraformer-zh-small-2024-03-09<BR>" +
                                LoggerFile.getH5Content(),
                        true
                    )
                    super.processAudioList(logFile, folder, fileList)
                } else {
                    showResult("初始化未完成，请稍候")
                }
            }

            override fun processAudioData(
                logFile: String,
                index: Int,
                folder: String,
                filePath: String
            ) {

                SherpaAudioConvertTools.readWavAudioToSherpaArray(filePath)?.let { audioData ->
                    val max = (Short.MAX_VALUE * 0.1f).toInt().toShort()
                    commonProcessAudioData(logFile, index, folder, filePath)
                    logInfo(
                        logFile,
                        "文件大小：" + FileUtils.getFileLength(File(filePath).length()) + "，最大振幅：" + (audioData.max() * Byte.MAX_VALUE * 2.0F).toInt() + "，基准：$max"
                    )
                    if (SherpaAudioConvertTools.isOverSilence(audioData, max)) {
                        var time = System.currentTimeMillis()
                        mAudioDataFactoryWithASR.mASROnlineManager.let {
                            val stream = mAudioDataFactoryWithASR.mOnlineStream
                            if (stream != null){
                                it.resetStream(stream)
                                val result = it.acceptWaveform(stream,AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData)
                                logInfo(
                                    logFile,
                                    "<B>流式模型1 </B>用时：${System.currentTimeMillis() - time}ms，识别结果：${result.result?.text}"
                                )
                            }
                        }
                        time = System.currentTimeMillis()
                        mAudioDataFactoryWithASR.mASROnlineManager2.let {
                            val stream = mAudioDataFactoryWithASR.mOnlineStream2
                            if (stream != null){
                                it.resetStream(stream)
                                val result = it.acceptWaveform(stream,AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData)
                                logInfo(
                                    logFile,
                                    "<B>流式模型2 </B>用时：${System.currentTimeMillis() - time}ms，识别结果：${result.result?.text}"
                                )
                            }
                        }
                        time = System.currentTimeMillis()
                        mAudioDataFactoryWithASR.mParaformerASROfflineManager.startRecognizer(
                            AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData
                        ).let { result ->
                            logInfo(
                                logFile,
                                "<B>非流式模型1 </B>用时：${System.currentTimeMillis() - time}ms，识别结果：$result"
                            )
                        }
                        time = System.currentTimeMillis()
                        mAudioDataFactoryWithASR.mSmallParaformerASROfflineManager.startRecognizer(
                            AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData
                        ).let { result ->
                            logInfo(
                                logFile,
                                "<B>非流式模型2 </B>用时：${System.currentTimeMillis() - time}ms，识别结果：$result"
                            )
                        }
                    } else {
                        logInfo(logFile, "无效音频，无有效内容")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hasInitSuccess = false
        mAudioDataFactoryWithASR.reset()
    }
}
