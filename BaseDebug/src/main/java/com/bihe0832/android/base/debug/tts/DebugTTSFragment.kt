package com.bihe0832.android.base.debug.tts

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.download.wrapper.DownloadAPK
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.tts.LibTTS
import com.bihe0832.android.lib.tts.TTSData
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.util.*

class DebugTTSFragment : BaseFragment() {
    val TAG = this.javaClass.simpleName
    private val FORMAT = "语音播报测试：语速 %s,语速 %s,音量 %s，最大ID %s "

    private var times = 0
    private var volume = 0.5f

    override fun getLayoutID(): Int {
        return R.layout.fragment_test_tts
    }

    override fun initView(view: View) {
        LibTTS.init(
            view.context,
            Locale.CHINA,
            "com.iflytek.vflynote",
            object : LibTTS.TTSInitListener {
                override fun onInitError() {
                    showGuide()
                }

                override fun onLangUnAvailable() {
                    showGuide()
                }

                override fun onLangAvailable() {
                    hideGuide()
                }

                override fun onTTSError() {
                    ZixieContext.showToast("TTS引擎异常，正在重新初始化")
                }
            })

        LibTTS.addTTSSpeakListener(object : LibTTS.TTSSpeakListener {

            var lastStart = System.currentTimeMillis()
            override fun onUtteranceStart(utteranceId: String) {
                lastStart = System.currentTimeMillis()
                ZLog.d(TAG, "onStart $utteranceId : $lastStart")
            }

            override fun onUtteranceDone(utteranceId: String) {
                var end = System.currentTimeMillis()
                ZLog.d(TAG, "onDone $utteranceId : ${lastStart} ${end}  ${end - lastStart}")
            }

            override fun onUtteranceError(utteranceId: String) {
                var end = System.currentTimeMillis()
                ZLog.d(TAG, "onError $utteranceId : ${lastStart} ${end}  ${end - lastStart}")
            }

            override fun onUtteranceFailed(utteranceId: String, textSpeak: String) {
                var end = System.currentTimeMillis()
                ZLog.d(TAG, "onError $utteranceId : ${textSpeak}")
            }

        })
        initViewItem()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
            view!!.findViewById<Button>(R.id.tts_set).visibility= View.GONE
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
            LibTTS.speak(getTTSData(), LibTTS.SPEEAK_TYPE_SEQUENCE)
        }

        view!!.findViewById<Button>(R.id.tts_save)?.setOnClickListener {
            LibTTS.save(
                getTTSData(),
                context!!.filesDir.absolutePath + "/audio_" + System.currentTimeMillis() + ".wav"
            )
        }

        view!!.findViewById<Button>(R.id.tts_voice_incre)?.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getSpeechRate() + 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_voice_decre)?.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getSpeechRate() - 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_pitch_incre)?.setOnClickListener {
            LibTTS.setPitch(LibTTS.getPitch() + 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_pitch_decre)?.setOnClickListener {
            LibTTS.setPitch(LibTTS.getPitch() - 0.1f)
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_volume_incre)?.setOnClickListener {
            volume += 0.1f
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_volume_decre)?.setOnClickListener {
            volume -= 0.1f
            updateTTSTitle()
        }

        view!!.findViewById<Button>(R.id.tts_sequence)?.setOnClickListener {
            LibTTS.speak(getTTSData(), LibTTS.SPEEAK_TYPE_SEQUENCE)
        }

        view!!.findViewById<Button>(R.id.tts_next)?.setOnClickListener {
            LibTTS.speak(getTTSData(), LibTTS.SPEEAK_TYPE_NEXT)
        }

        view!!.findViewById<Button>(R.id.tts_now)?.setOnClickListener {
            LibTTS.speak(getTTSData(), LibTTS.SPEEAK_TYPE_FLUSH)
        }

        view!!.findViewById<Button>(R.id.tts_clear)?.setOnClickListener {
            LibTTS.speak(getTTSData(), LibTTS.SPEEAK_TYPE_CLEAR)
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
        return TTSData(view!!.findViewById<TextView>(R.id.tts_test_text)?.text.toString()).apply {
            addSpeakParams(Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
            })
        }
    }

    private fun updateTTSTitle() {
        view!!.findViewById<TextView>(R.id.tts_title)?.text = String.format(FORMAT, LibTTS.getSpeechRate(), LibTTS.getPitch(), volume, times)
    }
}