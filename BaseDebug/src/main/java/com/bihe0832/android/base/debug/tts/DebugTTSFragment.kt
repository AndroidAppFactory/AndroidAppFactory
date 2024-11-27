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
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.tts.core.impl.TTSImplNotifyWithKey
import com.bihe0832.android.lib.tts.core.impl.TTSImplWithConfig
import com.bihe0832.android.lib.utils.IdGenerator
import java.util.Locale


class DebugTTSFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    private val idGenerator = IdGenerator(1000)

    private val ttsImpl1 by lazy {
        TTSImplWithConfig("ttsImpl1")
    }

    private val ttsImpl2 by lazy {
        TTSImplNotifyWithKey()
    }


    var tts1: TextToSpeech? = null
    var tts2: TextToSpeech? = null

    override fun initView(view: View) {
        super.initView(view)
        ttsImpl1.initTTSImplWithConfig(view.context, Locale.SIMPLIFIED_CHINESE, "com.iflytek.speechsuite")
        ttsImpl1.addTTSListener(object : TTSImplNotifyWithKey.TTSListener {
            override fun onStart(utteranceId: String, data: String) {
                ZLog.e("ttsImpl1 speak onStart:$utteranceId $data")
            }

            override fun onError(utteranceId: String, data: String) {
                ZLog.e("ttsImpl1 speak onError:$utteranceId $data")

            }

            override fun onComplete(utteranceId: String, data: String) {
                ZLog.e("ttsImpl1 speak onComplete:$utteranceId $data")
            }

        })
        ttsImpl1.setPitch(ttsImpl1.getDefaultPitch())
        ttsImpl1.setSpeechRate(ttsImpl1.getDefaultSpeechRate())

        ttsImpl2.initTTSImplWithKey(context!!,
            Locale.SIMPLIFIED_CHINESE,
            "com.google.android.tts",
            null,
            object : TTSImplNotifyWithKey.TTSListener {
                override fun onStart(utteranceId: String, data: String) {
                    ZLog.e("ttsImpl2 speak onStart:$utteranceId $data")
                }

                override fun onError(utteranceId: String, data: String) {
                    ZLog.e("ttsImpl2 speak onError:$utteranceId $data")

                }

                override fun onComplete(utteranceId: String, data: String) {
                    ZLog.e("ttsImpl2 speak onComplete:$utteranceId $data")
                }

            })
        tts1 = TextToSpeech(context!!) { status ->

        }

        tts2 = TextToSpeech(context!!) { status ->

        }

    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("TTS 基础功能调试", DebugTTSBasicFragment::class.java))
            add(getDebugItem("TTS1 和 TTS2 同时顺序播报", View.OnClickListener {
                ttsImpl1.speak(
                    idGenerator.generate().toString(), TTSData("TTS1 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE
                )
                ttsImpl2.speak(
                    idGenerator.generate().toString(), TTSData("TTS2 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE
                )
            }))

            add(getDebugItem("TTS1顺序播报", View.OnClickListener {
                ttsImpl1.speak(
                    idGenerator.generate().toString(), TTSData("TTS1 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE
                )
            }))

            add(getDebugItem("TTS2 顺序播报", View.OnClickListener {
                ttsImpl2.speak(
                    idGenerator.generate().toString(), TTSData("TTS2 顺序播报"), TTSConfig.SPEEAK_TYPE_SEQUENCE
                )
            }))
            add(getDebugItem("TTS1 和 TTS2 抢断播报", View.OnClickListener {
                ttsImpl1.speak(idGenerator.generate().toString(), TTSData("TTS1 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
                ttsImpl2.speak(idGenerator.generate().toString(), TTSData("TTS2 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
            }))

            add(getDebugItem("TTS1抢断播报", View.OnClickListener {
                ttsImpl1.speak(idGenerator.generate().toString(), TTSData("TTS1 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
            }))

            add(getDebugItem("TTS1清空播报", View.OnClickListener {
                ttsImpl1.speak(idGenerator.generate().toString(), TTSData("TTS1 清空播报"), TTSConfig.SPEEAK_TYPE_CLEAR)
            }))
            add(getDebugItem("TTS2 抢断播报", View.OnClickListener {
                ttsImpl2.speak(idGenerator.generate().toString(), TTSData("TTS2 抢断播报"), TTSConfig.SPEEAK_TYPE_FLUSH)
            }))

            add(getDebugItem("TextToSpeech 设置语言", View.OnClickListener {
                tts1?.setLanguage(Locale.CHINA)
                tts2?.setLanguage(Locale.CHINA)
            }))

            add(getDebugItem("TextToSpeech 递增设置播报参数", View.OnClickListener {
                ttsImpl1.setPitch(ttsImpl1.getConfigPitch() + 0.1f)
                ttsImpl1.setSpeechRate(ttsImpl1.getConfigSpeechRate() + 0.1f)
                ttsImpl1.setVoiceVolume(ttsImpl1.getConfigVoiceVolume() + 10)
                ttsImpl2.setPitch(ttsImpl1.getConfigPitch() + 0.1f)
                ttsImpl2.setSpeechRate(ttsImpl1.getConfigSpeechRate() + 0.1f)
                ttsImpl2.setVoiceVolume(ttsImpl2.getVoiceVolumeInt() + 10)
            }))
            add(getDebugItem("TextToSpeech 递减设置播报参数", View.OnClickListener {
                ttsImpl1.setPitch(ttsImpl1.getConfigPitch() - 0.1f)
                ttsImpl1.setSpeechRate(ttsImpl1.getConfigSpeechRate() - 0.1f)
                ttsImpl1.setVoiceVolume(ttsImpl1.getVoiceVolumeInt() - 10)
                ttsImpl2.setPitch(ttsImpl1.getConfigPitch() - 0.1f)
                ttsImpl2.setSpeechRate(ttsImpl1.getConfigSpeechRate() - 0.1f)
                ttsImpl2.setVoiceVolume(ttsImpl2.getVoiceVolumeInt() - 10)
            }))

            add(getDebugItem("TextToSpeech 抢断播报", View.OnClickListener {
                tts1?.speak("抢断播报1", TextToSpeech.QUEUE_FLUSH, null, null)
                tts2?.speak("抢断播报112", TextToSpeech.QUEUE_FLUSH, null, null)
            }))
        }
    }

}