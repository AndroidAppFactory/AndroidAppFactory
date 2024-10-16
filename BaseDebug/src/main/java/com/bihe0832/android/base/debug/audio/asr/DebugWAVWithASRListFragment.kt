/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.audio.asr

import android.util.Log
import android.view.View
import com.bihe0832.android.common.debug.audio.DebugWAVListFragment
import com.bihe0832.android.common.debug.audio.card.AudioData
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.speech.DEFAULT_ENDPOINT_MODEL_DIR
import com.bihe0832.android.lib.speech.DEFAULT_KWS_MODEL_DIR
import com.bihe0832.android.lib.speech.getDefaultOnlineRecognizerConfig
import com.bihe0832.android.lib.speech.getDefaultKeywordSpotterConfig
import com.bihe0832.android.lib.speech.kws.KeywordSpotterManager
import com.bihe0832.android.lib.speech.recognition.ASROfflineManager
import com.bihe0832.android.lib.speech.recognition.ASROnlineManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools
import java.io.File

class DebugWAVWithASRListFragment : DebugWAVListFragment() {
    private var hasInitSuccess = false
    private val mASROfflineManager by lazy { ASROfflineManager() }
    private val mASROnlineManager by lazy { ASROnlineManager() }
    private val mASROnlineManager2 by lazy { ASROnlineManager() }
    private val mKeywordSpotterManager by lazy { KeywordSpotterManager() }

    override fun initView(view: View) {
        super.initView(view)
        ThreadManager.getInstance().start {
            mASROfflineManager.initRecognizer(getASROfflineRecognizerConfig(context!!))
            mASROnlineManager.initRecognizer(
                context!!,
                getDefaultOnlineRecognizerConfig(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, DEFAULT_ENDPOINT_MODEL_DIR)
            )
            val modelDir = "sherpa-onnx-streaming-zipformer-bilingual-zh-en-2023-02-20"
            mASROnlineManager2.initRecognizer(
                context!!, OnlineRecognizerConfig(
                    featConfig = FeatureConfig(
                        sampleRate = AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, featureDim = 80
                    ), modelConfig = OnlineModelConfig(
                        transducer = OnlineTransducerModelConfig(
                            encoder = "$modelDir/encoder-epoch-99-avg-1.int8.onnx",
                            decoder = "$modelDir/decoder-epoch-99-avg-1.int8.onnx",
                            joiner = "$modelDir/joiner-epoch-99-avg-1.int8.onnx",
                        ),
                        tokens = "$modelDir/tokens.txt",
                        modelType = "zipformer",
                    ), hotwordsFile = "$modelDir/hotword.txt"
                )
            )
            mKeywordSpotterManager.initRecognizer(
                context!!, getDefaultKeywordSpotterConfig(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, DEFAULT_KWS_MODEL_DIR)
            )
            mKeywordSpotterManager.start("")
            showResult("")
            hasInitSuccess = true
        }
    }

    override fun filter(filePath: String): Boolean {
        return File(filePath).length() > 44
    }

    override fun palyAndRecognise(data: AudioData, play: Boolean) {
        if (hasInitSuccess) {
            super.palyAndRecognise(data, play)
            SherpaAudioConvertTools.readWavAudioToSherpaArray(data.filePath)?.let { audioData ->
                val max = (Short.MAX_VALUE * 0.1f).toInt().toShort()
                data.amplitude = "最大振幅：" + (audioData.max() * Byte.MAX_VALUE * 2.0F).toInt() + ", 基准：$max"
                Log.i(
                    TAG,
                    "record data size:${audioData.size} max:$max, audioMax: ${audioData.max() * Byte.MAX_VALUE * 2.0F}"
                )

                var msg = "未能识别数据"
                if (SherpaAudioConvertTools.isOverSilence(audioData, max)) {
                    var time = System.currentTimeMillis()
                    mASROfflineManager.startRecognizer(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData)
                            .let { result ->
                                Log.e(TAG, "mASROfflineManager Start to recognizer:$result")
                                msg = "离线识别（${System.currentTimeMillis() - time}）：$result"
                            }
                    time = System.currentTimeMillis()
                    mASROnlineManager.startRecognizer(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData)
                            .let { result ->
                                Log.e(TAG, "mASROnlineManager Start to recognizer:$result")
                                msg = "$msg\n流式识别1（${System.currentTimeMillis() - time}）：$result"
                            }
                    time = System.currentTimeMillis()
                    mASROnlineManager2.startRecognizer(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData)
                            .let { result ->
                                Log.e(TAG, "mASROnlineManager2 Start to recognizer:$result")
                                msg = "$msg\n流式识别2（${System.currentTimeMillis() - time}）：$result"
                            }
                    time = System.currentTimeMillis()
                    mKeywordSpotterManager.doRecognizer(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData)
                            .let { result ->
                                Log.e(TAG, "mKeywordSpotterManager Start to recognizer:$result")
                                msg = "$msg\n关键字识别（${System.currentTimeMillis() - time}）：$result"
                            }

                } else {
                    msg = "无效音频，无有效内容"
                }
                data.recogniseResult = msg
            }
        } else {
            showResult("初始化未完成，请稍候")
        }

    }
}
