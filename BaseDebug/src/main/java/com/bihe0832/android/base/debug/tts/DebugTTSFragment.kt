/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.tts


import android.speech.tts.TextToSpeech
import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.tts.core.TTSHelper
import java.util.Locale


class DebugTTSFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    private val ttsImpl1 by lazy {
        TTSHelper()
    }

    private val ttsImpl2 by lazy {
        TTSHelper()
    }

    var tts1: TextToSpeech? = null
    var tts2: TextToSpeech? = null

    override fun initView(view: View) {
        super.initView(view)
        ttsImpl1.init(
            view.context, "ttsImpl1", Locale.CHINA
        )
        ttsImpl2.init(
            view.context, "ttsImpl2", Locale.CHINA
        )
        tts1 = TextToSpeech(context!!) { status ->

        }

        tts2 = TextToSpeech(context!!) { status ->

        }

    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("TTS 基础功能调试", DebugTTSBasicFragment::class.java))
            add(DebugItemData("TTS1 和 TTS2 同时顺序播报", View.OnClickListener {
                ttsImpl1.speak(TTSData("TTS1 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE)
                ttsImpl2.speak(TTSData("TTS2 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE)
            }))

            add(DebugItemData("TTS1顺序播报", View.OnClickListener {
                ttsImpl1.speak(TTSData("TTS1 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE)
            }))

            add(DebugItemData("TTS2 顺序播报", View.OnClickListener {
                ttsImpl2.speak(TTSData("TTS2 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE)
            }))
            add(DebugItemData("TTS1 和 TTS2 抢断播报", View.OnClickListener {
                ttsImpl1.speak(TTSData("TTS1 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
                ttsImpl2.speak(TTSData("TTS2 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
            }))

            add(DebugItemData("TTS1抢断播报", View.OnClickListener {
                ttsImpl1.speak(TTSData("TTS1 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
            }))

            add(DebugItemData("TTS2 抢断播报", View.OnClickListener {
                ttsImpl2.speak(TTSData("TTS2 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
            }))

            add(DebugItemData("TextToSpeech 设置语言", View.OnClickListener {
                tts1?.setLanguage(Locale.CHINA)
                tts2?.setLanguage(Locale.CHINA)
            }))

            add(DebugItemData("TextToSpeech 抢断播报", View.OnClickListener {
                tts1?.speak("抢断播报1", TextToSpeech.QUEUE_FLUSH, null, null)
                tts2?.speak("抢断播报112", TextToSpeech.QUEUE_FLUSH, null, null)
            }))
        }
    }

}