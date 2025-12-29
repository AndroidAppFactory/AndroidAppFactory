/*
 * Created by zixie <code@bihe0832.com> on 2025/12/12
 * Copyright (c) 2025. All rights reserved.
 * Last modified 2025/12/12
 */

package com.bihe0832.android.base.compose.debug.tts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.download.wrapper.DownloadAPK
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.tts.LibTTS
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.tts.core.impl.TTSImpl
import com.bihe0832.android.lib.tts.core.impl.TTSImplNotifyWithKey
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.util.Locale

/**
 * TTS 基础功能调试界面
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/12/12
 * Description: TTS 基础功能测试，包括播报、保存、参数调整等
 */

private const val LOG_TAG = "DebugTTSBasicView"
private const val FORMAT = "语音播报测试：语速 %.1f，语调 %.1f，音量 %d，播报次数 %d"
@Preview
@Composable
fun DebugTTSBasicView() {
    val context = LocalContext.current
    var times by remember { mutableStateOf(0) }
    var ttsInfo by remember { mutableStateOf("") }
    var showGuide by remember { mutableStateOf(true) }
    var showDownloadButton by remember { mutableStateOf(true) }
    var testText by remember { mutableStateOf("上车、上车，上星。上分#上车|上车；上星:上分") }

    // 初始化 TTS
    LaunchedEffect(Unit) {
        LibTTS.init(
            context.applicationContext,
            Locale.CHINA,
            "",  // 使用系统默认 TTS 引擎
            object : TTSImpl.TTSInitListener {
                override fun onInitError() {
                    showGuide = true
                    showDownloadButton = APKUtils.getInstalledPackage(context, "com.google.android.tts") == null
                }

                override fun onLangUnAvailable() {
                    showGuide = true
                    showDownloadButton = APKUtils.getInstalledPackage(context, "com.google.android.tts") == null
                }

                override fun onLangAvailable() {
                    showGuide = false
                    showDownloadButton = false
                }
            }
        )

        LibTTS.addTTSSpeakListener(object : TTSImplNotifyWithKey.TTSListener {
            var lastStart = System.currentTimeMillis()

            override fun onStart(utteranceId: String, data: String) {
                lastStart = System.currentTimeMillis()
                ZLog.d(LOG_TAG, "onStart $data : $lastStart")
            }

            override fun onError(utteranceId: String, data: String) {
                val end = System.currentTimeMillis()
                ZLog.d(LOG_TAG, "onError $data : $lastStart $end  ${end - lastStart}")
            }

            override fun onComplete(utteranceId: String, data: String) {
                val end = System.currentTimeMillis()
                ZLog.d(LOG_TAG, "onComplete $data : $lastStart $end  ${end - lastStart}")
            }
        })

        // 添加定时任务
        TaskManager.getInstance().addTask(object : BaseTask() {
            override fun doTask() {
                LibTTS.stopSpeak()
            }

            override fun getNextEarlyRunTime(): Int = 0

            override fun getMyInterval(): Int = 20

            override fun getTaskName(): String = "TTS-DISABLED"
        })
    }

    // 更新 TTS 信息
    LaunchedEffect(times) {
        ttsInfo = String.format(
            FORMAT,
            LibTTS.getConfigSpeechRate(),
            LibTTS.getConfigPitch(),
            LibTTS.getConfigVoiceVolume(),
            times
        )
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            // TTS 资源会在 Application 中统一管理
        }
    }

    DebugContent {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // TTS 状态信息
            Text(
                text = ttsInfo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 30.dp)
            )

            // 测试文本输入
            OutlinedTextField(
                value = testText,
                onValueChange = { testText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                minLines = 2
            )

            // 引导信息
            if (showGuide) {
                Text(
                    text = "备注：\n\t 当前设备不支持中文，请点击下方按钮下载安装语音引擎后点击设置引擎切换语音引擎",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (showDownloadButton) {
                    Button(
                        onClick = {
                            (context as? Activity)?.let { activity ->
                                DownloadAPK.startDownloadWithCheckAndProcess(
                                    activity,
                                    "${ThemeResourcesManager.getString(com.bihe0832.android.lib.aaf.res.R.string.app_name)}:谷歌TTS下载",
                                    "${ThemeResourcesManager.getString(com.bihe0832.android.lib.aaf.res.R.string.app_name)}:谷歌TTS下载",
                                    "https://imtt.dd.qq.com/16891/apk/D1A7AE1C0B980EB66278E14008C9A6FF.apk",
                                    "",
                                    ""
                                )
                            } ?: ZixieContext.showToast("请在 Activity 中使用此功能")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text("下载并安装语音引擎")
                    }
                }

                Button(
                    onClick = {
                        IntentUtils.startSettings(context, "com.android.settings.TTS_SETTINGS")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 30.dp)
                ) {
                    Text("设置语音引擎")
                }
            }

            // 基础播报功能
            Button(
                onClick = {
                    times++
                    LibTTS.speak(TTSData(testText), TTSConfig.SPEEAK_TYPE_SEQUENCE)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (showGuide) 0.dp else 30.dp)
            ) {
                Text("播放语音")
            }

            Button(
                onClick = {
                    LibTTS.save(
                        TTSData(testText),
                        "${context.filesDir.absolutePath}/audio_${System.currentTimeMillis()}.wav"
                    )
                    ZixieContext.showToast("音频已保存")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("下载语音")
            }

            // 语速调整
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        LibTTS.setSpeechRate(LibTTS.getConfigSpeechRate() + 0.1f)
                        times++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("加快语速")
                }

                Button(
                    onClick = {
                        LibTTS.setSpeechRate(LibTTS.getConfigSpeechRate() - 0.1f)
                        times++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("减慢语速")
                }
            }

            // 语调调整
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        LibTTS.setPitch(LibTTS.getConfigPitch() + 0.1f)
                        times++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("升高音调")
                }

                Button(
                    onClick = {
                        LibTTS.setPitch(LibTTS.getConfigPitch() - 0.1f)
                        times++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("降低音调")
                }
            }

            // 音量调整
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        LibTTS.setVoiceVolume(LibTTS.getConfigVoiceVolume() + 10)
                        times++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("增加音量")
                }

                Button(
                    onClick = {
                        LibTTS.setVoiceVolume(LibTTS.getConfigVoiceVolume() - 10)
                        times++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("降低音量")
                }
            }

            // 播报模式说明
            Text(
                text = "备注：连续点击下方按钮可以测试各种语音效果：",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 30.dp)
            )

            // 播报模式
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        times++
                        LibTTS.speak(TTSData(testText), TTSConfig.SPEEAK_TYPE_SEQUENCE)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("顺序播放")
                }

                Button(
                    onClick = {
                        times++
                        LibTTS.speak(TTSData(testText), TTSConfig.SPEEAK_TYPE_NEXT)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("下一个播放")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        times++
                        LibTTS.speak(TTSData(testText), TTSConfig.SPEEAK_TYPE_FLUSH)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("马上播放")
                }

                Button(
                    onClick = {
                        times++
                        LibTTS.speak(TTSData(testText), TTSConfig.SPEEAK_TYPE_CLEAR)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("播放并清空剩下的")
                }
            }
        }
    }
}
