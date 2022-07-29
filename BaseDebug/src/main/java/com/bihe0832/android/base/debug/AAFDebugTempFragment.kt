/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug


import android.provider.Settings
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.debug.cache.DebugInfoCacheManager
import com.bihe0832.android.base.debug.icon.DebugIcon
import com.bihe0832.android.base.debug.icon.DebugTipsIcon
import com.bihe0832.android.base.debug.ipc.TestIPC1Activity
import com.bihe0832.android.base.debug.ipc.TestIPCActivity
import com.bihe0832.android.base.debug.json.JsonTest
import com.bihe0832.android.base.debug.network.DebugNetworkActivity
import com.bihe0832.android.base.debug.request.ROUTRT_NAME_TEST_HTTP
import com.bihe0832.android.base.debug.touch.TouchRegionActivity
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.log.DebugLogActivity
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieContext.showToast
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.config.OnConfigChangedListener
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.action.FileAction
import com.bihe0832.android.lib.floatview.IconManager
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.sqlite.BaseDBHelper
import com.bihe0832.android.lib.sqlite.impl.CommonDBManager
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.ui.view.ext.getActivity
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.MathUtils
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.utils.encrypt.ZlibUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.utils.time.TimeUtil
import com.bihe0832.android.lib.zip.ZipUtils
import java.io.File


class AAFDebugTempFragment : DebugEnvFragment() {
    val LOG_TAG = "AAFDebugTempFragment"

    val configListener = object : OnConfigChangedListener {
        override fun onValueChanged(key: String?, value: String?) {
            ZLog.d("zixie", "onNewValue config key: $key value: $value")
        }

        override fun onValueAgain(key: String?, value: String?) {
            ZLog.d("zixie", "onValueSetted config key: $key value: $value")
        }

    }

    override fun initView(view: View) {
        super.initView(view)
        DebugLogTips.showView(mDebugTips, false)
        Config.addOnConfigChangedListener(configListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Config.removeOnConfigChangedListener(configListener)
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("简单测试函数", View.OnClickListener { testFunc() }))
            add(DebugItemData("通用测试预处理", View.OnClickListener { preTest(it) }))
            add(DebugItemData("测试自定义请求", View.OnClickListener { testOneRequest() }))
            add(DebugItemData("数据读取缓存", View.OnClickListener { testCache() }))

            add(DebugItemData("文本查看器", View.OnClickListener { testEdit() }))
            add(DebugItemData("Assets 操作", View.OnClickListener { testAssets() }))


            add(DebugItemData("Sqlite测试", View.OnClickListener { testDB() }))
            add(DebugItemData("数据压缩解压", View.OnClickListener { testZlib() }))
            add(DebugItemData("数据时间转换", View.OnClickListener { testConvert() }))
            add(DebugItemData("数据百分比转化", View.OnClickListener { testPercent() }))
            add(DebugItemData("展示悬浮窗", View.OnClickListener { showIcon() }))
            add(DebugItemData("隐藏悬浮窗", View.OnClickListener { hideIcon() }))
            add(DebugItemData("自定义日志管理", View.OnClickListener {
                startActivity(DebugLogActivity::class.java)
            }))
            add(DebugItemData("定时任务测试", View.OnClickListener { testTask() }))
            add(
                    DebugItemData(
                            "默认关于页",
                            View.OnClickListener { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT) })
            )
            add(
                    DebugItemData(
                            "网络切换监控",
                            View.OnClickListener { startActivity(DebugNetworkActivity::class.java) })
            )
            add(DebugItemData("打开应用安装界面", View.OnClickListener {
                IntentUtils.startAppSettings(
                        context,
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                )
            }))
            add(DebugItemData("TextView对HTML的支持测试", View.OnClickListener {
                showInputDialog("TextView对HTML的支持测试",
                        "请在输入框输入需要验证的文本内容，无需特殊编码",
                        "<font color='#428bca'>测试文字加粗</font> <BR> 正常的文字效果<BR> <b>测试文字加粗</b> <em>文字斜体</em> <p><font color='#428bca'>修改文字颜色</font></p>",
                        object : InputDialogCompletedCallback {
                            override fun onInputCompleted(result: String?) {
                                DebugTools.showInfoWithHTML(
                                        context,
                                        "TextView对HTML的支持测试",
                                        result,
                                        "分享给我们"
                                )
                            }

                        })
            }))

            add(DebugItemData("点击区扩大Demo", View.OnClickListener {
                startActivity(TouchRegionActivity::class.java)
            }))

