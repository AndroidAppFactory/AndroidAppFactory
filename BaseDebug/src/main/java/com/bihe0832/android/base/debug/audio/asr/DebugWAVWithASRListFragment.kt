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
import com.bihe0832.android.lib.audio.record.wrapper.AAFAudioTools
import com.bihe0832.android.lib.speech.recognition.ASRManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.k2fsa.sherpa.onnx.SherpaAudioConvertTools

class DebugWAVWithASRListFragment : DebugWAVListFragment() {
    private val mASRManager by lazy { ASRManager() }


    override fun initView(view: View) {
        super.initView(view)
        ThreadManager.getInstance().start {
            mASRManager.initRecognizer(
                context!!,
                "sherpa-onnx-paraformer-zh-2023-09-14",
                AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ
            )
        }
    }

    override fun palyAndRecognise(data: AudioData, play: Boolean) {
        super.palyAndRecognise(data, play)
        SherpaAudioConvertTools.readWavAudioToSherpaArray(data.filePath)?.let { audioData ->
            val max = (Short.MAX_VALUE * 0.1f).toInt().toShort()
            data.amplitude = "最大振幅：" + (audioData.max() * Byte.MAX_VALUE * 2.0F).toInt() + ", 基准：$max"
            Log.i(
                TAG, "record data size:${audioData.size} max:$max, audioMax: ${audioData.max() * Byte.MAX_VALUE * 2.0F}"
            )
            var msg = "未能识别数据"
            if (SherpaAudioConvertTools.isOverSilence(audioData, max)) {
                mASRManager.startRecognizer(AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, audioData).let { result ->
                    Log.i(TAG, "mRecognizerManager Start to recognizer:$result")
                    msg = result
                }
            } else {
                msg = "无效音频，无有效内容"
            }
            data.recogniseResult = msg
        }
    }
}
