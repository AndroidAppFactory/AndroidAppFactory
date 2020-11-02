package com.bihe0832.android.test.module

import android.os.Bundle
import android.widget.Toast
import com.bihe0832.android.app.router.APPFactoryRouter
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.encypt.MD5
import com.bihe0832.android.lib.zip.ZipUtils
import com.bihe0832.android.test.module.request.TestHttpActivity
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.TestItem
import com.bihe0832.android.test.module.json.JsonTest
import com.bihe0832.android.test.module.request.ROUTRT_NAME_TEST_HTTP
import com.bihe0832.android.test.module.touch.TouchRegionActivity
import java.io.File

class TestDebugTempFragment : BaseTestFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): TestDebugTempFragment {
            val args = Bundle()
            val fragment = TestDebugTempFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getDataList(): List<TestItem> {
        return mutableListOf<TestItem>().apply {
            add(TestItem("简单测试函数") { testFunc() })
            add(TestItem("通用测试预处理") { preTest() })
            add(TestItem("测试自定义请求") { testOneRequest() })

            add(TestItem("点击区扩大Demo") {
                startActivity(TouchRegionActivity::class.java)
            })

            add(TestItem("HTTP Request") {
                APPFactoryRouter.openPageByRouter(ROUTRT_NAME_TEST_HTTP)
            })

            add(TestItem("文件MD5") {
                testMD5()
            })

            add(TestItem("JsonHelper") { testJson() })
            add(TestItem("Toast测试") {
                ToastUtil.showTop(context, "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>", Toast.LENGTH_LONG)
            })
            add(TestItem("ZIP测试") { testZIP() })

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

    private fun testTextView() {
        var data =
                "正常的文字效果<BR>" +
                        "<p>正常的文字效果</p>" +
                        "<p><b>文字加粗</b></p>" +
                        "<p><em>文字斜体</em></p>" +
                        "<p><font color='#428bca'>修改文字颜色</font></p>"

//        testResult?.text = Html.fromHtml(data)
    }



    private fun testOneRequest() {
    }

    private fun preTest() {

    }

    private fun testFunc() {
        ZLog.d("test")
    }

}