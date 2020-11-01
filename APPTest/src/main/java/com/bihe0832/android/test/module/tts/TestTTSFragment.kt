package com.bihe0832.android.test.module.tts

import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.tts.LibTTS
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.test.R
import kotlinx.android.synthetic.main.fragment_test_tts.*
import java.util.*

class TestTTSFragment : BaseFragment() {
    val TAG = "TestTTSFragment"
    private val FORMAT = "语音播报测试：语速 %s,语调 %s，最大ID %s "

    private var times = 0

    companion object {
        fun newInstance(): TestTTSFragment {
            val args = Bundle()
            val fragment = TestTTSFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test_tts, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }
    private fun initView(view: View) {
        LibTTS.init(view.context, Locale.CHINA, "com.iflytek.vflynote", object : LibTTS.TTSInitListener {
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
            tts_tips.visibility = View.VISIBLE
            tts_download.visibility = View.GONE
            tts_set.visibility = View.VISIBLE
        } else {
            tts_tips.visibility = View.VISIBLE
            tts_download.visibility = View.VISIBLE
            tts_set.visibility = View.GONE
        }
    }

    fun hideGuide() {
        tts_tips.visibility = View.GONE
        tts_download.visibility = View.GONE
        tts_set.visibility = View.GONE
    }


    fun initViewItem() {
        updateTTSTitle()

        tts_download?.setOnClickListener {
            DownloadItem().apply {
                notificationVisibility = DownloadManager.Request.VISIBILITY_VISIBLE
                dowmloadTitle = context!!.getString(R.string.app_name) + ":谷歌TTS下载 "
                fileName = "com.google.android.tts.apk"
                downloadURL = "https://imtt.dd.qq.com/16891/apk/D1A7AE1C0B980EB66278E14008C9A6FF.apk"
            }.let {
                DownloadUtils.startDownload(context!!, it, object : DownloadListener {
                    override fun onProgress(total: Long, cur: Long) {
                        ZLog.d(TAG, "startDownloadApk download onProgress: $cur")
                    }

                    override fun onSuccess(finalFileName: String) {
                        ZLog.d(TAG, "startDownloadApk download installApkPath: $finalFileName")
                        InstallUtils.installAPP(context, finalFileName)
                    }

                    override fun onError(error: Int, errmsg: String) {
                        ZixieContext.showToast("下载失败（$error）")
                    }
                })
            }

        }

        tts_set?.setOnClickListener {
            Intent().apply {
                setAction("com.android.settings.TTS_SETTINGS")
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.let {
                startActivity(it)
            }
        }

        tts_speak?.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_SEQUENCE)
        }

        tts_save?.setOnClickListener {
            LibTTS.save(getMsg(), context!!.filesDir.absolutePath + "/audio_" + System.currentTimeMillis() + ".wav")
        }

        tts_voice_incre?.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getSpeechRate() + 0.1f)
            updateTTSTitle()
        }

        tts_voice_decre?.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getSpeechRate() - 0.1f)
            updateTTSTitle()
        }


        tts_pitch_incre?.setOnClickListener {
            LibTTS.setPitch(LibTTS.getPitch() + 0.1f)
            updateTTSTitle()
        }

        tts_pitch_decre?.setOnClickListener {
            LibTTS.setPitch(LibTTS.getPitch() - 0.1f)
            updateTTSTitle()
        }

        tts_sequence?.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_SEQUENCE)
        }

        tts_next?.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_NEXT)
        }

        tts_now?.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_FLUSH)
        }

        tts_clear?.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_CLEAR)
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

    private fun getMsg(): String {
        times++
        updateTTSTitle()
        return tts_test_text?.text.toString() + times
    }

    private fun updateTTSTitle() {
        tts_title?.text = String.format(FORMAT, LibTTS.getSpeechRate(), LibTTS.getPitch(), times)
    }


}