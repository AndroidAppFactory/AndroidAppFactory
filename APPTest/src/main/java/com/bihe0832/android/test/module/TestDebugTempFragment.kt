package com.bihe0832.android.test.module

import android.os.Bundle
import com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encypt.MD5
import com.bihe0832.android.test.module.request.TestHttpActivity
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.TestItem
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

            add(TestItem("点击区扩大Demo") {
                startActivity(TouchRegionActivity::class.java)
            })

            add(TestItem("HTTP Request") {
                startActivity(TestHttpActivity::class.java)
            })

            add(TestItem("文件MD5") {
                testMD5()
            })

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

    private fun preTest() {

    }

    private fun testFunc() {
        ZLog.d("test")
    }

}