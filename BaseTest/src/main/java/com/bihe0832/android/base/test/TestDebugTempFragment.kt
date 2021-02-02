package com.bihe0832.android.base.test

import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.test.json.JsonTest
import com.bihe0832.android.base.test.network.TestNetworkActivity
import com.bihe0832.android.base.test.request.ROUTRT_NAME_TEST_HTTP
import com.bihe0832.android.base.test.touch.TouchRegionActivity
import com.bihe0832.android.common.test.base.BaseTestFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.debug.InputDialogCompletedCallback
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.encypt.MD5
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.zip.ZipUtils
import java.io.File

class TestDebugTempFragment : BaseTestFragment() {
    val LOG_TAG = "TestDebugTempFragment"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("简单测试函数", View.OnClickListener { testFunc() }))
            add(TestItemData("通用测试预处理", View.OnClickListener { preTest() }))
            add(TestItemData("测试自定义请求", View.OnClickListener { testOneRequest() }))
            add(TestItemData("定时任务测试", View.OnClickListener { testTask() }))
            add(TestItemData("默认关于页", View.OnClickListener { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT) }))
            add(TestItemData("网络切换监控", View.OnClickListener { startActivity(TestNetworkActivity::class.java) }))
            add(TestItemData("打开应用安装界面", View.OnClickListener {
                IntentUtils.startAppSettings(context, android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            }))
            add(TestItemData("TextView对HTML的支持测试", View.OnClickListener {
                showInputDialog("TextView对HTML的支持测试",
                        "请在输入框输入需要验证的文本内容，无需特殊编码",
                        "<font color='#428bca'>测试文字加粗</font> <BR> 正常的文字效果<BR> <b>测试文字加粗</b> <em>文字斜体</em> <p><font color='#428bca'>修改文字颜色</font></p>",
                        object : InputDialogCompletedCallback {
                            override fun onInputCompleted(result: String?) {
                                DebugTools.showInfoWithHTML(context, "TextView对HTML的支持测试", result, "分享给我们")
                            }

                        })
            }))

            add(TestItemData("点击区扩大Demo", View.OnClickListener {
                startActivity(TouchRegionActivity::class.java)
            }))

            add(TestItemData("HTTP Request", View.OnClickListener {
                RouterHelper.openPageByRouter(ROUTRT_NAME_TEST_HTTP)
            }))

            add(TestItemData("文件MD5", View.OnClickListener {
                testMD5()
            }))

            add(TestItemData("JsonHelper", View.OnClickListener { testJson() }))
            add(TestItemData("Toast测试", View.OnClickListener {
                ToastUtil.showTop(context, "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>", Toast.LENGTH_LONG)
            }))
            add(TestItemData("ZIP测试", View.OnClickListener { testZIP() }))
            add(TestItemData("配置管理测试", View.OnClickListener { testConfig() }))
            add(TestItemData("应用前后台信息", View.OnClickListener { testAPPObserver() }))


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

    private fun testZIP() {

        var startTime = System.currentTimeMillis()
        ZipUtils.unCompress("/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip", "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927")
        var duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip com.herogame.gplay.lastdayrulessurvival_20200927.zip cost:$duration")

        startTime = System.currentTimeMillis();
        ZipUtils.unCompress("/sdcard/Download/com.garena.game.kgtw.zip", "/sdcard/Download/com.garena.game.kgtw")
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip com.garena.game.kgtw.zip cost:$duration")

        startTime = System.currentTimeMillis();
        ZipUtils.unCompress("/sdcard/Download/com.supercell.brawlstars.zip", "/sdcard/Download/com.supercell.brawlstars")
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip com.supercell.brawlstars.zip cost:$duration")

        startTime = System.currentTimeMillis();
        ZipUtils.unCompress("/sdcard/Download/jp.co.sumzap.pj0007.zip", "/sdcard/Download/jp.co.sumzap.pj0007")
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip jp.co.sumzap.pj0007.zip cost:$duration")
    }

    private fun testConfig() {
        try {
//            var startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.readConfig("test$i", "")
//            }
//            var duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")
//
//            startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.writeConfig("test$i", i.toString())
//            }
//            duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig write 1000 cost:$duration")
//
//            startTime = System.currentTimeMillis()
//            for (i in 0 until 100){
//                Config.readConfig("test$i", "")
//            }
//            duration = System.currentTimeMillis() - startTime
//            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
//            Config.writeConfig("A","testconfig")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
//            Config.writeConfig("A","testconfig")
//            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
            var key = "aaa"
            ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
            Config.writeConfig(key, true)
            ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
            Config.writeConfig(key, false)
            ZLog.d(LOG_TAG, "readConfig A::${Config.isSwitchEnabled(key, false)}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun testAPPObserver() {
        ZLog.d("testAPPObserver", "getAPPStartTime ： ${ApplicationObserver.getAPPStartTime()}")
        ZLog.d("testAPPObserver", "getLastPauseTime ： ${ApplicationObserver.getLastPauseTime()}")
        ZLog.d("testAPPObserver", "getLastResumedTime ： ${ApplicationObserver.getLastResumedTime()}")
        ZLog.d("testAPPObserver", "getCurrentActivity ： ${ActivityObserver.getCurrentActivity()}")
    }

    private fun testTask() {
        val TASK_NAME = "AAA"
        for (i in 0..20) {
            ThreadManager.getInstance().start({
                TaskManager.getInstance().removeTask(TASK_NAME)
                TaskManager.getInstance().addTask(object : BaseTask() {
                    override fun getMyInterval(): Int {
                        return 2
                    }

                    override fun getNextEarlyRunTime(): Int {
                        return 0
                    }

                    override fun run() {
                        ZLog.d("TaskManager", "TASK_NAME $i")
                    }

                    override fun getTaskName(): String {
                        return TASK_NAME
                    }

                })
            }, i * 2)

            ThreadManager.getInstance().start({
                TaskManager.getInstance().removeTask(TASK_NAME)
            }, i * 2 + 2700L)

        }
    }

    private fun testOneRequest() {
    }

    private fun preTest() {

    }

    private fun testFunc() {
        IntentUtils.startAppSettings(context, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
    }

}