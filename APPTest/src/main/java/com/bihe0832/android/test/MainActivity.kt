package com.bihe0832.android.test

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.test.module.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : Activity() {
    val LOG_TAG = "MainActivity"
    var process = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Config.init(applicationContext, "", true)

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(LOG_TAG, "user input:$intent")
                intent?.let {
                    val notificationId = it.getIntExtra(NotifyManager.NOTIFICATION_ID_KEY, -1)
                    val action = it.getStringExtra(NotifyManager.ACTION_KEY)
                    Log.d(LOG_TAG, "[DownloadNotificationsManager] onReceive: $action")
                    when (action) {
                        NotifyManager.ACTION_RESUME -> {
                            testNotifyProcess(notificationId)
                        }

                        NotifyManager.ACTION_PAUSE -> {
                            TaskManager.getInstance().removeTask(LOG_TAG)
                            NotifyManager.sendDownloadNotify(applicationContext,
                                    "王者荣耀",
                                    1000000, 2345600789, 239909 * process.toLong(), process, NotifyManager.DOWNLOAD_TYPE_PAUSED, "download", notificationId)
                        }

                        NotifyManager.ACTION_DELETE -> {
                            TaskManager.getInstance().removeTask(LOG_TAG)
                            NotifyManager.cancleNotify(this@MainActivity, notificationId)
                        }
                        NotifyManager.ACTION_INSTALL -> {
                            TaskManager.getInstance().removeTask(LOG_TAG)
                            NotifyManager.cancleNotify(this@MainActivity, notificationId)
                        }
                        else -> {

                        }
                    }
                }

            }
        }, IntentFilter(NotifyManager.NOTIFICATION_BROADCAST_ACTION))

        val a = View.OnClickListener {
            val item = DownloadItem()
            item.notificationVisibility = DownloadManager.Request.VISIBILITY_VISIBLE
            item.dowmloadTitle = this.getString(R.string.app_name)
            item.downloadDesc = "ffsf"
            item.fileName = "MobileAssistant_1.apk"
            item.downloadURL = "https://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk"
            item.forceDownloadNew = true
            DownloadUtils.startDownload(this, item, object : DownloadListener {
                override fun onProgress(total: Long, cur: Long) {
                    showResult("$cur/$total")
                }

                override fun onSuccess(finalFileName: String) {
//                    var finalFileName: String = applicationContext!!.getExternalFilesDir("zixie/").absolutePath + "/" + "MobileAssistant_1.apk"
                    showResult("startDownloadApk download installApkPath: $finalFileName")
                    if (it.id == doActionWithMyProvider.id) {
                        var photoURI = FileProvider.getUriForFile(this@MainActivity, "com.bihe0832.android.test.bihe0832.test", File(finalFileName))
                        InstallUtils.installAPP(this@MainActivity, photoURI, File(finalFileName))
                    }

                    if (it.id == doActionWithLibProvider.id) {
                        InstallUtils.installAPP(this@MainActivity, finalFileName)
                    }
                }

                override fun onError(error: Int, errmsg: String) {
                    showResult("应用下载失败（$error）")
                }
            })
        }
        doActionWithMyProvider.setOnClickListener(a)
        doActionWithLibProvider.setOnClickListener(a)

        speak.setOnClickListener {
            val intent = Intent(this, TestTTSActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        TaskManager.getInstance().addTask(TestTask())

        testToast.setOnClickListener {
            ToastUtil.showTop(this, "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>", Toast.LENGTH_LONG)
        }

        testFun.setOnClickListener {
            testLoading(this)
        }
    }


    private fun userInput(): String? {
        var input = testInput.text?.toString()
        return if (input?.isEmpty() == true) {
            Toast.makeText(this, "user input is bad!", Toast.LENGTH_LONG).show()
            ""
        } else {
            Log.d(LOG_TAG, "user input:$input")
            input
        }
    }

    private fun showResult(s: String?) {
        s?.let {
            Log.d(LOG_TAG, "showResult:$s")
            testResult.text = "Result: $s"
        }
    }

}
