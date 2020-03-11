package com.bihe0832.android.test

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.tts.LibTTS
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.apk.APKUtils
import kotlinx.android.synthetic.main.activity_test_tts.*
import java.util.*

class TestTTSActivity : Activity() {
    private val TAG = "TestTTSFragment-> "
    private val FORMAT = "语音播报测试：语速 %s,语调 %s，最大ID %s "
    private val MY_LOCALE = Locale.CHINA
    private var times = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //透明导航栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        setContentView(R.layout.activity_test_tts)

        LibTTS.init(this, Locale.CHINA, object : LibTTS.TTSInitListener {
            override fun onInitError() {
                showGuide()
            }

            override fun onLangUnAvaiavble() {
                showGuide()
            }

            override fun onLangAvaiavble() {
                hideGuide()
            }
        })

        LibTTS.addTTSSpeakListener(object : LibTTS.TTSSpeakListener {

            var lastStart = System.currentTimeMillis()
            override fun onUtteranceStart(utteranceId: String) {
                lastStart = System.currentTimeMillis()
                Log.d(TAG,"onStart $utteranceId : $lastStart")
            }

            override fun onUtteranceDone(utteranceId: String) {
                var end = System.currentTimeMillis()
                Log.d(TAG,"onDone $utteranceId : ${lastStart} ${end}  ${end - lastStart}")
            }

            override fun onUtteranceError(utteranceId: String) {
                var end = System.currentTimeMillis()
                Log.d(TAG,"onError $utteranceId : ${lastStart} ${end}  ${end - lastStart}")
            }

        })
        initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showGuide() {
        if (null != APKUtils.getInstalledPackage(applicationContext, "com.google.android.tts")) {
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


    fun initView() {
        updateTTSTitle()

        tts_download?.setOnClickListener {
            DownloadItem().apply {
                notificationVisibility = DownloadManager.Request.VISIBILITY_VISIBLE
                dowmloadTitle = applicationContext.getString(R.string.app_name) + ":谷歌TTS下载 "
                fileName = "com.google.android.tts.apk"
                downloadURL = "https://imtt.dd.qq.com/16891/apk/D1A7AE1C0B980EB66278E14008C9A6FF.apk"
            }.let {
                DownloadUtils.startDownload(applicationContext, it, object : DownloadListener {
                    override fun onProgress(total: Long, cur: Long) {
                    }

                    override fun onSuccess(finalFileName: String) {
                        Log.d(TAG,"startDownloadApk download installApkPath: $finalFileName")
                        InstallUtils.installAPP(applicationContext, finalFileName)
                    }

                    override fun onError(error: Int, errmsg: String) {
                        ToastUtil.showShort(this@TestTTSActivity, "下载失败（$error）")
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

        tts_speak.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_SEQUENCE)
        }

        tts_save.setOnClickListener {
            LibTTS.save(getMsg(), applicationContext.filesDir.absolutePath + "/audio_" + System.currentTimeMillis() + ".wav")
        }

        tts_voice_incre.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getSpeechRate() + 0.1f)
            updateTTSTitle()
        }

        tts_voice_decre.setOnClickListener {
            LibTTS.setSpeechRate(LibTTS.getSpeechRate() - 0.1f)
            updateTTSTitle()
        }


        tts_pitch_incre.setOnClickListener {
            LibTTS.setPitch(LibTTS.getPitch() + 0.1f)
            updateTTSTitle()
        }

        tts_pitch_decre.setOnClickListener {
            LibTTS.setPitch(LibTTS.getPitch() - 0.1f)
            updateTTSTitle()
        }

        tts_sequence.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_SEQUENCE)
        }

        tts_next.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_NEXT)
        }

        tts_now.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_FLUSH)
        }

        tts_clear.setOnClickListener {
            LibTTS.speak(getMsg(), LibTTS.SPEEAK_TYPE_CLEAR)
        }
    }

    private fun getMsg(): String {
        times++
        updateTTSTitle()
        return tts_test_text.text.toString() + times
    }

    private fun updateTTSTitle() {
        tts_title.text = String.format(FORMAT, LibTTS.getSpeechRate(), LibTTS.getPitch(), times)
    }

}
