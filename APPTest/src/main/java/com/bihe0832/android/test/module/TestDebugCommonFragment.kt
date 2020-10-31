package com.bihe0832.android.test.module

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.bihe0832.android.framework.ZixieContext.showDebug
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.InputDialogCompletedCallback
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.zip.ZipUtils
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.TestItem
import com.bihe0832.android.test.module.json.JsonTest

open class TestDebugCommonFragment : BaseTestFragment() {
    private var lastUrl = "https://blog.bihe0832.com"

    companion object {
        @JvmStatic
        fun newInstance(): TestDebugCommonFragment {
            val args = Bundle()
            val fragment = TestDebugCommonFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getDataList(): List<TestItem> {
        return mutableListOf<TestItem>().apply {
            add(TestItem("打开指定Web页面") {
                showInputDialog("打开指定Web页面", "请在输入框输入网页地址后点击“确定”", lastUrl, InputDialogCompletedCallback { result: String ->
                    try {
                        if (!TextUtils.isEmpty(result)) {
                            lastUrl = result
                            openWeb(result)
                        } else {
                            showDebug("请输入正确的网页地址")
                        }
                    } catch (e: Exception) {
                    }
                })
            })
            add(TestItem("打开JSbridge调试页面") { openWeb("https://microdemo.bihe0832.com/jsbridge/index.html") })
            add(TestItem("测试自定义请求") { testOneRequest() })
            add(TestItem("打开TBS调试页面") { openWeb("http://debugtbs.qq.com/") })
            add(TestItem("JsonHelper") { testJson() })
            add(TestItem("Toast测试") {
                ToastUtil.showTop(context, "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>", Toast.LENGTH_LONG)
            })
            add(TestItem("ZIP测试") { testZIP() })

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
}