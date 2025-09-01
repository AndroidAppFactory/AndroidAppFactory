package com.bihe0832.android.base.debug.tts

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseFragment
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

class DebugTTSBasicFragment : BaseFragment() {
    val TAG = this.javaClass.simpleName
    private val FORMAT = "语音播报测试：语速 %s,语调 %s,音量 %s，最大ID %s "

    private var times = 0

    override fun getLayoutID(): Int {
        return R.layout.fragment_test_tts
    }

    override fun initView(view: View) {
        LibTTS.init(
            view.context,
            Locale.CHINA,
            "com.iflytek.vflynote",

            object : TTSImpl.TTSInitListener {
                override fun onInitError() {
                    showGuide()
                }

                override fun onLangUnAvailable() {
                    showGuide()
                }

                override fun onLangAvailable() {
                    hideGuide()
                }
            })

        LibTTS.addTTSSpeakListener(object : TTSImplNotifyWithKey.TTSListener {

            var lastStart = System.currentTimeMillis()
            override fun onStart(utteranceId: String, data: String) {
                lastStart = System.currentTimeMillis()
                ZLog.d(TAG, "onStart $data : $lastStart")
            }

            override fun onError(utteranceId: String, data: String) {
                var end = System.currentTimeMillis()
                ZLog.d(TAG, "onError $data : ${lastStart} ${end}  ${end - lastStart}")
            }

            override fun onComplete(utteranceId: String, data: String) {
                var end = System.currentTimeMillis()
                ZLog.d(TAG, "onComplete $data : ${lastStart} ${end}  ${end - lastStart}")
            }

        })
        initViewItem()
    }

    fun showGuide() {
        if (null != APKUtils.getInstalledPackage(context, "com.google.android.tts")) {
            view!!.findViewById<TextView>(R.id.tts_tips).visibility = View.VISIBLE
            view!!.findViewById<Button>(R.id.tts_download).visibility = View.GONE
            view!!.findViewById<Button>(R.id.tts_set).visibility = View.VISIBLE
        } else {
            view!!.findViewById<TextView>(R.id.tts_tips).visibility = View.VISIBLE
            view!!.findViewById<Button>(R.id.tts_download).visibility = View.VISIBLE
            view!!.findViewById<Button>(R.id.tts_set).visibility = View.GONE
        }
    }

    fun hideGuide() {
        if (isRootViewCreated()) {
            view!!.findViewById<TextView>(R.id.tts_tips).visibility = View.GONE
            view!!.findViewById<Button>(R.id.tts_download).visibility = View.GONE
            view!!.findViewById<Button>(R.id.tts_set).visibility = View.GONE
        }

    }


    fun initViewItem() {
        updateTTSTitle()

        view!!.findViewById<Button>(R.id.tts_download)?.setOnClickListener {
            DownloadAPK.startDownloadWithCheckAndProcess(
                activity!!,
                ThemeResourcesManager.getString(R.string.app_name) + ":谷歌TTS下载 ",
                ThemeResourcesManager.getString(R.string.app_name) + ":谷歌TTS下载 ",
                "https://imtt.dd.qq.com/16891/apk/D1A7AE1C0B980EB66278E14008C9A6FF.apk",
                "",
                ""
            )

        }

        view!!.findViewById<Button>(R.id.tts_set)?.setOnClickListener {
            IntentUtils.startSettings(context, "com.android.settings.TTS_SETTINGS")
        }

        view!!.findViewById<Button>(R.id.tts_speak)?.setOnClickListener {
            LibTTS.speak(getTTSData(), TTSConfig.SPEEAK_TYPE_SEQUENCE)
        }

        view!!.findViewById<Button>(R.id.tts_save)?.setOnClickListener {
            LibTTS.save(
                getTTSData(),
                context!!.filesDir.absolutePath + "/audio_" + System.currentTimeMillis() + ".wav"
            )
        }

        view!!.findViewById<Button>(R.id.tts_voice_incre)?.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getConfigSpeechRate() + 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_voice_decre)?.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getConfigSpeechRate() - 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_pitch_incre)?.setOnClickListener {
            LibTTS.setPitch(LibTTS.getConfigPitch() + 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_pitch_decre)?.setOnClickListener {
            LibTTS.setPitch(LibTTS.getConfigPitch() - 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_volume_incre)?.setOnClickListener {
            LibTTS.setVoiceVolume(LibTTS.getConfigVoiceVolume() + 10)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_volume_decre)?.setOnClickListener {
            LibTTS.setVoiceVolume(LibTTS.getConfigVoiceVolume() - 10)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_sequence)?.setOnClickListener {
            LibTTS.speak(getTTSData(), TTSConfig.SPEEAK_TYPE_SEQUENCE)
        }

        view!!.findViewById<Button>(R.id.tts_next)?.setOnClickListener {
            LibTTS.speak(getTTSData(), TTSConfig.SPEEAK_TYPE_NEXT)
        }

        view!!.findViewById<Button>(R.id.tts_now)?.setOnClickListener {
            LibTTS.speak(getTTSData(), TTSConfig.SPEEAK_TYPE_FLUSH)
        }

        view!!.findViewById<Button>(R.id.tts_clear)?.setOnClickListener {
            LibTTS.speak(getTTSData(), TTSConfig.SPEEAK_TYPE_CLEAR)
        }

        TaskManager.getInstance().addTask(object : BaseTask() {
            override fun run() {
                LibTTS.stopSpeak()
            }

            override fun getNextEarlyRunTime(): Int {
                return 0
            }

            override fun getMyInterval(): Int {
                return 10 * 2
            }

            override fun getTaskName(): String {
                return "TTS-DISABLED"
            }
        })
    }

    private fun getTTSData(): TTSData {
        times++
        updateTTSTitle()
        return TTSData(view!!.findViewById<TextView>(R.id.tts_test_text)?.text.toString())
    }

    private fun updateTTSTitle() {
        view!!.findViewById<TextView>(R.id.tts_title)?.text =
            String.format(
                FORMAT,
                LibTTS.getConfigSpeechRate(),
                LibTTS.getConfigPitch(),
                LibTTS.getConfigVoiceVolume(),
                times
            )
    }
}