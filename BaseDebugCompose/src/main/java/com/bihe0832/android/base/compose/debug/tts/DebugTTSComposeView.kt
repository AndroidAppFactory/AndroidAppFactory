/*
 * Created by zixie <code@bihe0832.com> on 2025/12/12
 * Copyright (c) 2025. All rights reserved.
 * Last modified 2025/12/12
 */

package com.bihe0832.android.base.compose.debug.tts

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.tts.core.impl.TTSImplNotifyWithKey
import com.bihe0832.android.lib.tts.core.impl.TTSImplWithConfig
import com.bihe0832.android.lib.utils.IdGenerator
import java.util.Locale

/**
 * TTS（文字转语音）调试界面
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/12/12
 * Description: TTS 功能调试，包括多实例播报、播报模式、参数调整等
 */

private const val LOG_TAG = "DebugTTSComposeView"

@Composable
fun DebugTTSComposeView() {
    val context = LocalContext.current
    
    // 创建 TTS 实例
    val ttsImpl1 = remember {
        TTSImplWithConfig("ttsImpl1").apply {
            initTTSImplWithConfig(context.applicationContext, Locale.SIMPLIFIED_CHINESE, "")  // 使用系统默认 TTS 引擎
            addTTSListener(object : TTSImplNotifyWithKey.TTSListener {
                override fun onStart(utteranceId: String, data: String) {
                    ZLog.e("$LOG_TAG ttsImpl1 speak onStart:$utteranceId $data")
                }

                override fun onError(utteranceId: String, data: String) {
                    ZLog.e("$LOG_TAG ttsImpl1 speak onError:$utteranceId $data")
                }

                override fun onComplete(utteranceId: String, data: String) {
                    ZLog.e("$LOG_TAG ttsImpl1 speak onComplete:$utteranceId $data")
                }
            })
            setPitch(getDefaultPitch())
            setSpeechRate(getDefaultSpeechRate())
        }
    }

    val ttsImpl2 = remember {
        TTSImplNotifyWithKey().apply {
            initTTSImplWithKey(
                context.applicationContext,
                Locale.SIMPLIFIED_CHINESE,
                "",
                null,
                object : TTSImplNotifyWithKey.TTSListener {
                    override fun onStart(utteranceId: String, data: String) {
                        ZLog.e("$LOG_TAG ttsImpl2 speak onStart:$utteranceId $data")
                    }

                    override fun onError(utteranceId: String, data: String) {
                        ZLog.e("$LOG_TAG ttsImpl2 speak onError:$utteranceId $data")
                    }

                    override fun onComplete(utteranceId: String, data: String) {
                        ZLog.e("$LOG_TAG ttsImpl2 speak onComplete:$utteranceId $data")
                    }
                }
            )
        }
    }

    val tts1 = remember {
        TextToSpeech(context.applicationContext) { status ->
            ZLog.d(LOG_TAG, "tts1 init status: $status")
        }
    }

    val tts2 = remember {
        TextToSpeech(context.applicationContext) { status ->
            ZLog.d(LOG_TAG, "tts2 init status: $status")
        }
    }

    val idGenerator = remember { IdGenerator(1000) }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            ttsImpl1.onDestroy()
            ttsImpl2.onDestroy()
            tts1.shutdown()
            tts2.shutdown()
        }
    }

    DebugContent {
        DebugComposeItem("TTS 基础功能调试", "DebugTTSBasicView") { 
            DebugTTSBasicView() 
        }

        DebugItem("TTS1 和 TTS2 同时顺序播报") {
            ttsImpl1.speak(
                idGenerator.generate().toString(),
                TTSData("TTS1 顺序播报"),
                TTSConfig.SPEEAK_TYPE_SEQUENCE
            )
            ttsImpl2.speak(
                idGenerator.generate().toString(),
                TTSData("TTS2 顺序播报"),
                TTSConfig.SPEEAK_TYPE_SEQUENCE
            )
        }

        DebugItem("TTS1 顺序播报") {
            ttsImpl1.speak(
                idGenerator.generate().toString(),
                TTSData("TTS1 顺序播报"),
                TTSConfig.SPEEAK_TYPE_SEQUENCE
            )
        }

        DebugItem("TTS2 顺序播报") {
            ttsImpl2.speak(
                idGenerator.generate().toString(),
                TTSData("TTS2 顺序播报"),
                TTSConfig.SPEEAK_TYPE_SEQUENCE
            )
        }

        DebugItem("TTS1 和 TTS2 抢断播报") {
            ttsImpl1.speak(
                idGenerator.generate().toString(),
                TTSData("TTS1 抢断播报"),
                TTSConfig.SPEEAK_TYPE_FLUSH
            )
            ttsImpl2.speak(
                idGenerator.generate().toString(),
                TTSData("TTS2 抢断播报"),
                TTSConfig.SPEEAK_TYPE_FLUSH
            )
        }

        DebugItem("TTS1 抢断播报") {
            ttsImpl1.speak(
                idGenerator.generate().toString(),
                TTSData("TTS1 抢断播报"),
                TTSConfig.SPEEAK_TYPE_FLUSH
            )
        }

        DebugItem("TTS1 清空播报") {
            ttsImpl1.speak(
                idGenerator.generate().toString(),
                TTSData("TTS1 清空播报"),
                TTSConfig.SPEEAK_TYPE_CLEAR
            )
        }

        DebugItem("TTS2 抢断播报") {
            ttsImpl2.speak(
                idGenerator.generate().toString(),
                TTSData("TTS2 抢断播报"),
                TTSConfig.SPEEAK_TYPE_FLUSH
            )
        }

        DebugItem("TextToSpeech 设置语言") {
            tts1.setLanguage(Locale.CHINA)
            tts2.setLanguage(Locale.CHINA)
        }

        DebugItem("TextToSpeech 递增设置播报参数") {
            ttsImpl1.setPitch(ttsImpl1.getConfigPitch() + 0.2f)
            ttsImpl1.setSpeechRate(ttsImpl1.getConfigSpeechRate() + 0.2f)
            ttsImpl1.setVoiceVolume(ttsImpl1.getConfigVoiceVolume() + 10)
            ttsImpl1.speak(idGenerator.generate().toString(), TTSData("递增设置播报参数"))
            
            ttsImpl2.setPitch(ttsImpl1.getConfigPitch() + 0.2f)
            ttsImpl2.setSpeechRate(ttsImpl1.getConfigSpeechRate() + 0.2f)
            ttsImpl2.setVoiceVolume(ttsImpl2.getVoiceVolumeInt() + 10)
            ttsImpl2.speak(idGenerator.generate().toString(), TTSData("递增设置播报参数"))
        }

        DebugItem("TextToSpeech 递减设置播报参数") {
            ttsImpl1.setPitch(ttsImpl1.getConfigPitch() - 0.1f)
            ttsImpl1.setSpeechRate(ttsImpl1.getConfigSpeechRate() - 0.1f)
            ttsImpl1.setVoiceVolume(ttsImpl1.getVoiceVolumeInt() - 10)
            ttsImpl1.speak(idGenerator.generate().toString(), TTSData("递减设置播报参数"))
            
            ttsImpl2.setPitch(ttsImpl1.getConfigPitch() - 0.1f)
            ttsImpl2.setSpeechRate(ttsImpl1.getConfigSpeechRate() - 0.1f)
            ttsImpl2.setVoiceVolume(ttsImpl2.getVoiceVolumeInt() - 10)
            ttsImpl2.speak(idGenerator.generate().toString(), TTSData("递减设置播报参数"))
        }

        DebugItem("TextToSpeech 抢断播报") {
            tts1.speak("抢断播报1", TextToSpeech.QUEUE_FLUSH, null, null)
            tts2.speak("抢断播报112", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
}
