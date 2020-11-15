package com.bihe0832.android.test.module

import android.view.View
import android.widget.Toast
import com.bihe0832.android.app.router.APPFactoryRouter
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.encypt.MD5
import com.bihe0832.android.lib.zip.ZipUtils
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.item.TestItemData
import com.bihe0832.android.test.module.json.JsonTest
import com.bihe0832.android.test.module.request.ROUTRT_NAME_TEST_HTTP
import com.bihe0832.android.test.module.touch.TouchRegionActivity
import java.io.File
import java.lang.Exception

class TestDebugTempFragment : BaseTestFragment() {
    val LOG_TAG = "TestDebugTempFragment"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("简单测试函数", View.OnClickListener { testFunc() }))
            add(TestItemData("通用测试预处理", View.OnClickListener { preTest() }))
            add(TestItemData("测试自定义请求", View.OnClickListener { testOneRequest() }))

            add(TestItemData("点击区扩大Demo", View.OnClickListener {
                startActivity(TouchRegionActivity::class.java)
            }))

            add(TestItemData("HTTP Request", View.OnClickListener {
                APPFactoryRouter.openPageByRouter(ROUTRT_NAME_TEST_HTTP)
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

    private fun testConfig(){
        try {
            var startTime = System.currentTimeMillis()
            for (i in 0 until 100){
                Config.readConfig("test$i", "")
            }
            var duration = System.currentTimeMillis() - startTime
            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")

            startTime = System.currentTimeMillis()
            for (i in 0 until 100){
                Config.writeConfig("test$i", i.toString())
            }
            duration = System.currentTimeMillis() - startTime
            ZLog.d(LOG_TAG, "testConfig write 1000 cost:$duration")

            startTime = System.currentTimeMillis()
            for (i in 0 until 100){
                Config.readConfig("test$i", "")
            }
            duration = System.currentTimeMillis() - startTime
            ZLog.d(LOG_TAG, "testConfig read 1000 cost:$duration")
            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
            Config.writeConfig("A","testconfig")
            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
            Config.writeConfig("A","testconfig")
            ZLog.d(LOG_TAG, "readConfig A::${Config.readConfig("A","")}")
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun testOneRequest() {
    }

    private fun preTest() {

    }

    private fun testFunc() {
        ZLog.d("test")
    }

}