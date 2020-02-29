package com.bihe0832.android.test

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.thread.ThreadManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.time.Duration


class MainActivity : Activity() {
    val LOG_TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }

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
                    ThreadManager.getInstance().runOnUIThread {
                        showResult("startDownloadApk download installApkPath: $finalFileName")
                        if (it.id == doActionWithMyProvider.id) {
                            var photoURI = FileProvider.getUriForFile(this@MainActivity, "com.bihe0832.android.test.bihe0832.test", File(finalFileName))
                            InstallUtils.installAPP(this@MainActivity, photoURI, File(finalFileName))
                        }

                        if (it.id == doActionWithLibProvider.id) {
                            InstallUtils.installAPP(this@MainActivity, finalFileName)
                        }
                    }
                }

                override fun onError(error: Int, errmsg: String) {
                    showResult("应用下载失败（$error）")
                }
            })
        }
        doActionWithMyProvider.setOnClickListener(a)
        doActionWithLibProvider.setOnClickListener(a)
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
