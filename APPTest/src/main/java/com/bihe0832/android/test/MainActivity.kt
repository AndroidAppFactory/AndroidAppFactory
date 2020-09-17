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
import android.text.Html
import android.view.View
import android.widget.Toast
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.router.Routers
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.encypt.MD5
import com.bihe0832.android.test.module.JsonTest
import com.bihe0832.android.test.module.testNotifyProcess
import com.flyco.tablayout.listener.CustomTabEntity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

@APPMain
@Module("MainActivity")
class MainActivity : Activity() {
    val LOG_TAG = "TestHttpActivity"
    var process = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ZLog.setDebug(true)
        Config.init(applicationContext, "", true)

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                ZLog.d(LOG_TAG, "user input:$intent")
                intent?.let {
                    val notificationId = it.getIntExtra(NotifyManager.NOTIFICATION_ID_KEY, -1)
                    val action = it.getStringExtra(NotifyManager.ACTION_KEY)
                    ZLog.d(LOG_TAG, "[DownloadNotificationsManager] onReceive: $action")
                    when (action) {
                        NotifyManager.ACTION_RESUME -> {
                            testNotifyProcess(notificationId)
                        }

                        NotifyManager.ACTION_PAUSE -> {
                            TaskManager.getInstance().removeTask(LOG_TAG)
                            NotifyManager.sendDownloadNotify(applicationContext,
                                    "王者荣耀",
                                    "https://blog.bihe0832.com/public/img/head.jpg", 1000000, 2345600789, 239909 * process.toLong(), process, NotifyManager.DOWNLOAD_TYPE_PAUSED, "download", notificationId)
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

        View.OnClickListener {
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
        }.let {
            doActionWithMyProvider.setOnClickListener(it)
            doActionWithLibProvider.setOnClickListener(it)
        }


        speak.setOnClickListener {
            startActivity(TestTTSActivity::class.java)
        }

        httptest.setOnClickListener {
            startActivity(TestHttpActivity::class.java)
        }


        testToast.setOnClickListener {
            ToastUtil.showTop(this, "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>", Toast.LENGTH_LONG)
        }

        testFun.setOnClickListener {
//            testNotifyProcess(0)
            ZLog.d(LOG_TAG, "result:" + Routers.getMainActivityList().size)
//            Routers.open(this,"mna://TestHttpActivity")
            APKUtils.startApp(this,"com.tencent",true)
        }

        val mTitles = arrayOf("首页", "消息", "联系人", "更多")
        val mTabTextSizes = floatArrayOf(
                10f, 20f, 20f, 10f)

        val tabEntities = ArrayList<CustomTabEntity>()
        for (i in mTitles.indices) {
            tabEntities.add(TabEntity(mTitles[i], mTabTextSizes[i]))
        }

        commonTabLayout.apply {
            setTabData(tabEntities)
            showMsg(1, "免费")
            setMsgMargin(1, -15f, 5f)
            showMsg(2, 100)
            setMsgMargin(2, -15f, 5f)
            showMsg(3, 5)
        }
    }

    inner class TabEntity(var title: String, var tabTs: Float) : CustomTabEntity {

        override fun getTabTitle(): String {
            return title
        }

        override fun getTabTextSize(): Float {
            return tabTs
        }

        override fun getTabSelectedIcon(): Int {
            return R.mipmap.icon
        }

        override fun getTabUnselectedIcon(): Int {
            return R.mipmap.icon
        }
    }


    private fun testJson() {
//        for (i in 0..100) {
//            var start = System.currentTimeMillis()
//            ZLog.d(LOG_TAG, "JsonHelper: start $start")
//            JsonHelper.getGson().fromJson("{\"key\": 1222}", JsonTest::class.java)
//            var end = System.currentTimeMillis()
//            ZLog.d(LOG_TAG, "JsonHelper: end $end; duration : ${end - start}")
//        }
        var result = JsonHelper.fromJsonList<JsonTest>("[{\"key\": 1111,\"value\": [1222,2222]},{\"key\": 2222,\"value\": [1222,2222]}]", JsonTest::class.java)
        ZLog.d(LOG_TAG, "result:" + result)
        JsonTest().apply {
            key = 1212
        }.let {
            ZLog.d(LOG_TAG, "result:" + JsonHelper.toJson(it))
        }
    }

    private fun testMD5() {
        File("/sdcard/screen.png").let {
            ZLog.d(LOG_TAG, MD5.getFileMD5(it))
            ZLog.d(LOG_TAG, MD5.getFileMD5(it, 0, it.length()))
        }

        ThreadManager.getInstance().start {
            File("/sdcard/10053761_com.tencent.hjzqgame_h759087_1.0.1306_lcbw83.apk").let {
                ZLog.d(LOG_TAG, "===============start==================")
                var start = System.currentTimeMillis() / 1000
                for (i in 0..5) {
                    ZLog.d(LOG_TAG, MD5.getFileMD5(it))
                }

                ZLog.d(LOG_TAG, "total time : " + (System.currentTimeMillis() / 1000 - start))
                ZLog.d(LOG_TAG, "===============end==================")
                ZLog.d(LOG_TAG, "===============start==================")
                start = System.currentTimeMillis() / 1000
                for (i in 0..5) {
                    ZLog.d(LOG_TAG, MD5.getFileMD5(it, 0, it.length()))
                }
                ZLog.d(LOG_TAG, "total time : " + (System.currentTimeMillis() / 1000 - start))
                ZLog.d(LOG_TAG, "===============end==================")

            }
        }
    }

    private fun testTextView() {
        var data =
                "正常的文字效果<BR>" +
                "<p>正常的文字效果</p>" +
                "<p><b>文字加粗</b></p>" +
                "<p><em>文字斜体</em></p>" +
                "<p><font color='#428bca'>修改文字颜色</font></p>"

        testResult.text = Html.fromHtml(data)

    }

    private fun userInput(): String? {
        var input = testInput.text?.toString()
        return if (input?.isEmpty() == true) {
            Toast.makeText(this, "user input is bad!", Toast.LENGTH_LONG).show()
            ""
        } else {
            ZLog.d(LOG_TAG, "user input:$input")
            input
        }
    }

    private fun showResult(s: String?) {
        s?.let {
            ZLog.d(LOG_TAG, "showResult:$s")
            testResult.text = "Result: $s"
        }
    }

    private fun startActivity(cls: Class<*>?) {
        val intent = Intent(this, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}