            add(DebugItemData("HTTP Request", View.OnClickListener {
                RouterHelper.openPageByRouter(ROUTRT_NAME_TEST_HTTP)
            }))

            add(DebugItemData("文件MD5", View.OnClickListener {
                testMD5()
            }))

            add(DebugItemData("文件选择", View.OnClickListener {
//                activity?.showPhotoChooser()
            }))

            add(DebugItemData("JsonHelper", View.OnClickListener { testJson() }))
            add(DebugItemData("Toast测试", View.OnClickListener {
                ToastUtil.showTop(
                        context,
                        "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>",
                        Toast.LENGTH_LONG
                )
            }))
            add(DebugItemData("ZIP测试", View.OnClickListener { testZIP() }))
            add(DebugItemData("配置 Config 管理测试", View.OnClickListener { testConfig() }))
            add(DebugItemData("应用前后台信息", View.OnClickListener { testAPPObserver() }))
            add(
                    DebugItemData(
                            "多进程",
                            View.OnClickListener { startActivity(TestIPCActivity::class.java) })
            )
            add(
                    DebugItemData(
                            "多进程1",
                            View.OnClickListener { startActivity(TestIPC1Activity::class.java) })
            )
            add(DebugItemData("模拟环境切换", View.OnClickListener {
                mutableListOf<String>().apply {
                    add("测试环境1")
                    add("测试环境2")
                }.let { data ->
                    getChangeEnvSelectDialog("查看应用版本及环境", data, 0, object : DebugEnvFragment.OnEnvChangedListener {
                        override fun onChanged(index: Int) {
                            showChangeEnvDialog("环境切换", data.get(index))
                        }
                    }).let {
                        it.show()
                    }
                }
            }))
        }
    }


    val mIcon by lazy {
        DebugIcon(activity)
    }
    val mIconManager by lazy {
        IconManager(activity!!, mIcon).apply {
            setIconClickListener(View.OnClickListener {
                ZixieContext.showToast("点了一下Icon")
            })
        }
    }

    val mDebugTips by lazy {
        DebugTipsIcon(activity!!)
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
        var result = JsonHelper.fromJsonList<JsonTest>(
                "[" +
                        "{\"key\": \"value1\",\"value1\": [1222,2222],\"value\":true}," +
                        "{\"key\": 2,\"value1\": [1222,2222],\"value2\":1}," +
                        "{\"key\": 3,\"value1\": [1222,2222],\"value2\":\"true\"}," +
                        "{\"key\": 4,\"value1\": [1222,2222],\"value2\":\"1\"}," +
                        "{\"key\": 5,\"value1\": [1222,2222],\"value2\":\"0\"}," +
                        "{\"key\": 6,\"value1\": [1222,2222],\"value2\":false}," +
                        "{\"key\": 7,\"value1\": [1222,2222],\"value2\":0}," +
                        "{\"key\": 8,\"value1\": [1222,2222],\"value2\":\"false\"}" +
                        "]", JsonTest::class.java
        )
        ZLog.d(LOG_TAG, "result:" + result)
        JsonTest().apply {
            key = 1212
        }.let {
            ZLog.d(LOG_TAG, "result:" + JsonHelper.toJson(it))
        }

    }

    private fun testZIP() {

        var startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927.zip",
                "/sdcard/Download/com.herogame.gplay.lastdayrulessurvival_20200927"
        )
        var duration = System.currentTimeMillis() - startTime
        ZLog.d(
                LOG_TAG,
                "ZipCompressor unzip com.herogame.gplay.lastdayrulessurvival_20200927.zip cost:$duration"
        )

        startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/com.garena.game.kgtw.zip",
                "/sdcard/Download/com.garena.game.kgtw"
        )
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip com.garena.game.kgtw.zip cost:$duration")

        startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/com.supercell.brawlstars.zip",
                "/sdcard/Download/com.supercell.brawlstars"
        )
        duration = System.currentTimeMillis() - startTime
        ZLog.d(LOG_TAG, "ZipCompressor unzip com.supercell.brawlstars.zip cost:$duration")

        startTime = System.currentTimeMillis()
        ZipUtils.unCompress(
                "/sdcard/Download/jp.co.sumzap.pj0007.zip",
                "/sdcard/Download/jp.co.sumzap.pj0007"
        )
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
            Config.writeConfig(key, Config.isSwitchEnabled(key, false))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun testAPPObserver() {
        ZLog.d("testAPPObserver", "getAPPStartTime ： ${ApplicationObserver.getAPPStartTime()}")
        ZLog.d("testAPPObserver", "getLastPauseTime ： ${ApplicationObserver.getLastPauseTime()}")
        ZLog.d(
                "testAPPObserver",
                "getLastResumedTime ： ${ApplicationObserver.getLastResumedTime()}"
        )
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

                    override fun runAfterAdd(): Boolean {
                        return false
                    }

                    override fun run() {
                        ZLog.d("TaskManager", "TASK_NAME $i ${this.hashCode()}")
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

        ThreadManager.getInstance().start({
            TaskManager.getInstance().removeTask(TASK_NAME)
        }, 60)


    }

    private fun showIcon() {
        mIconManager.showIconWithPermissionCheck(null)
        mDebugTips.append("<B>提示信息</B>:<BR>    ")
        DebugLogTips.append("<B>提示信息</B> fs df d fsdf:     ")
        mIcon.setHasNew(true)
    }

    private fun hideIcon() {
        mIconManager.hideIcon()
        mDebugTips.show("")
        DebugLogTips.show("")
    }

    fun testConvert() {

        mutableListOf(
                1645771904111,
                1345775904112,
                1625775904313,
                1645775304114,
                1645772904115,
                1645772404116
        ).forEach { data ->
            ZLog.d(
                    "testDateUtil",
                    "$data trans result is:" + DateUtil.getDateCompareResult(data.toLong())
            )
            ZLog.d(
                    "testDateUtil",
                    "$data trans result is:" + DateUtil.getDateCompareResult1(data.toLong())
            )
            ZLog.d("testDateUtil", "$data trans result is:" + DateUtil.getDateCompareResult2(data))
        }


        mutableListOf(
                "1",
                "-1",
                "-1",
                "0",
                "233",
                "true",
                "tRUe",
                "false",
                "False"
        ).forEach { data ->
            ZLog.d("testConvert", data + " result is:" + ConvertUtils.parseBoolean(data, false))
            ZLog.d("testConvert", data + " result is:" + ConvertUtils.parseBoolean(data, true))
        }

        mutableListOf(1, 37, 67, 2434, 24064, 2403564).forEach {
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong())
            )
            ZLog.d(
                    "formatSecondsTo00", "Value $it trans to :" + TimeUtil.formatSecondsTo00(
                    it.toLong(),
                    false,
                    false,
                    false
            )
            )
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong(), false, false, true)
            )
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong(), true, true, false)
            )
            ZLog.d(
                    "formatSecondsTo00",
                    "Value $it trans to :" + TimeUtil.formatSecondsTo00(it.toLong(), true, true, true)
            )
        }
        mutableListOf(1, 37, 67, 2434, 3600, 3602, 24064, 86400, 86440, 2403564).forEach {
            ZLog.d(
                    "formatSecondsToDurationDesc",
                    "Value $it trans to :" + TimeUtil.formatSecondsToDurationDesc(context, it.toLong())
            )
        }
    }


    private fun testZlib() {
        val builder = StringBuilder()
        for (i in 0..50) {
            builder.append('a' + (TextFactoryUtils.getRandomString(26)))
        }
        val text = builder.toString()

        val compres = ZlibUtil.compress(text.toByteArray())
        ZLog.d("testZlib", "compres 前后： " + compres.size + " : " + text.toByteArray().size)

        val b = Base64.encode(ZlibUtil.compress(text.toByteArray()), Base64.DEFAULT)
        val uncompressResult = String(ZlibUtil.uncompress(Base64.decode(b, Base64.DEFAULT)))


        val res = String(ZlibUtil.uncompress(compres))
        ZLog.d("testZlib", "压缩再解压一致性确认：")
        ZLog.d("testZlib", "text：\n$text\n\n")
        ZLog.d("testZlib", "result：\n$res\n\n")

    }

    fun testCache() {
        DebugInfoCacheManager.loggerData()
    }

    fun testPercent() {
        testPercentItem(MathUtils.getFormatPercent(1, 1, 4))
        testPercentItem(MathUtils.getFormatPercent(3, 1, 4))
        testPercentItem(MathUtils.getFormatPercent(3, 0, 4))
        testPercentItem(MathUtils.getFormatPercent(0, 3, 4))
        testPercentItem(MathUtils.getFormatPercent(1L, 3L, 4))
        testPercentItem(MathUtils.getFormatPercent(212121212121212121, 32121212121212121, 4))
        testPercentItem(MathUtils.getFormatPercent(4344343.43434, 4344343.43434, 4))
        testPercentItem(MathUtils.getFormatPercent(43443434343434343.43434, 434434343434343.43434, 4))
    }

    private fun testPercentItem(data: Float) {
        data.let {
            ZLog.d("testPercent", data.toString() + " " + MathUtils.getFormatPercentDesc(data))
        }
    }

    private fun testOneRequest() {

    }

    private fun preTest(itemView: View) {
        showToast(itemView.getActivity()?.getLocalClassName() ?: "")


    }

    private fun testDB(){
        System.currentTimeMillis().let {
            CommonDBManager.saveData("sss" + it, "Fsdfsd")
            CommonDBManager.getData("sss" + it).let {
                ZLog.d(BaseDBHelper.TAG, it.toString())
            }
            CommonDBManager.getAll().forEach {
                ZLog.d(BaseDBHelper.TAG, it.toString())
            }
        }
    }

    fun testEdit() {
        RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_EDITOR)
    }

    private fun testAssets() {
        File(ZixieContext.getLogFolder()).listFiles().forEach { file ->
            ZLog.d("testAssets", file.absolutePath)
        }

        ZLog.d("testAssets", ZixieContext.getLogFolder())
        var path = ZixieContext.getLogFolder() + "config.default"
        FileAction.copyAssetsFileToPath(context, "config.default", path).let {
            ZLog.d("testAssets", " ${FileUtils.getAssetFileContent(context!!,"config.default")}")
            ZLog.d("testAssets", " ${FileUtils.getFileContent(path)}")
            ZLog.d("testAssets", " $it")
            ZLog.d("testAssets", " " + FileUtils.checkFileExist(path))
            FileUtils.deleteFile(path)
        }

        FileAction.copyAssetsFolderToFolder(context, "", ZixieContext.getLogFolder()).let {
            ZLog.d("testAssets", " ")
            File(ZixieContext.getLogFolder()).listFiles().forEach { file ->
                ZLog.d("testAssets", file.absolutePath)
            }
        }
    }

    private fun testFunc() {
        AAFLoggerFile.log("Test0", "This is a test log for Test by ${Thread.currentThread().id}")

//        PermissionManager.checkPermission(activity, Manifest.permission.RECORD_AUDIO)

//        FileUtils.checkAndCreateFolder(ZixieContext.getZixieExtFolder() + "pictures" + File.separator + "m3u8" + File.separator + System.currentTimeMillis())
//        CommonDBManager.getAll().forEach {
//            ZLog.d("zixie" ,it.toString())
//        }

//        ZLog.d("3 " + "3".toFloat() + " " + ConvertUtils.parseFloat("3", 0f))
//        ZLog.d("3 " + "3".toDouble() + " " + ConvertUtils.parseDouble("3", 0.0))
//        ZLog.d("3.6 " + "3.6".toFloat() + " " + ConvertUtils.parseFloat("3.6", 0f))
//        ZLog.d("0.6 " + "0.6".toFloat() + " " + ConvertUtils.parseFloat("0.6.1", 0f))
//        ZLog.d("0.61 " + "0.61".toFloat() + " " + ConvertUtils.parseFloat("0.61", 0f))
//        ZLog.d("3.6 " + "3.6".toDouble() + " " + ConvertUtils.parseDouble("3.6", 0.0))
//        ZLog.d("0.6 " + "0.6".toDouble() + " " + ConvertUtils.parseDouble("0.6.1", 0.0))
//        ZLog.d("0.61 " + "0.61".toDouble() + " " + ConvertUtils.parseDouble("0.61", 0.0))
//        IntentUtils.startAppSettings(activity!!, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)


//        val v1 = "1.0.1"
//        val v2 = "1.0.02"
//        val v2_1 = "1.0.002.1"
//        val v2_2 = "1.0.2.02"
//        val v3 = "1.0.3"
//
//        ZLog.d("testVerion","v1 VS v1:" + APKUtils.compareVersion(v1, v1))
//        ZLog.d("testVerion","v1 VS v2:" + APKUtils.compareVersion(v1, v2))
//        ZLog.d("testVerion","v2 VS v1:" + APKUtils.compareVersion(v2, v1))
//        ZLog.d("testVerion","v2_1 VS v1:" + APKUtils.compareVersion(v2_1, v1))
//        ZLog.d("testVerion","v2_1 VS v2:" + APKUtils.compareVersion(v2_1, v2))
//        ZLog.d("testVerion","v2_2 VS v2_1:" + APKUtils.compareVersion(v2_2, v2_1))
//        ZLog.d("testVerion","v3 VS v2:" + APKUtils.compareVersion(v3, v2))
//        ZLog.d("testVerion","v3 VS v2_2:" + APKUtils.compareVersion(v3, v2_2))

    }


}